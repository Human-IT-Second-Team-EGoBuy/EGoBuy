import CustomSelect from "../../../../main/customselect/CustomSelect";
import { styles } from "./FilterPanel.styles";

export default function RetailFilterPanel({
    selected,
    sggList,
    categoryList,
    itemList,
    varietyList,
    filter,
    setFilter,
    setSelected,
    handleCategoryChange,
    handleItemChange,
    handleGetSearchData,
    onReset,
}) {
    return (
        <div className={styles.wrapper}>
            <div className={styles.gridContainer}>
                <div>
                    <label className={styles.label}>지역명</label>
                    <CustomSelect
                        label="지역 선택"
                        value={selected.sggNm}
                        option={sggList}
                        onChange={(e) => setSelected((prev) => ({ ...prev, sggNm: e }))}
                    />
                </div>

                <div>
                    <label className={styles.label}>부류</label>
                    <CustomSelect
                        label="부류 선택"
                        value={selected.category}
                        option={categoryList}
                        onChange={(e) => handleCategoryChange({ target: { value: e } })}
                    />
                </div>

                <div>
                    <label className={styles.label}>품목</label>
                    <CustomSelect
                        label="품목 선택"
                        value={selected.item}
                        option={itemList}
                        onChange={(e) => handleItemChange({ target: { value: e } })}
                        disabled={!selected.category}
                    />
                </div>

                <div>
                    <label className={styles.label}>품종</label>
                    <CustomSelect
                        label="품종 선택"
                        value={selected.variety}
                        option={varietyList}
                        onChange={(e) => setSelected((prev) => ({ ...prev, variety: e }))}
                        disabled={!selected.item}
                    />
                </div>
            </div>

            <div className={styles.bottomPanel}>
                <div className={styles.filterGroup}>
                    <span className={styles.filterLabel}>가격 구분</span>
                    <div className={styles.toggleContainer}>
                        {["도매가", "소매가"].map((type) => (
                            <button
                                key={type}
                                onClick={() => setFilter(type)}
                                className={styles.toggleButton(filter === type)}
                            >
                                {type}
                            </button>
                        ))}
                    </div>
                </div>

                <div className={styles.buttonGroup}>
                    <button
                        className={styles.resetBtn}
                        onClick={onReset}
                    >
                        초기화
                    </button>
                    <button
                        className={styles.searchBtn}
                        onClick={handleGetSearchData}
                    >
                        조회하기
                    </button>
                </div>
            </div>
        </div>
    );
}