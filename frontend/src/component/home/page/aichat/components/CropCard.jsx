// src/component/page/aichat/components/CropCard.jsx
export default function CropCard({ emoji, label, selected, onClick }) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`cc-btn ${selected ? "cc-selected" : "cc-default"}`}
    >
      <div className="cc-emoji">{emoji}</div>
      <div className="cc-label">{label}</div>
    </button>
  );
}
