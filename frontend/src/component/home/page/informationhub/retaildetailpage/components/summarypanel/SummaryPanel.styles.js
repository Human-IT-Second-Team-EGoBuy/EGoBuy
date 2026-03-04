// RetailSummaryPanel.styles.js
export const styles = {
    wrapper: "p-6 h-full border border-slate-200 bg-white",
    title: "text-lg font-bold text-slate-800 mb-4",
    
    // 리스트 컨테이너
    contentStack: "space-y-4 text-sm",
    
    // 개별 카드 스타일
    card: "rounded-xl bg-white border border-slate-200 p-4",
    label: "text-slate-500 mb-1",
    mainText: "font-bold text-slate-800",
    subText: "text-xs text-slate-500 mt-1",
    
    // 변화율 텍스트 (상태에 따른 색상 변경)
    changeRate: (rate) => `
        font-bold text-lg 
        ${rate >= 0 ? "text-rose-600" : "text-blue-600"}
    `,
    
    // 빈 상태 (조회 전)
    emptyWrapper: "text-slate-500 text-sm text-center mt-20",
};