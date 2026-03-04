import {
    BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, LabelList
} from "recharts";
import { styles } from "./ChartPanel.styles";

export default function RetailChartPanel({
    isSearchMode,
    monthKeys,
    selectedMonth,
    setSelectedMonth,
    setPage,
    selectedMonthChartData,
    avgData,
    safeChartData,
    yMin,
    yMax,
    page,
    totalPages,
    startIndex,
    endIndex,
}) {
    return (
        <div className={styles.wrapper}>
            <h3 className={styles.title}>가격 추이 그래프</h3>

            {isSearchMode && monthKeys.length > 0 && (
                <div className={styles.tabContainer}>
                    {monthKeys.map((monthKey) => (
                        <button
                            key={monthKey}
                            onClick={() => {
                                setSelectedMonth(monthKey);
                                setPage(0);
                            }}
                            className={styles.tabButton(selectedMonth === monthKey)}
                        >
                            {monthKey.slice(0, 4)}.{monthKey.slice(4, 6)}
                        </button>
                    ))}
                </div>
            )}

            <div className={styles.chartBox}>
                {(isSearchMode ? selectedMonthChartData.length > 0 : avgData.length > 0) ? (
                    <ResponsiveContainer width="100%" height="100%">
                        <BarChart data={safeChartData}>
                            <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />

                            <XAxis
                                dataKey="name"
                                axisLine={false}
                                tickLine={false}
                                tick={{ fill: "#64748b", fontSize: 13 }}
                                tickFormatter={(value) => {
                                    const str = String(value);
                                    if (str.includes("-")) {
                                        const [year, month] = str.split("-");
                                        if (month && month.length === 2) return `${year}.${month}`;
                                    }
                                    if (str.includes("/")) return str;
                                    return str;
                                }}
                            />

                            <YAxis
                                domain={[yMin, yMax]}
                                axisLine={false}
                                tickLine={false}
                                tick={{ fill: "#64748b", fontSize: 13 }}
                                tickFormatter={(value) => `${Number(value).toLocaleString()}원`}
                            />

                            <Tooltip
                                cursor={{ fill: "#f8fafc" }}
                                contentStyle={{
                                    borderRadius: "16px",
                                    border: "none",
                                    boxShadow: "0 10px 15px -3px rgba(0,0,0,0.1)",
                                }}
                                labelFormatter={(label) => (isSearchMode ? `조사일: ${label}` : `월: ${label}`)}
                                formatter={(value) => [`${Number(value).toLocaleString()}원`, "가격"]}
                            />

                            <Bar dataKey="price" fill="#3b82f6" radius={[8, 8, 0, 0]} barSize={40}>
                                <LabelList
                                    dataKey="price"
                                    position="top"
                                    formatter={(value) => `${Number(value).toLocaleString()}원`}
                                    style={{ fontSize: 11, fill: "#475569", fontWeight: 600 }}
                                />
                            </Bar>
                        </BarChart>
                    </ResponsiveContainer>
                ) : (
                    <div className={styles.emptyState}>데이터 분석중...</div>
                )}
            </div>

            {isSearchMode && selectedMonthChartData.length > 0 && (
                <div className={styles.paginationWrapper}>
                    <div className={styles.pageInfo}>
                        {selectedMonth.slice(0, 4)}.{selectedMonth.slice(4, 6)} / {page + 1} / {totalPages}
                        <span className={styles.pageRange}>
                            ({startIndex + 1}-{Math.min(endIndex, selectedMonthChartData.length)} / {selectedMonthChartData.length})
                        </span>
                    </div>

                    <div className={styles.btnGroup}>
                        <button
                            onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
                            disabled={page === 0}
                            className={styles.navBtn}
                        >
                            이전
                        </button>

                        <button
                            onClick={() => setPage((prev) => Math.min(prev + 1, totalPages - 1))}
                            disabled={page >= totalPages - 1}
                            className={styles.navBtn}
                        >
                            다음
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}