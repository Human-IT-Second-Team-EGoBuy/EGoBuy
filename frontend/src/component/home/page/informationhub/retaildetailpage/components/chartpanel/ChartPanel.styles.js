// RetailChartPanel.styles.js
export const styles = {
    wrapper: "p-6 h-full",
    title: "text-lg font-bold text-slate-800 mb-4",
    
    // 월 선택 버튼 영역
    tabContainer: "mb-4 flex flex-wrap gap-2",
    tabButton: (isActive) => `
        px-3 py-1.5 rounded-lg text-sm font-bold border transition
        ${isActive 
            ? "bg-blue-600 text-white border-blue-600" 
            : "bg-white text-slate-600 border-slate-200 hover:bg-slate-50"}
    `,

    // 차트 메인 박스
    chartBox: "h-[400px] w-full p-4 border border-slate-100 rounded-3xl bg-white shadow-inner",
    emptyState: "h-full flex items-center justify-center text-slate-500",

    // 하단 페이지네이션 영역
    paginationWrapper: "mt-4 flex items-center justify-between",
    pageInfo: "text-sm text-slate-500",
    pageRange: "ml-2 text-slate-400",
    
    btnGroup: "flex gap-2",
    navBtn: "px-3 py-2 rounded-lg border border-slate-200 text-sm font-bold disabled:opacity-40 hover:bg-slate-50 transition",
    
    // 차트 커스텀 스타일 (Recharts 내부에서 사용될 값들)
    chartConfig: {
        gridStroke: "#f1f5f9",
        axisTick: { fill: "#64748b", fontSize: 13 },
        barColor: "#3b82f6",
        labelStyle: { fontSize: 11, fill: "#475569", fontWeight: 600 },
        tooltipStyle: {
            borderRadius: "16px",
            border: "none",
            boxShadow: "0 10px 15px -3px rgba(0,0,0,0.1)",
        }
    }
};