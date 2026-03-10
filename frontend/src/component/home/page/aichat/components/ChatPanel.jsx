// src/component/page/aichat/components/ChatPanel.jsx
import { useEffect, useMemo, useRef, useState, useCallback } from "react";
import apiClient from "@/api/axios";


/** ====== UI 문구 ====== */
const WELCOME_BOT_MSG = "새 대화를 시작할게요. 어떤 도움이 필요하세요?";
const LOGIN_REQUIRED_TITLE = "로그인이 필요한 기능입니다.";
const LOGIN_REQUIRED_DESC = "챗봇을 사용하려면 로그인 후 다시 시도해 주세요.";

/** ====== API (현재 백 스펙: { success, content, error }) ======
 * - list:   GET  /api/ai-chat/conversations  -> content.elements (status=1만)
 * - create: POST /api/ai-chat/conversations  -> content { conversationId, ... }
 * - detail: GET  /api/ai-chat/conversations/{id} -> content { messages: [] }
 * - send:   POST /api/ai-chat/conversations/{id}/messages -> content { userMessage, assistantMessage, conversation }
 * - hide:   PATCH /api/ai-chat/conversations/{id}/status {status:0}
 */
const API = {
  listConversations: "/api/ai-chat/conversations",
  createConversation: "/api/ai-chat/conversations",
  getConversation: (conversationId) => `/api/ai-chat/conversations/${conversationId}`,
  sendMessage: (conversationId) => `/api/ai-chat/conversations/${conversationId}/messages`,
  patchConversationStatus: (conversationId) => `/api/ai-chat/conversations/${conversationId}/status`,
};

/** ====== 유틸 ====== */
const safeText = (v) => String(v ?? "").replace(/\s+/g, " ").trim();

const makeTitle = (text) => {
  const t = safeText(text);
  return t.length > 18 ? t.slice(0, 12) + "…" : t;
};

const makePreview = (text, max = 12) => {
  const t = safeText(text);
  return t.length > max ? t.slice(0, max) + "…" : t;
};

const makeClientMessageId = () => {
  const rand = Math.random().toString(16).slice(2, 8);
  return `cmsg_${Date.now()}_${rand}`;
};

/** ====== (중요) 응답 파서: 현재 백은 res.data.content ====== */
const pickContent = (res) => res?.data?.content ?? null;

/** ====== API ↔ UI 매핑 (현재 백 스펙 기반) ====== */
const mapApiConversationItemToUi = (item) => {
  const id = item?.conversationId;
  return {
    id: id != null ? String(id) : "",
    title: item?.title ?? "",
    status: item?.status ?? 1,
    lastMessageAt: item?.lastMessageAt ?? null,
    messages: Array.isArray(item?.messages) ? item.messages.map(mapApiMessageToUi) : [],
  };
};

function mapApiMessageToUi(m) {
  const messageId = m?.messageId ?? m?.conversationsMessagesId ?? m?.id;
  const roleRaw = String(m?.role ?? "").toUpperCase();
  const role = roleRaw === "USER" ? "user" : "bot";

  // metadata는 string or object 둘 다 올 수 있음(현재 응답에선 object도 올 수 있음)
  return {
    messageId: messageId != null ? String(messageId) : undefined,
    clientMessageId: m?.clientMessageId ?? undefined,
    role,
    text: m?.content ?? "",
    status: m?.status ?? 1,
    metadata: m?.metadata ?? null,
    createdAt: m?.createdAt ?? null,
    updatedAt: m?.updatedAt ?? null,
  };
}

const mapApiConversationDetailToUi = (data) => {
  const id = data?.conversationId;
  const apiMsgs = Array.isArray(data?.messages) ? data.messages : [];
  return {
    id: id != null ? String(id) : "",
    title: data?.title ?? "",
    status: data?.status ?? 1,
    lastMessageAt: data?.lastMessageAt ?? null,
    messages: apiMsgs.map(mapApiMessageToUi),
  };
};

/** ====== 전송 응답 파서(현재 백 스펙) ======
 * content: { userMessage, assistantMessage, conversation }
 */
const pickFromSendResponse = (content, fallbackUserText, fallbackClientMessageId) => {
  const umRaw = content?.userMessage ?? null;
  const amRaw = content?.assistantMessage ?? null;

  const userMsg = umRaw
    ? mapApiMessageToUi(umRaw)
    : {
        role: "user",
        text: fallbackUserText,
        status: 1,
        clientMessageId: fallbackClientMessageId,
      };

  if (!userMsg.clientMessageId && fallbackClientMessageId) {
    userMsg.clientMessageId = fallbackClientMessageId;
  }

  const botMsg = amRaw ? mapApiMessageToUi(amRaw) : null;

  const title = content?.conversation?.title ?? undefined;
  const lastMessageAt = content?.conversation?.lastMessageAt ?? undefined;

  return { userMsg, botMsg, title, lastMessageAt };
};

export default function ChatPanel() {
  /** ====== 인증 상태 ====== */
  const [authBlocked, setAuthBlocked] = useState(false);
  const needLogin = authBlocked;

  /** ====== 상태 ====== */
  const [chats, setChats] = useState([]);
  const [activeId, setActiveId] = useState(null);

  const [input, setInput] = useState("");
  const [sending, setSending] = useState(false);

  const [loadingList, setLoadingList] = useState(false);
  const [loadingChat, setLoadingChat] = useState(false);

  /** ====== 401 공통 처리 ====== */
  const handleAuthError = useCallback((e) => {
    const status = e?.response?.status;
    if (status === 401 || status === 403 || e?.__AUTH_REQUIRED__) {
      setAuthBlocked(true);
      setSending(false);
      setLoadingChat(false);
      setLoadingList(false);
      return true;
    }
    return false;
  }, []);

  /** ====== 목록 재조회 (백: content.elements) ====== */
  const reloadList = useCallback(async () => {
    if (needLogin) return;

    setLoadingList(true);
    try {
      const res = await apiClient.get(API.listConversations, {
        params: { page: 1, size: 50 },
      });

      const content = pickContent(res) ?? {};
      const elements = Array.isArray(content?.elements) ? content.elements : [];

      const uiList = elements
        .map(mapApiConversationItemToUi)
        .filter((c) => c.id);

      setChats(uiList);

      setActiveId((prev) => {
        const exists = prev && uiList.some((c) => c.id === prev);
        return exists ? prev : uiList[0]?.id ?? null;
      });
    } catch (e) {
      if (handleAuthError(e)) return;
      console.error("conversations list error:", e);
    } finally {
      setLoadingList(false);
    }
  }, [needLogin, handleAuthError]);

  /** ====== 최초 로딩 ====== */
  useEffect(() => {
    if (needLogin) return;
    reloadList();
  }, [needLogin, reloadList]);

  /** ====== 활성 대화 ====== */
  const activeChat = useMemo(
    () => chats.find((c) => c.id === activeId) ?? null,
    [chats, activeId]
  );

  const messageCount = activeChat?.messages?.length ?? 0;

  /** ====== 스크롤 ====== */
  const bottomRef = useRef(null);
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [activeId, messageCount]);

  /** ====== 상세 로딩 (백: content.messages) ====== */
  useEffect(() => {
    if (needLogin) return;
    if (!activeId) return;
    if ((activeChat?.messages?.length ?? 0) > 0) return;

    let mounted = true;

    (async () => {
      setLoadingChat(true);
      try {
        const res = await apiClient.get(API.getConversation(activeId), {
          params: { includeMessages: true, limit: 100 },
        });

        const content = pickContent(res) ?? {};
        const ui = mapApiConversationDetailToUi(content);

        if (!mounted || !ui?.id) return;

        setChats((prev) =>
          (prev ?? []).map((c) => (c.id === activeId ? { ...c, ...ui } : c))
        );
      } catch (e) {
        if (handleAuthError(e)) return;

        if (e?.response?.status === 404) {
          await reloadList();
          return;
        }

        console.error("conversation detail error:", e);
        await reloadList();
      } finally {
        if (mounted) setLoadingChat(false);
      }
    })();

    return () => {
      mounted = false;
    };
  }, [needLogin, activeId, activeChat?.messages?.length, reloadList, handleAuthError]);

  /** ====== pending bot 유틸 ====== */
  const removePendingBots = (conv) => ({
    ...conv,
    messages: (conv.messages ?? []).filter(
      (m) => !(m?.role === "bot" && m?._pending)
    ),
  });

  const replacePendingBotByClientMessageId = (conv, clientMessageId, nextBotText) => {
    const msgs = [...(conv.messages ?? [])];

    const userIdx = msgs.findIndex(
      (m) => m?.role === "user" && m?.clientMessageId === clientMessageId
    );

    if (userIdx >= 0) {
      for (let i = userIdx + 1; i < msgs.length; i++) {
        if (msgs[i]?.role === "bot" && msgs[i]?._pending) {
          msgs[i] = { ...msgs[i], text: nextBotText, _pending: false, status: 1 };
          return { ...conv, messages: msgs };
        }
      }
    }

    for (let i = msgs.length - 1; i >= 0; i--) {
      if (msgs[i]?.role === "bot" && msgs[i]?._pending) {
        msgs[i] = { ...msgs[i], text: nextBotText, _pending: false, status: 1 };
        break;
      }
    }
    return { ...conv, messages: msgs };
  };

  /** ====== 새 대화 ====== */
  const handleNewChat = async () => {
    if (needLogin) return alert(LOGIN_REQUIRED_TITLE);
    if (sending || loadingList) return;

    setSending(true);
    try {
      const res = await apiClient.post(API.createConversation, {});
      const content = pickContent(res) ?? {};
      const ui = mapApiConversationItemToUi(content);

      if (!ui?.id) throw new Error("conversationId missing");

      const uiWithWelcome = {
        ...ui,
        messages: [{ role: "bot", text: WELCOME_BOT_MSG, status: 1 }],
      };

      setChats((prev) => [uiWithWelcome, ...(prev ?? [])]);
      setActiveId(ui.id);
      setInput("");
    } catch (e) {
      if (handleAuthError(e)) return;
      console.error(e);
      alert("새 대화창을 생성하는데 실패하셨습니다.");
    } finally {
      setSending(false);
    }
  };

  /** ====== 대화 숨기기(soft delete) ====== */
  const handleHideConversation = async (conversationId) => {
    if (needLogin) return alert(LOGIN_REQUIRED_TITLE);
    if (!conversationId || sending) return;

    const ok = window.confirm("이 대화를 삭제 하시겠습니까?");
    if (!ok) return;

    const targetId = String(conversationId);
    const wasActive = activeId === targetId;

    // optimistic
    setChats((prev) => {
      const next = (prev ?? []).filter((c) => c.id !== targetId);
      if (wasActive) setActiveId(next[0]?.id ?? null);
      return next;
    });

    setSending(true);
    try {
      await apiClient.patch(API.patchConversationStatus(conversationId), { status: 0 });
    } catch (e) {
      if (handleAuthError(e)) return;
      console.error("patch conversation status error:", e);
      alert("대화 삭제에 실패하셨습니다.");
      await reloadList();
    } finally {
      setSending(false);
    }
  };

  /** ====== 메시지 전송 ====== */
  const handleSend = async () => {
    if (needLogin) return alert(LOGIN_REQUIRED_TITLE);
    if (sending) return;

    const text = safeText(input);
    if (!text || !activeId) return;

    setSending(true);
    const clientMessageId = makeClientMessageId();

    // optimistic
    setChats((prev) =>
      (prev ?? []).map((c) => {
        if (c.id !== activeId) return c;
        const nextTitle = safeText(c.title) ? c.title : makeTitle(text);
        return {
          ...c,
          title: nextTitle,
          messages: [
            ...(c.messages ?? []),
            { role: "user", text, status: 1, clientMessageId },
            { role: "bot", text: "전송중...", _pending: true, status: 1, clientMessageId },
          ],
        };
      })
    );
    setInput("");

    try {
      const res = await apiClient.post(API.sendMessage(activeId), {
        content: text,
        clientMessageId,
      });

      const content = pickContent(res) ?? {};
      const { userMsg, botMsg, title, lastMessageAt } = pickFromSendResponse(
        content,
        text,
        clientMessageId
      );

      setChats((prev) =>
        (prev ?? []).map((c) => {
          if (c.id !== activeId) return c;

          const cleaned = removePendingBots(c);
          const nextMessages = [...(cleaned.messages ?? [])];

          // 서버 userMessage로 optimistic userMessage 보강
          if (userMsg?.clientMessageId) {
            const idx = nextMessages.findIndex(
              (m) => m?.role === "user" && m?.clientMessageId === userMsg.clientMessageId
            );
            if (idx >= 0) nextMessages[idx] = { ...nextMessages[idx], ...userMsg };
            else nextMessages.push(userMsg);
          }

          if (botMsg) nextMessages.push(botMsg);

          return {
            ...cleaned,
            title: safeText(cleaned.title) ? cleaned.title : title ?? makeTitle(text),
            lastMessageAt: lastMessageAt ?? cleaned.lastMessageAt ?? null,
            messages: nextMessages,
          };
        })
      );
    } catch (e) {
      if (handleAuthError(e)) return;

      if (e?.response?.status === 404) {
        await reloadList();
        return;
      }

      console.error("send message error:", e);

      setChats((prev) =>
        (prev ?? []).map((c) =>
          c.id === activeId
            ? replacePendingBotByClientMessageId(
                c,
                clientMessageId,
                "전송에 실패했어요. 잠시 후 다시 시도해 주세요."
              )
            : c
        )
      );

      alert("전송 중 오류가 발생했어요.");
    } finally {
      setSending(false);
    }
  };

  /** ====== Enter 전송(IME 조합 방지) ====== */
  const onKeyDown = (e) => {
    if (e.key !== "Enter") return;
    if (e.nativeEvent?.isComposing || e.keyCode === 229) return;
    handleSend();
  };

  /** ====== 복사 ====== */
  const handleCopyMessage = async (message) => {
    const text = safeText(message?.text);
    if (!text) return;

    try {
      await navigator.clipboard.writeText(text);
    } catch {
      const ta = document.createElement("textarea");
      ta.value = text;
      document.body.appendChild(ta);
      ta.select();
      document.execCommand("copy");
      document.body.removeChild(ta);
    }
  };

  /** ====== Sidebar 목록 ====== */
  const sidebarChats = useMemo(() => chats ?? [], [chats]);

  /** ====== 로그인 필요 화면 ====== */
  if (needLogin) {
    return (
      <div className="cp-shell">
        <section className="cp-main" style={{ width: "100%" }}>
          <div className="cp-empty" style={{ padding: 24 }}>
            <b>{LOGIN_REQUIRED_TITLE}</b>
            <div style={{ marginTop: 8, fontSize: 13, opacity: 0.8 }}>
              {LOGIN_REQUIRED_DESC}
            </div>

            <div style={{ marginTop: 16, display: "flex", gap: 8 }}>
              <a className="cp-btn" href="/login">
                로그인 하러가기
              </a>
              <button className="cp-btn" type="button" onClick={() => setAuthBlocked(false)}>
                다시 시도
              </button>
            </div>
          </div>
        </section>
      </div>
    );
  }

  /** ====== 정상 채팅 UI ====== */
  return (
    <div className="cp-shell">
      {/* Sidebar */}
      <aside className="cp-side">
        <button className="cp-new" onClick={handleNewChat} disabled={sending || loadingList}>
          + 새 대화
        </button>

        <nav className="cp-nav cp-scroll">
          <div className="cp-nav-title">
            내 대화
            {loadingList && <span style={{ marginLeft: 8, fontSize: 12 }}>불러오는 중…</span>}
          </div>

          {sidebarChats.length === 0 ? (
            <div className="cp-nav-empty">
              아직 대화가 없어요. <b>+ 새 대화</b>를 눌러 시작해보세요.
            </div>
          ) : (
            sidebarChats.map((c) => {
              // const title = safeText(c.title) ? c.title : "새 대화";
              const titleRaw = safeText(c.title) ? c.title : "새 대화";
              const title = makePreview(titleRaw, 15);
              const rawLastText = c.messages?.[c.messages.length - 1]?.text ?? "";
              const lastText = makePreview(rawLastText, 12);
              const isActive = c.id === activeId;

              return (
                <div key={c.id} className={`cp-chat-row ${isActive ? "is-active" : ""}`}>
                  <button
                    className={`cp-chat-item ${isActive ? "is-active" : ""}`}
                    disabled={sending}
                    onClick={() => !sending && setActiveId(c.id)}
                    title={title}
                  >
                    <span className="cp-chat-title">{title}</span>
                    <span className="cp-chat-sub" title={rawLastText}>
                      {lastText}
                    </span>
                  </button>

                  <button
                    className="cp-chat-del"
                    disabled={sending}
                    title="대화 숨기기"
                    onClick={() => handleHideConversation(c.id)}
                  >
                    ✕
                  </button>
                </div>
              );
            })
          )}
        </nav>
      </aside>

      {/* Main */}
      <section className="cp-main">
        <div className="cp-chat cp-scroll">
          {!activeChat ? (
            <div className="cp-empty">
              왼쪽에서 대화를 선택하거나 <b>+ 새 대화</b>를 눌러 시작해요.
            </div>
          ) : (
            <div className="cp-msg-list">
              {loadingChat && (
                <div className="cp-empty" style={{ opacity: 0.8 }}>
                  대화 내용을 불러오는 중…
                </div>
              )}

              {(activeChat.messages ?? []).map((m, idx) => {
                const base = m.messageId ?? m.clientMessageId ?? `idx_${idx}`;
                const messageKey = `${m.role}_${base}`;
                const isUser = m.role === "user";

                return (
                  <div
                    key={messageKey}
                    className={[
                      "cp-bubble",
                      isUser ? "is-user" : "is-bot",
                      m?._pending ? "is-pending" : "",
                    ].join(" ")}
                  >
                    {!m?._pending && (
                      <div className={`cp-msg-actions ${isUser ? "is-user" : "is-bot"}`}>
                        <button className="cp-menu-btn" title="복사" onClick={() => handleCopyMessage(m)}>
                          ⧉
                        </button>
                      </div>
                    )}

                    <span className="cp-text">{m.text}</span>

                    {m?._pending && (
                      <span className="cp-dots" aria-label="loading">
                        <i />
                        <i />
                        <i />
                      </span>
                    )}
                  </div>
                );
              })}

              <div ref={bottomRef} />
            </div>
          )}
        </div>

        {/* 입력 영역 */}
        <div className="cp-row">
          <input
            className="cp-input"
            disabled={sending || !activeId}
            placeholder="예) 토마토 잎이 말려요. 원인과 대처법은?"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={onKeyDown}
          />
          <button className="cp-btn" onClick={handleSend} disabled={sending || !activeId}>
            {sending ? "전송중..." : "전송"}
          </button>
        </div>
      </section>
    </div>
  );
}
