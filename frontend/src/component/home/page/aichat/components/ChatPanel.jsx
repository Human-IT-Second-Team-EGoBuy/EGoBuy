// src/component/page/aichat/components/ChatPanel.jsx
import { useEffect, useMemo, useRef, useState } from "react";
import axios from "axios";

/* =========================================================
   [MOCK_ZONE_START] 더미(Mock) 데이터/모드 전용 import
   - 백엔드 완전 연결(실 API 100%) 후 이 블록 삭제
   ========================================================= */
import { DEV_USE_MOCK, DUMMY_CHATS } from "../constants";
/* =========================================================
   [MOCK_ZONE_END]
   ========================================================= */

/** ====== UI 문구 ====== */
const WELCOME_BOT_MSG = "새 대화를 시작할게요. 어떤 도움이 필요하세요?";

/* =========================================================
   [MOCK_ZONE_START] Mock 모드 전용 기본 답변
   - 백엔드에서 assistantMessage가 내려오면 삭제
   ========================================================= */
const DEFAULT_BOT_REPLY =
  "확인했어요. 증상/사진/재배 환경 정보를 더 주시면 더 정확히 안내할게요.";
/* =========================================================
   [MOCK_ZONE_END]
   ========================================================= */

/** ====== API ====== */
const API = {
  listConversations: "/api/ai-chat/conversations", // GET
  createConversation: "/api/ai-chat/conversations", // POST
  getConversation: (conversationId) => `/api/ai-chat/conversations/${conversationId}`, // GET
  sendMessage: (conversationId) => `/api/ai-chat/conversations/${conversationId}/messages`, // POST
  // 대화 숨기기(soft delete)
  patchConversationStatus: (conversationId) =>
    `/api/ai-chat/conversations/${conversationId}/status`, // PATCH { status:0|1 }
};

/** ====== 유틸 ====== */
const safeText = (v) => String(v ?? "").replace(/\s+/g, " ").trim();
const makeTitle = (text) => {
  const t = safeText(text);
  return t.length > 18 ? t.slice(0, 18) + "…" : t;
};
const makePreview = (text, max = 12) => {
  const t = safeText(text);
  return t.length > max ? t.slice(0, max) + "…" : t;
};
const makeClientMessageId = () => {
  const rand = Math.random().toString(16).slice(2, 8);
  return `cmsg_${Date.now()}_${rand}`;
};

/* =========================================================
   [MOCK_ZONE_START] 더미 데이터 안전 복제 유틸
   - 백엔드 완전 연결 후 삭제
   ========================================================= */
const cloneDummyChats = (list) =>
  (Array.isArray(list) ? list : []).map((c) => ({
    ...c,
    id: String(c?.id ?? ""),
    title: c?.title ?? "",
    status: c?.status ?? 1, // hide 지원 위해 유지
    lastMessageAt: null,
    messages: Array.isArray(c?.messages)
      ? c.messages.map((m, idx) => ({
          ...m,
          messageId: m?.messageId ?? `dm_${c?.id ?? "c"}_${idx}`,
          status: m?.status ?? 1,
          createdAt: m?.createdAt ?? new Date().toISOString(),
          updatedAt: m?.updatedAt ?? null,
        }))
      : [],
  }));
/* =========================================================
   [MOCK_ZONE_END]
   ========================================================= */

/** ====== API ↔ UI 매핑 ====== */
const mapApiConversationItemToUi = (item) => {
  const id =
    item?.conversationId ??
    item?.conversation_id ??
    item?.id ??
    item?.conversationID;

  return {
    id: id != null ? String(id) : "",
    title: item?.title ?? "",
    status: item?.status ?? 1, // 0이면 숨김
    lastMessageAt: item?.lastMessageAt ?? item?.last_message_at ?? null,
    messages: [],
  };
};

const mapApiMessageToUi = (m) => {
  const messageId =
    m?.messageId ??
    m?.conversationsMessagesId ??
    m?.conversations_messages_id ??
    m?.id;

  const roleRaw = String(m?.role ?? "").toUpperCase();
  const role = roleRaw === "USER" ? "user" : "bot";
  const text = m?.content ?? m?.text ?? "";

  return {
    messageId: messageId != null ? String(messageId) : undefined,
    clientMessageId: m?.clientMessageId ?? m?.client_message_id ?? undefined,
    role,
    text,
    status: m?.status ?? 1,
  };
};

const mapApiConversationDetailToUi = (data) => {
  const id =
    data?.conversationId ??
    data?.conversation_id ??
    data?.id ??
    data?.conversationID;

  const apiMsgs = Array.isArray(data?.messages) ? data.messages : [];
  const messages = apiMsgs.map(mapApiMessageToUi);

  return {
    id: id != null ? String(id) : "",
    title: data?.title ?? "",
    status: data?.status ?? 1,
    lastMessageAt: data?.lastMessageAt ?? data?.last_message_at ?? null,
    messages,
  };
};

/** ====== 전송 응답 파서 ====== */
const pickFromSendResponseV3 = (data, fallbackUserText, fallbackClientMessageId) => {
  const umRaw = data?.userMessage ?? data?.user_message;
  const amRaw =
    data?.assistantMessage ??
    data?.assistant_message ??
    data?.botMessage ??
    data?.bot_message;

  const userMsg = umRaw
    ? mapApiMessageToUi(umRaw)
    : { role: "user", text: fallbackUserText, status: 1, clientMessageId: fallbackClientMessageId };

  if (!userMsg.clientMessageId && fallbackClientMessageId) {
    userMsg.clientMessageId = fallbackClientMessageId;
  }

  const botMsg = amRaw ? mapApiMessageToUi(amRaw) : null;

  const title =
    data?.conversation?.title ??
    data?.conversationTitle ??
    data?.title ??
    undefined;

  const lastMessageAt =
    data?.conversation?.lastMessageAt ??
    data?.conversation?.last_message_at ??
    undefined;

  return { userMsg, botMsg, title, lastMessageAt };
};

axios.defaults.headers.common["Content-Type"] = "application/json";

export default function ChatPanel() {
  /* =========================================================
     [MOCK_ZONE_START] Mock 모드 상태
     - 백엔드 완전 연결 후 이 state 자체 삭제
     ========================================================= */
  const [useDummy, setUseDummy] = useState(DEV_USE_MOCK);
  /* =========================================================
     [MOCK_ZONE_END]
     ========================================================= */

  const [chats, setChats] = useState([]);
  const [activeId, setActiveId] = useState(null);

  const [input, setInput] = useState("");
  const [sending, setSending] = useState(false);

  const [loadingList, setLoadingList] = useState(false);
  const [loadingChat, setLoadingChat] = useState(false);

  // 목록 숨김(soft delete) 토글: OFF면 숨김대화는 목록에서 제외
  const [showHidden, setShowHidden] = useState(false);

  /** ====== 최초 로딩 ====== */
  useEffect(() => {
    let mounted = true;

    /* =========================================================
       [MOCK_ZONE_START] Mock 최초 로딩(더미 세팅)
       ========================================================= */
    if (useDummy) {
      const fallback = cloneDummyChats(DUMMY_CHATS);
      setChats(fallback);
      setActiveId(fallback.find((c) => (showHidden ? true : (c.status ?? 1) !== 0))?.id ?? null);
      return () => {
        mounted = false;
      };
    }
    /* =========================================================
       [MOCK_ZONE_END]
       ========================================================= */

    (async () => {
      setLoadingList(true);
      try {
        const res = await axios.get(API.listConversations, {
          params: { page: 1, size: 50, status: showHidden ? undefined : 1 },
        });
        const items = res?.data?.data?.items ?? res?.data?.data ?? res?.data?.items ?? [];
        const uiList = (Array.isArray(items) ? items : [])
          .map(mapApiConversationItemToUi)
          .filter((c) => c.id)
          .filter((c) => (showHidden ? true : (c.status ?? 1) !== 0));

        if (!mounted) return;
        setChats(uiList);
        setActiveId(uiList[0]?.id ?? null);
      } catch (e) {
        console.error("conversations list error:", e);

        /* =========================================================
           [MOCK_ZONE_START] API 실패 시 Mock fallback
           ========================================================= */
        if (!mounted) return;
        setUseDummy(true);
        const fallback = cloneDummyChats(DUMMY_CHATS);
        setChats(fallback);
        setActiveId(fallback[0]?.id ?? null);
        /* =========================================================
           [MOCK_ZONE_END]
           ========================================================= */
      } finally {
        if (mounted) setLoadingList(false);
      }
    })();

    return () => {
      mounted = false;
    };
  }, [useDummy, showHidden]);

  /** ====== 활성 대화 ====== */
  const activeChat = useMemo(() => chats.find((c) => c.id === activeId) ?? null, [chats, activeId]);
  const messageCount = activeChat?.messages?.length ?? 0;

  /** ====== 스크롤 ====== */
  const bottomRef = useRef(null);
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [activeId, messageCount]);

  /** ====== 채팅 선택 시: 상세 로딩 ====== */
  useEffect(() => {
    if (!activeId) return;

    /* =========================================================
       [MOCK_ZONE_START] 더미 모드에서는 상세 로딩 불필요
       ========================================================= */
    if (useDummy) return;
    /* =========================================================
       [MOCK_ZONE_END]
       ========================================================= */

    if ((activeChat?.messages?.length ?? 0) > 0) return;

    let mounted = true;

    (async () => {
      setLoadingChat(true);
      try {
        const res = await axios.get(API.getConversation(activeId), {
          params: { includeMessages: true, limit: 100, includeHidden: showHidden ? true : undefined },
        });

        const data = res?.data?.data ?? res?.data ?? {};
        const ui = mapApiConversationDetailToUi(data);

        if (!mounted || !ui?.id) return;
        setChats((prev) => (prev ?? []).map((c) => (c.id === activeId ? { ...c, ...ui } : c)));
      } catch (e) {
        console.error("conversation detail error:", e);
      } finally {
        if (mounted) setLoadingChat(false);
      }
    })();

    return () => {
      mounted = false;
    };
  }, [activeId, useDummy, showHidden, activeChat?.messages?.length]);

  /** ====== pending bot 유틸 ====== */
  const removePendingBots = (conv) => ({
    ...conv,
    messages: (conv.messages ?? []).filter((m) => !(m?.role === "bot" && m?._pending)),
  });

  const replacePendingBotByClientMessageId = (conv, clientMessageId, nextBotText) => {
    const msgs = [...(conv.messages ?? [])];

    const userIdx = msgs.findIndex((m) => m?.role === "user" && m?.clientMessageId === clientMessageId);
    if (userIdx >= 0) {
      for (let i = userIdx + 1; i < msgs.length; i++) {
        if (msgs[i]?.role === "bot" && msgs[i]?._pending) {
          msgs[i] = { ...msgs[i], role: "bot", text: nextBotText, _pending: false, status: 1 };
          return { ...conv, messages: msgs };
        }
      }
    }

    for (let i = msgs.length - 1; i >= 0; i--) {
      if (msgs[i]?.role === "bot" && msgs[i]?._pending) {
        msgs[i] = { ...msgs[i], role: "bot", text: nextBotText, _pending: false, status: 1 };
        break;
      }
    }
    return { ...conv, messages: msgs };
  };

  /** ====== 새 대화 ====== */
  const handleNewChat = async () => {
    if (sending || loadingList) return;

    /* =========================================================
       [MOCK_ZONE_START] Mock 새 대화 생성
       ========================================================= */
    if (useDummy) {
      const id = `c${Date.now()}`;
      const newChat = {
        id,
        title: "",
        status: 1,
        lastMessageAt: null,
        messages: [{ role: "bot", text: WELCOME_BOT_MSG, status: 1, messageId: `dm_${id}_0` }],
      };

      setChats((prev) => [newChat, ...(prev ?? [])]);
      setActiveId(id);
      setInput("");
      return;
    }
    /* =========================================================
       [MOCK_ZONE_END]
       ========================================================= */

    setSending(true);
    try {
      const res = await axios.post(API.createConversation, {});
      const created = res?.data?.data ?? res?.data ?? {};
      const ui = mapApiConversationItemToUi(created);
      if (!ui?.id) throw new Error("conversationId missing");

      const uiWithWelcome = { ...ui, messages: [{ role: "bot", text: WELCOME_BOT_MSG, status: 1 }] };
      setChats((prev) => [uiWithWelcome, ...(prev ?? [])]);
      setActiveId(ui.id);
      setInput("");
    } catch (e) {
      console.error(e);
      alert("새 대화 생성 실패");
    } finally {
      setSending(false);
    }
  };

  /** ====== 대화 숨기기(soft delete) ====== */
  const patchConversationStatus = async (conversationId, nextStatus) => {
    if (!conversationId) return;
    if (sending) return;

    // optimistic
    setChats((prev) =>
      (prev ?? []).map((c) => (c.id === String(conversationId) ? { ...c, status: nextStatus } : c))
    );

    /* =========================================================
       [MOCK_ZONE_START] 더미면 API 호출 없이 종료
       ========================================================= */
    if (useDummy) return;
    /* =========================================================
       [MOCK_ZONE_END]
       ========================================================= */

    setSending(true);
    try {
      await axios.patch(API.patchConversationStatus(conversationId), { status: nextStatus });
    } catch (e) {
      console.error("patch conversation status error:", e);
      alert("대화 숨김 실패");

      // rollback
      setChats((prev) =>
        (prev ?? []).map((c) =>
          c.id === String(conversationId) ? { ...c, status: nextStatus === 0 ? 1 : 0 } : c
        )
      );
    } finally {
      setSending(false);
    }
  };

  const handleHideConversation = async (conversationId) => {
    const ok = window.confirm("이 대화를 숨길까요? (목록에서 사라집니다)");
    if (!ok) return;

    await patchConversationStatus(conversationId, 0);

    // showHidden이 꺼져있으면 목록에서 제거 + activeId 보정
    if (!showHidden) {
      setChats((prev) => (prev ?? []).filter((c) => c.id !== String(conversationId)));

      if (activeId === String(conversationId)) {
        const next = (chats ?? []).find((x) => x.id !== String(conversationId) && (x.status ?? 1) !== 0);
        setActiveId(next?.id ?? null);
      }
    }
  };

  /** ====== 메시지 전송 ====== */
  const handleSend = async () => {
    if (sending) return;

    const text = safeText(input);
    if (!text || !activeId) return;

    setSending(true);
    const clientMessageId = makeClientMessageId();

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
      /* =========================================================
         [MOCK_ZONE_START] Mock 전송 응답 (백엔드 연결 후 삭제)
         ========================================================= */
      if (useDummy) {
        setChats((prev) =>
          (prev ?? []).map((c) =>
            c.id === activeId
              ? replacePendingBotByClientMessageId(c, clientMessageId, DEFAULT_BOT_REPLY)
              : c
          )
        );
        return;
      }
      /* =========================================================
         [MOCK_ZONE_END]
         ========================================================= */

      const res = await axios.post(API.sendMessage(activeId), { content: text, clientMessageId });
      const data = res?.data?.data ?? res?.data ?? {};

      const { userMsg, botMsg, title, lastMessageAt } = pickFromSendResponseV3(
        data,
        text,
        clientMessageId
      );

      setChats((prev) =>
        (prev ?? []).map((c) => {
          if (c.id !== activeId) return c;

          const cleaned = removePendingBots(c);
          const nextMessages = [...(cleaned.messages ?? [])];

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

  /** ====== 복사(유일 기능) ====== */
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

  /** ====== Sidebar 목록(숨김 포함 토글 반영) ====== */
  const sidebarChats = useMemo(() => {
    const arr = chats ?? [];
    return showHidden ? arr : arr.filter((c) => (c.status ?? 1) !== 0);
  }, [chats, showHidden]);

  return (
    <div className="cp-shell">
      {/* Sidebar */}
      <aside className="cp-side">
        <button className="cp-new" onClick={handleNewChat} disabled={sending || loadingList}>
          + 새 대화
        </button>

        <div className="cp-side-tools">
          <label className="cp-toggle">
            <input
              type="checkbox"
              checked={showHidden}
              onChange={(e) => setShowHidden(e.target.checked)}
              disabled={sending || loadingList}
            />
            <span>숨김 포함</span>
          </label>
        </div>

        <nav className="cp-nav cp-scroll">
          <div className="cp-nav-title">
            내 대화
            {!useDummy && loadingList && (
              <span style={{ marginLeft: 8, fontSize: 12 }}>불러오는 중…</span>
            )}
          </div>

          {sidebarChats.length === 0 ? (
            <div className="cp-nav-empty">
              아직 대화가 없어요. <b>+ 새 대화</b>를 눌러 시작해보세요.
            </div>
          ) : (
            sidebarChats.map((c) => {
              const title = safeText(c.title) ? c.title : "새 대화";
              const rawLastText = c.messages?.[c.messages.length - 1]?.text ?? "";
              const lastText = makePreview(rawLastText, 12);
              const isActive = c.id === activeId;
              const hidden = (c.status ?? 1) === 0;

              return (
                <div
                  key={c.id}
                  className={`cp-chat-row ${isActive ? "is-active" : ""} ${hidden ? "is-hidden" : ""}`}
                >
                  <button
                    className={`cp-chat-item ${isActive ? "is-active" : ""}`}
                    disabled={sending}
                    onClick={() => !sending && setActiveId(c.id)}
                    title={title}
                  >
                    <span className="cp-chat-title">
                      {title}
                      {hidden && <em className="cp-badge-hidden">숨김</em>}
                    </span>
                    <span className="cp-chat-sub" title={rawLastText}>
                      {lastText}
                    </span>
                  </button>

                  {/* 숨김 버튼(✕): 숨김 포함 OFF일 때도 가능 */}
                  {!hidden && (
                    <button
                      className="cp-chat-del"
                      disabled={sending}
                      title="대화 숨기기"
                      onClick={() => handleHideConversation(c.id)}
                    >
                      ✕
                    </button>
                  )}
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
              {!useDummy && loadingChat && (
                <div className="cp-empty" style={{ opacity: 0.8 }}>
                  대화 내용을 불러오는 중…
                </div>
              )}

              {(activeChat.messages ?? [])
                .filter((m) => (showHidden ? true : (m?.status ?? 1) !== 0))
                .map((m, idx) => {
                  const messageKey = m.messageId ?? m.clientMessageId ?? `idx_${idx}`;
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

/* =========================================================
   [MOCK_CLEANUP_CHECKLIST]
   백엔드 완전 연결 후 "검색/삭제" 기준
   ---------------------------------------------------------
   1) [MOCK_ZONE_START] ~ [MOCK_ZONE_END] 블록 통째로 삭제
   2) useDummy 관련 분기 제거 후:
      - useDummy state 삭제
      - useEffect deps에서 useDummy 제거
      - sidebar 로딩 표시의 (MOCK) 제거
   ========================================================= */
