// src/component/page/aichat/components/CropSelector.jsx
import CropCard from "./CropCard";

export default function CropSelector({ crop, items, selectedModel, onSelect }) {
  return (
    <div className="cs-wrap">
      <div className="cs-title">작물 선택</div>

      <div className="cs-grid">
        {items.map((item) => (
          <CropCard
            key={item.key}
            item={item}
            selected={crop === item.key}
            onClick={() => onSelect(item.key)}
          />
        ))}
      </div>

      <div className="cs-footer">
        선택 작물: <span className="cs-footer-strong">{crop}</span>
        {/* 화면에는 숨김(값은 유지) */}
        <span className="cs-model-hidden">{selectedModel}</span>
      </div>
    </div>
  );
}
