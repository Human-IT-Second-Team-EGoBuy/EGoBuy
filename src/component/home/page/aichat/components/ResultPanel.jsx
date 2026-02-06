const percent = (n) => `${Math.round(n * 100)}%`;

export default function ResultPanel({ crop, result }) {
  return (
    <div className="rp-wrap">
      <div className="rp-header">
        <div className="rp-title">진단 결과</div>
        <div className="rp-crop">
          작물: <span className="rp-crop-strong">{crop}</span>
        </div>
      </div>

      <div className="rp-body">
        <div className="rp-card">
          <div className="rp-label">Top-1</div>
          <div className="rp-name">{result.top1.name}</div>

          <div className="rp-row">
            <div className="rp-row-top">
              <span>Confidence</span>
              <span className="rp-row-value">{percent(result.top1.conf)}</span>
            </div>

            <div className="rp-bar-bg">
              <div
                className="rp-bar"
                style={{ width: `${Math.round(result.top1.conf * 100)}%` }}
              />
            </div>
          </div>
        </div>

        <div className="rp-tip">
          <div className="rp-tip-title">정확도를 높이는 팁</div>
          <ul className="rp-tip-list">
            {result.advice.map((t) => (
              <li key={t}>{t}</li>
            ))}
          </ul>
        </div>
      </div>
    </div>
  );
}
