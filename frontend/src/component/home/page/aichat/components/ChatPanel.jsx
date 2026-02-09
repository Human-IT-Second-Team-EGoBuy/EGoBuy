import { useEffect, useMemo, useRef, useState} from "react";

// src/component/page/aichat/components/ChatPanel.jsx
export default function ChatPanel() {
  // 1) 가짜 대화 목록(처음 로드용)
  const initialChats = useMemo(
    () => [
      {
        id: "c1",
        title: "토마토 잎 말림 문의",
        messages: [
          { role: "bot", text: "토마토 잎이 말리는 원인은 수분 스트레스/해충/바이러스 등 다양해요." },
          { role: "user", text: "최근 잎이 위로 말리고 노랗게 변해요." },
          { role: "bot", text: "관수/온도 변화 여부와 잎 뒷면 해충(진딧물) 유무를 먼저 확인해볼까요?" },
        ],
      },
      {
        id: "c2",
        title: "사과 점무늬 증상",
        messages: [
          { role: "user", text: "사과 잎에 갈색 점이 많이 생겼어요." },
          { role: "bot", text: "탄저병/겹무늬썩음병/점무늬낙엽병 가능성이 있어요. 사진이 있으면 더 정확해요." },
        ],
      },
      {
        id: "c3",
        title: "감자 잎 반점",
        messages: [
          { role: "user", text: "감자 잎에 반점이 퍼져요." },
          { role: "bot", text: "역병/겹무늬병 등을 의심할 수 있어요. 발생 시기와 날씨도 중요해요." },
        ],
      },
      {
        id: "c4",
        title: "벼(쌀) 생육 상담",
        messages: [
          { role: "user", text: "벼 잎 끝이 마르는 것 같아요." },
          { role: "bot", text: "수분/염류/비료 과다/병해 가능성이 있어요. 논 물관리 상태를 알려주세요." },
        ],
      },
    ],
    []
  );

  const [chats, setChats] = useState(initialChats);
  const [activeId, setActiveId] = useState(initialChats[0]?.id ?? null);
  const [input, setInput] = useState("");

  const activeChat = chats.find((c) => c.id === activeId);

  // 2) 새 대화 추가
  const handleNewChat = () => {
    const id = `c${Date.now()}`;
    const newChat = {
      id,
      title: "",
      messages: [{ role: "bot", text: "새 대화를 시작할게요. 어떤 도움이 필요하세요?" }],
    };
    setChats((prev) => [newChat, ...prev]);
    setActiveId(id);
    setInput("");
  };

  const makeTitle = (text) => {
    const t = String(text ?? "").replace(/\s+/g, " ").trim();
    return t.length > 18 ? t.slice(0, 18) + "…" : t;
  };

  // 3) 메시지 전송(목업: 유저 메시지 추가 + 봇 자동응답)
  const handleSend = () => {
    const text = input.trim();
    if (!text || !activeId) return;

    setChats((prev) =>
      prev.map((c) => {
        if (c.id !== activeId) return c;

        const nextMessages = [
          ...c.messages,
          { role: "user", text },
          { role: "bot", text: "확인했어요. 증상/사진/재배 환경 정보를 더 주시면 더 정확히 안내할게요." },
        ];

        const firstUser = nextMessages.find((m) => m.role === "user");
        const nextTitle =
          c.title && c.title.trim()
            ? c.title
            : makeTitle(text);

        return { ...c, title: nextTitle, messages: nextMessages };
      })
    );

    setInput("");
  };

  const onKeyDown = (e) => {
    // 한글 IME 조합 중 Enter는 무시
    if (e.key === "Enter") {
      if (e.nativeEvent.isComposing || e.keyCode === 229) return;
      handleSend();
    }
  };

  const bottomRef = useRef(null);
  // 메시지/대화가 바뀔 때 맨 아래로 자동 스크롤
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [activeId, activeChat?.messages?.length]);

  return (
    <div className="cp-shell">
      {/* Sidebar */}
      <aside className="cp-side">
        <button className="cp-new" onClick={handleNewChat}>
          + 새 대화
        </button>

        <nav className="cp-nav cp-scroll">
          <div className="cp-nav-title">내 대화</div>

          {chats.length === 0 ? (
            <div className="cp-nav-empty">아직 대화가 없어요. 새 대화를 시작해보세요.</div>
          ) : (
            chats.map((c) => (
              <button
                key={c.id}
                className={`cp-chat-item ${c.id === activeId ? "is-active" : ""}`}
                onClick={() => setActiveId(c.id)}
                title={c.title}
              >
                <span className="cp-chat-title">{c.title?.trim() ? c.title : "새 대화"}</span>
                <span className="cp-chat-sub">
                  {c.messages?.[c.messages.length - 1]?.text ?? ""}
                </span>
              </button>
            ))
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
              {activeChat.messages.map((m, idx) => (
                <div
                  key={idx}
                  className={`cp-bubble ${m.role === "user" ? "is-user" : "is-bot"}`}
                >
                  {m.text}
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
            placeholder="예) 토마토 잎이 말려요. 원인과 대처법은?"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={onKeyDown}
          />
          <button className="cp-btn" onClick={handleSend}>
            전송
          </button>
        </div>
      </section>
    </div>
  );
}
