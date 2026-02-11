// src/component/page/aichat/components/ChatPanel.jsx
import { useEffect, useMemo, useRef, useState } from "react";
// import axios from "axios";

import { DUMMY_CHATS } from "../constants";

/** ====== UI 문구 ====== */
const WELCOME_BOT_MSG = "새 대화를 시작할게요. 어떤 도움이 필요하세요?";
const DEFAULT_BOT_REPLY =
  "확인했어요. 증상/사진/재배 환경 정보를 더 주시면 더 정확히 안내할게요.";

/** ====== 유틸 ====== */
const safeText = (v) => String(v ?? "").replace(/\s+/g, " ").trim();
const makeTitle = (text) => {
  const t = safeText(text);
  return t.length > 18 ? t.slice(0, 18) + "…" : t;
};

/** ====== 더미 데이터 안전 복제(상수 원본 보호) ====== */
const cloneDummyChats = (list) =>
  (Array.isArray(list) ? list : []).map((c) => ({
    ...c,
    id: String(c?.id ?? ""),
    title: c?.title ?? "",
    messages: Array.isArray(c?.messages) ? c.messages.map((m) => ({ ...m })) : [],
  }));

/** ====== API 대화 -> UI 대화 매핑(실API 대비) ====== */
const mapApiConversationToUi = (conv) => {
  const id =
    conv?.conversation_id ??
    conv?.id ??
    conv?.conversationId ??
    conv?.conversationID;

  const title = conv?.title ?? "";

  const apiMsgs =
    conv?.messages ??
    conv?.conversations_messages ??
    conv?.conversation_messages ??
    conv?.conversationMessages ??
    [];

  const messages = Array.isArray(apiMsgs)
    ? apiMsgs
        .filter(Boolean)
        .map((m) => {
          const roleRaw = String(m?.role ?? "").toUpperCase();
          const role = roleRaw === "USER" ? "user" : "bot"; // ASSISTANT/SYSTEM 모두 bot로 흡수
          const text = m?.content ?? m?.text ?? "";
          return { role, text };
        })
    : [];

  return { id: id != null ? String(id) : "", title, messages, _raw: conv };
};

/** ====== 메시지 전송 응답 -> UI 메시지(실API 대비) ====== */
const pickMessagesFromSendResponse = (data, fallbackUserText) => {
  // 1) { userMessage, botMessage, title }
  if (data?.userMessage || data?.botMessage) {
    const um = data.userMessage
      ? { role: "user", text: data.userMessage.content ?? data.userMessage.text ?? "" }
      : { role: "user", text: fallbackUserText };

    const bm = data.botMessage
      ? { role: "bot", text: data.botMessage.content ?? data.botMessage.text ?? "" }
      : null;

    return { userMsg: um, botMsg: bm, title: data.title };
  }

  // 2) { messages: [ ... ] }
  if (Array.isArray(data?.messages)) {
    const mapped = data.messages.map((m) => {
      const roleRaw = String(m?.role ?? "").toUpperCase();
      const role = roleRaw === "USER" ? "user" : "bot";
      const text = m?.content ?? m?.text ?? "";
      return { role, text };
    });

    const lastUser = [...mapped].reverse().find((m) => m.role === "user") ?? {
      role: "user",
      text: fallbackUserText,
    };
    const lastBot = [...mapped].reverse().find((m) => m.role === "bot") ?? null;

    return { userMsg: lastUser, botMsg: lastBot, title: data?.title };
  }

  // 3) { answer: "..." }
  if (data?.answer) {
    return {
      userMsg: { role: "user", text: fallbackUserText },
      botMsg: { role: "bot", text: String(data.answer) },
      title: data?.title,
    };
  }

  // fallback
  return {
    userMsg: { role: "user", text: fallbackUserText },
    botMsg: { role: "bot", text: DEFAULT_BOT_REPLY },
    title: data?.title,
  };
};

export default function ChatPanel() {
  const [chats, setChats] = useState([]);
  const [activeId, setActiveId] = useState(null);
  const [input, setInput] = useState("");
  const [sending, setSending] = useState(false);

  /** ====== 최초 로딩: 더미 ====== */
  useEffect(() => {
    const cloned = cloneDummyChats(DUMMY_CHATS);
    setChats(cloned);
    setActiveId(cloned[0]?.id ?? null);

    // ===== 실API 모드(현재 주석) =====
    // (async () => {
    //   try {
    //     const res = await axios.get("/api/ai-chat/conversations");
    //     const list = Array.isArray(res.data?.data) ? res.data.data : [];
    //     const uiList = list.map(mapApiConversationToUi).filter((c) => c.id);
    //     setChats(uiList);
    //     setActiveId(uiList[0]?.id ?? null);
    //   } catch (e) {
    //     console.error(e);
    //     const fallback = cloneDummyChats(DUMMY_CHATS);
    //     setChats(fallback);
    //     setActiveId(fallback[0]?.id ?? null);
    //   }
    // })();
  }, []);

  /** ====== 활성 대화 ====== */
  const activeChat = useMemo(() => chats.find((c) => c.id === activeId) ?? null, [chats, activeId]);
  const messageCount = activeChat?.messages?.length ?? 0;

  /** ====== 스크롤 ====== */
  const bottomRef = useRef(null);
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [activeId, messageCount]);

  /** ====== 새 대화 ====== */
  const handleNewChat = async () => {
    if (sending) return;

    const id = `c${Date.now()}`;
    const newChat = {
      id,
      title: "",
      messages: [{ role: "bot", text: WELCOME_BOT_MSG }],
    };

    setChats((prev) => [newChat, ...(prev ?? [])]);
    setActiveId(id);
    setInput("");

    // ===== 실API 모드(현재 주석) =====
    // setSending(true);
    // try {
    //   const res = await axios.post("/api/ai-chat/conversations", {});
    //   const created = res.data?.data;
    //   const ui = mapApiConversationToUi(created);
    //   if (!ui?.id) return;
    //   setChats((prev) => [ui, ...(prev ?? [])]);
    //   setActiveId(ui.id);
    //   setInput("");
    // } catch (e) {
    //   console.error(e);
    //   alert("새 대화 생성 실패");
    // } finally {
    //   setSending(false);
    // }
  };

  /** ====== pending bot 교체 유틸 ====== */
  const replacePendingBot = (conv, nextBotText) => {
    const msgs = [...(conv.messages ?? [])];
    for (let i = msgs.length - 1; i >= 0; i--) {
      if (msgs[i]?.role === "bot" && msgs[i]?._pending) {
        msgs[i] = { role: "bot", text: nextBotText };
        break;
      }
    }
    return { ...conv, messages: msgs };
  };

  const removePendingBots = (conv) => ({
    ...conv,
    messages: (conv.messages ?? []).filter((m) => !(m?.role === "bot" && m?._pending)),
  });

  /** ====== 메시지 전송 ====== */
  const handleSend = async () => {
    if (sending) return;

    const text = safeText(input);
    if (!text || !activeId) return;

    setSending(true);

    // 1) 일단 UI에 user + pending bot 추가 (로컬/실API 공통 UX)
    setChats((prev) =>
      (prev ?? []).map((c) => {
        if (c.id !== activeId) return c;

        //  제목 자동 갱신 규칙: title이 비어있을 때만 "첫 유저 메시지"로 생성
        const nextTitle = safeText(c.title) ? c.title : makeTitle(text);

        return {
          ...c,
          title: nextTitle,
          messages: [
            ...(c.messages ?? []),
            { role: "user", text },
            { role: "bot", text: "전송중...", _pending: true }, // ✅ pending
          ],
        };
      })
    );
    setInput("");

    try {
      // ===== 로컬(더미) 응답 =====
      setChats((prev) =>
        (prev ?? []).map((c) => {
          if (c.id !== activeId) return c;
          return replacePendingBot(c, DEFAULT_BOT_REPLY);
        })
      );

      // ===== 실API 모드(현재 주석) =====
      // const res = await axios.post(`/api/ai-chat/conversations/${activeId}/messages`, { text });
      // const data = res.data?.data;
      // const { userMsg, botMsg, title } = pickMessagesFromSendResponse(data, text);
      //
      // setChats((prev) =>
      //   (prev ?? []).map((c) => {
      //     if (c.id !== activeId) return c;
      //
      //        pending 제거 후, API 메시지 반영
      //     const cleaned = removePendingBots(c);
      //
      //     return {
      //       ...cleaned,
      //       title: safeText(cleaned.title) ? cleaned.title : (title ?? makeTitle(text)),
      //       messages: [...(cleaned.messages ?? []), userMsg, botMsg].filter(Boolean),
      //     };
      //   })
      // );
    } catch (e) {
      console.error(e);

      // 실패 시 pending을 에러 문구로 교체
      setChats((prev) =>
        (prev ?? []).map((c) => {
          if (c.id !== activeId) return c;
          return replacePendingBot(c, "전송에 실패했어요. 잠시 후 다시 시도해 주세요.");
        })
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

  return (
    <div className="cp-shell">
      {/* Sidebar */}
      <aside className="cp-side">
        <button className="cp-new" onClick={handleNewChat} disabled={sending}>
          + 새 대화
        </button>

        <nav className="cp-nav cp-scroll">
          <div className="cp-nav-title">내 대화</div>

          {chats.length === 0 ? (
            <div className="cp-nav-empty">아직 대화가 없어요. 새 대화를 시작해보세요.</div>
          ) : (
            chats.map((c) => {
              const title = safeText(c.title) ? c.title : "새 대화";
              const lastText = c.messages?.[c.messages.length - 1]?.text ?? "";
              return (
                <button
                  key={c.id}
                  disabled={sending}
                  className={`cp-chat-item ${c.id === activeId ? "is-active" : ""}`}
                  onClick={() => !sending && setActiveId(c.id)}
                  title={title}
                >
                  <span className="cp-chat-title">{title}</span>
                  <span className="cp-chat-sub">{lastText}</span>
                </button>
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
              {(activeChat.messages ?? []).map((m, idx) => (
                <div
                  key={idx}
                  className={[
                    "cp-bubble",
                    m.role === "user" ? "is-user" : "is-bot",
                    m?._pending ? "is-pending" : "",
                  ].join(" ")}
                >
                  <span className="cp-text">{m.text}</span>

                  {/* pending일 때 점 애니메이션 */}
                  {m?._pending && (
                    <span className="cp-dots" aria-label="loading">
                      <i />
                      <i />
                      <i />
                    </span>
                  )}
                </div>
              ))}
              <div ref={bottomRef} />
            </div>
          )}
        </div>

        {/* 입력 영역 */}
        <div className="cp-row">
          <input
            className="cp-input"
            disabled={sending}
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
