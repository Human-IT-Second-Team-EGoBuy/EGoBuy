// src/component/page/aichat/components/CropCard.jsx
export default function CropCard({ item, selected, onClick }) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`cc-btn ${selected ? "cc-selected" : "cc-default"}`}
    >
      <div className="cc-emoji">{item.emoji}</div>
      <div className="cc-label">{item.label}</div>
    </button>
  );
}
