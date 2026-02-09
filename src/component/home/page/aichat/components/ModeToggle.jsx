// src/component/page/aichat/components/ModeToggle.jsx
export default function ModeToggle({ mode, onChange }) {
  return (
    <div className="mt-wrap">
      <button
        onClick={() => onChange("chat")}
        className={`mt-btn ${mode === "chat" ? "mt-active" : "mt-idle"}`}
      >
        챗봇
      </button>

      <button
        onClick={() => onChange("vision")}
        className={`mt-btn ${mode === "vision" ? "mt-active" : "mt-idle"}`}
      >
        이미지 진단
      </button>
    </div>
  );
}
