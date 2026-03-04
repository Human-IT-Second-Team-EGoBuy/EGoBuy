// 카드용도 컨테이너
export function Card({ children, className = "", ...props }) {
  return (
    <div
      {...props}
      className={`card-base ${className}`}
    >
      {children}
    </div>
  );
}

// 날씨 ui 컨테이너
export function WeatherUi({ children, className = "" }) {
  return (
    <section
      className={`weather-container ${className}`}
    >
      {children}
    </section>
  );
}

// 제목 컨테이너
export function SectionText({ title, right = null, className = "" }) {
  return (
    <div className={`section-text-wrapper ${className}`}>
      <h3 className="section-title">{title}</h3>
      {right}
    </div>
  );
}
