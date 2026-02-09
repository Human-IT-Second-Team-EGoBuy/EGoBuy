export default function HeaderUi({
  children,
  variant = "ghost",
  className = "",
  ...props
}) {
    // 버튼의 배경색, 간격 스타일
  const base =
    "bg-white text-sm font-medium px-4 py-2 focus:outline-none";

    // 버튼의 용도에 따른 스타일
  const variants = {
    // 인증 버튼
    ghost:
      "bg-white text-slate-700 border border-slate-200 hover:bg-slate-100",
      
    // 카테고리 버튼
    text:
      "bg-white text-slate-700 hover:text-emerald-600",
  };

  return (
    <button
      className={[base, variants[variant], className].join(" ")}
      {...props}
    >
      {children}
    </button>
  );
}
