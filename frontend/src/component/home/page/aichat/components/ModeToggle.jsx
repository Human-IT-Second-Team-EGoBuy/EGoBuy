// src/component/page/aichat/components/ModeToggle.jsx
export default function ModeToggle({ mode, setMode }) {
  return (
    <div className="mt-wrap">
      <button
        type="button"
        onClick={() => setMode("chat")}
        className={`mt-btn ${mode === "chat" ? "mt-active" : "mt-idle"}`}
      >
        챗봇
      </button>

      <button
        type="button"
        onClick={() => setMode("vision")}
        className={`mt-btn ${mode === "vision" ? "mt-active" : "mt-idle"}`}
      >
        이미지 진단
      </button>
    </div>
  );
}
