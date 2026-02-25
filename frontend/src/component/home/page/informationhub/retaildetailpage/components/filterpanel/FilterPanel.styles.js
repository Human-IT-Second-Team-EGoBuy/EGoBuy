// RetailFilterPanel.styles.js
export const styles = {
    // 컨테이너 및 레이아웃
    wrapper: "p-8",
    gridContainer: "grid grid-cols-1 md:grid-cols-4 gap-6 mb-8",
    
    // 섹션별 스타일 (필터 하단 영역)
    bottomPanel: "flex flex-col md:flex-row items-center justify-between gap-6 p-6 bg-slate-50 rounded-2xl border border-slate-100",
    
    // 입력 요소 관련
    label: "block text-sm font-bold text-slate-700 mb-3 ml-1",
    
    // 가격 구분 버튼 그룹
    filterGroup: "flex items-center gap-4",
    filterLabel: "text-sm font-bold text-slate-600 mr-2",
    toggleContainer: "flex bg-slate-200 p-1 rounded-xl",
    
    // 버튼 스타일 함수 (상태에 따라 가변적인 클래스 처리)
    toggleButton: (isActive) => `
        px-8 py-2 rounded-lg font-bold text-sm transition-all
        ${isActive ? "bg-white text-emerald-600 shadow-sm" : "text-slate-500 hover:text-slate-700"}
    `,
    
    // 하단 액션 버튼
    buttonGroup: "flex gap-3 w-full md:w-auto items-stretch",
    resetBtn: "flex-1 md:w-32 py-3 rounded-xl border border-slate-300 bg-white text-slate-600 font-bold hover:bg-slate-50 transition whitespace-nowrap",
    searchBtn: "flex-[2] md:px-2 py-3 rounded-xl bg-blue-600 text-white font-bold hover:bg-emerald-700 shadow-lg shadow-emerald-100 transition whitespace-nowrap",
};