import { styles } from "./SummaryPanel.styles";

export default function RetailSummaryPanel({
    isSearchMode,
    normalStartLabel,
    normalEndLabel,
    normalChangeRate,
    normalStart,
    normalEnd,
    selectedMonth,
    selectedMonthRows,
    page,
}) {
    return (
        <div className={styles.wrapper}>
            <h3 className={styles.title}>상세 정보 요약</h3>

            {isSearchMode ? (
                <div className={styles.contentStack}>
                    <div className={styles.card}>
                        <p className={styles.label}>평년 기준 기간</p>
                        <p className={styles.mainText}>
                            {normalStartLabel} ~ {normalEndLabel}
                        </p>
                    </div>

                    <div className={styles.card}>
                        <p className={styles.label}>평년 예상 가격 변화</p>
                        <p className={styles.changeRate(normalChangeRate)}>
                            {normalChangeRate === null
                                ? "-"
                                : `${normalChangeRate >= 0 ? "+" : ""}${normalChangeRate.toFixed(1)}%`}
                        </p>
                        {normalStart && normalEnd && (
                            <p className={styles.subText}>
                                {normalStart.priceNum.toLocaleString()}원 → {normalEnd.priceNum.toLocaleString()}원
                            </p>
                        )}
                    </div>

                    <div className={styles.card}>
                        <p className={styles.label}>현재 선택 월</p>
                        <p className={styles.mainText}>
                            {selectedMonth ? `${selectedMonth.slice(0, 4)}.${selectedMonth.slice(4, 6)}` : "-"}
                        </p>
                        <p className={styles.subText}>
                            {selectedMonthRows.length}건 / {page + 1}페이지
                        </p>
                    </div>
                </div>
            ) : (
                <div className={styles.emptyWrapper}>
                    조회 후 평년 요약 정보가 <br /> 여기에 표시됩니다.
                </div>
            )}
        </div>
    );
}