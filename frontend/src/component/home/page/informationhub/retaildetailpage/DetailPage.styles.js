// RetailDetailPage.styles.js
export const styles = {
    // 페이지 전체 컨테이너
    container: "p-6 max-w-7xl mx-auto space-y-8",
    
    // 헤더 섹션
    headerWrapper: "mb-8 border-b border-slate-100 pb-6",
    title: "text-2xl font-bold text-slate-800",
    description: "text-slate-500 text-sm mt-1",
    
    // 메인 레이아웃 그리드 (7:3 비율)
    mainGrid: "grid grid-cols-1 lg:grid-cols-10 gap-6",
    chartColumn: "lg:col-span-7",
    summaryColumn: "lg:col-span-3",
    
    // 공통 카드 래퍼 (내부 패딩 등)
    filterCard: "p-8",
    contentCard: "p-6 h-full",
    
    // 요약 패널 전용 카드 스타일 (배경색 등 커스텀이 필요한 경우)
    summaryCard: "p-6 h-full border-slate-200 bg-white shadow-sm", 
};