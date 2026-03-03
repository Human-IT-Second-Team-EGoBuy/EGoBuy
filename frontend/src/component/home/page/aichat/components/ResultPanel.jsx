// 0~1 확률로 강제(clamp) (NaN/undefined 방지 + 0~100 들어와도 대응)
const clamp01 = (n) => {
  const v = Number(n);
  if (!Number.isFinite(v)) return 0;

  // 혹시 0~100으로 들어오면 0~1로 보정
  const normalized = v > 1 ? v / 100 : v;

  return Math.max(0, Math.min(1, normalized));
};

const toPercent = (n) => `${Math.round(clamp01(n) * 100)}%`;
const toWidth = (n) => Math.round(clamp01(n) * 100);

// top1 키 호환( name/conf  또는 label/prob  또는 best 구조)
const pickTop1 = (result) => {
  const t = result?.top1 ?? result?.best ?? null;

  const name =
    t?.name ??
    t?.labelKo ??
    t?.label_ko ??
    t?.label ??
    "결과 없음";

  const conf = clamp01(
    t?.conf ??
      t?.prob ??
      t?.prob_global ??
      0
  );

  return { name, conf };
};

export default function ResultPanel({ crop, result }) {
  const top1 = pickTop1(result);
  const advice = Array.isArray(result?.advice) ? result.advice : [];

  return (
    <div className="rp-wrap">
      <div className="rp-header">
        <div className="rp-title">진단 결과</div>
        <div className="rp-crop">
          작물: <span className="rp-crop-strong">{crop || "-"}</span>
        </div>
      </div>

      <div className="rp-body">
        <div className="rp-card">
          <div className="rp-label">Top-1</div>
          <div className="rp-name">{top1.name}</div>

          {result?.summary ? <div className="rp-summary">{result.summary}</div> : null}

          <div className="rp-row">
            <div className="rp-row-top">
              <span>Confidence</span>
              <span className="rp-row-value">{toPercent(top1.conf)}</span>
            </div>

            <div className="rp-bar-bg">
              <div className="rp-bar" style={{ width: `${toWidth(top1.conf)}%` }} />
            </div>
          </div>
        </div>

        <div className="rp-tip">
          <div className="rp-tip-title">정확도를 높이는 팁</div>

          {advice.length === 0 ? (
            <div className="rp-tip-empty">추가 안내가 없어요.</div>
          ) : (
            <ul className="rp-tip-list">
              {advice.map((t, idx) => (
                <li key={idx}>{t}</li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </div>
  );
}