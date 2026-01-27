// 카드용도 컨테이너
export function Card({ children, className = "" }) {
  return (
    <div
      className={[
        "bg-white rounded-3xl p-6 border border-slate-200 shadow-sm text-emerald-600",
        className,
      ].join(" ")}
    >
      {children}
    </div>
  );
}

// 날씨 ui 컨테이너
export function WeatherUi({ children, className = "" }) {
  return (
    <section
      className={[
        "bg-gradient-to-br from-emerald-500 to-teal-600 rounded-[2rem] p-8 text-white shadow-xl relative overflow-hidden min-h-[260px]",
        className,
      ].join(" ")}
    >
      {children}
    </section>
  );
}

// 제목 컨테이너
export function SectionText({ title, right = null, className = "" }) {
  return (
    <div className={["flex justify-between items-center", className].join(" ")}>
      <h3 className="font-bold text-lg text-emerald-600">{title}</h3>
      {right}
    </div>
  );
}
