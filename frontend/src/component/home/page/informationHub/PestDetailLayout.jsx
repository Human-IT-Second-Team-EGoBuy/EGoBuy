// src/component/home/page/informationHub/PestDetailLayout.jsx
import { useNavigate } from "react-router-dom";
import "./pestDetail.css";

export default function PestDetailLayout({ title, subtitle, badge, sections, state }) {
  const nav = useNavigate();

  // state: "loading" | "ok" | "nf" | "error"
  const isEmpty = state !== "ok";

  return (
    <div className="pd-wrap">
      <div className="pd-container">
        <div className="pd-card">
          <div className="pd-head">
            <button className="pd-back" onClick={() => nav(-1)}>
              ← 목록
            </button>

            <div className="pd-title-row">
              <h1 className="pd-title">{title}</h1>
              {badge}
            </div>

            {subtitle && <div className="pd-sub">{subtitle}</div>}
          </div>

          <div className="pd-divider" />

          <div className="pd-body">
            {isEmpty ? (
              <div className="pd-state">
                {state === "loading" && "불러오는 중…"}
                {state === "nf" && "데이터를 찾을 수 없어요."}
                {state === "error" && "일시적인 오류가 발생했어요."}
              </div>
            ) : (
              sections.map((s) => (
                <div key={s.key} className="pd-section">
                  <div className="pd-section-title">{s.label}</div>
                  <div className="pd-section-body">
                    {s.value ? s.value : <span className="pd-empty">내용 없음</span>}
                  </div>
                </div>
              ))
            )}
          </div>

          <div className="pd-spacer" />
        </div>
      </div>
    </div>
  );
}
