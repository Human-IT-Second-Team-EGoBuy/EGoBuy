// src/component/page/aichat/components/ChatPanel.jsx
export default function ChatPanel() {
  return (
    <div className="cp-wrap">
      <div className="cp-note">(챗봇 UI는 다음 단계에서 붙이면 돼요)</div>

      <div className="cp-row">
        <input
          className="cp-input"
          placeholder="예) 토마토 잎이 말려요. 원인과 대처법은?"
        />
        <button className="cp-btn">전송</button>
      </div>
    </div>
  );
}
