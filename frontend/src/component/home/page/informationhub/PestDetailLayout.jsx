// src/component/home/page/informationHub/PestDetailLayout.jsx
import { useNavigate } from "react-router-dom";


export default function PestDetailLayout({ title, subtitle, badge, sections, state }) {
  const nav = useNavigate();

  // state: "loading" | "ok" | "nf" | "error"
  const isEmpty = state !== "ok";

  return (
    <div className="pd2-wrap select-none">
      <div className="pd2-container">
        <div className="pd2-shell">
          {/* Header */}
          <div className="pd2-header">
            <button className="pd2-back" onClick={() => nav(-1)} aria-label="back">
              ← 목록
            </button>

            <div className="pd2-titleRow">
              <div className="pd2-titleGroup">
                <h1 className="pd2-title">{title}</h1>
                {badge && <div className="pd2-badgeWrap">{badge}</div>}
              </div>

              {subtitle && <div className="pd-sub whitespace-pre-line">{subtitle}</div>}
            </div>
          </div>

          {/* Body */}
          <div className="pd2-body">
            {isEmpty ? (
              <div className="pd2-state">
                {state === "loading" && "불러오는 중…"}
                {state === "nf" && "데이터를 찾을 수 없어요."}
                {state === "error" && "일시적인 오류가 발생했어요."}
              </div>
            ) : (
              <div className="pd2-grid">
                {sections.map((s) => (
                  <section key={s.key} className="pd2-card">
                    <div className="pd2-cardHead">
                      <div className="pd2-dot" />
                      <div className="pd2-cardTitle">{s.label}</div>
                    </div>

                    <div className="pd2-cardBody">
                      {s.value ? s.value : <span className="pd2-empty">내용 없음</span>}
                    </div>
                  </section>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
