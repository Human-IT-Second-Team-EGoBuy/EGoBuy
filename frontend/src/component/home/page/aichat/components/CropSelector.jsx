import CropCard from "./CropCard";

export default function CropSelector({ cropId, cropName, items, onSelect }) {
  return (
    <div className="cs-wrap">
      <div className="cs-title">작물 선택</div>

      <div className="cs-grid">
        {(items ?? []).map((item) => (
          <CropCard
            key={String(item.crop_id)}
            emoji={item.emoji ?? "🌱"}
            label={item.crop_name ?? ""}
            selected={Number(cropId) === Number(item.crop_id)}
            onClick={() => onSelect(item.crop_id)}
          />
        ))}
      </div>

      <div className="cs-footer">
        선택 작물: <span className="cs-footer-strong">{cropName || "선택 없음"}</span>
      </div>
    </div>
  );
}
