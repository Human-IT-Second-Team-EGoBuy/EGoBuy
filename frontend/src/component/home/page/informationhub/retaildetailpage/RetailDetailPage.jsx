import { useState, useEffect } from "react";
import { useSearchParams } from "react-router-dom";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, LabelList } from "recharts";
import CustomSelect from "../../main/customselect/CustomSelect";
import { Card } from "../../main/MainPageUi";
import axios from "axios";
import RetailFilterPanel from "./components/filterpanel/RetailFilterPanel";
import RetailChartPanel from "./components/chartpanel/RetailChartPanel";
import RetailSummaryPanel from "./components/summarypanel/RetailSummaryPanel";
import { styles } from "./DetailPage.styles";

export default function RetailDetailPage() {
    const [searchParams] = useSearchParams();

    const [categoryList, setCategoryList] = useState([]);
    const [itemList, setItemList] = useState([]);
    const [varietyList, setVarietyList] = useState([]);
    const [sggList, setSggList] = useState([]);
    const [avgData, setAvgData] = useState([]);
    const [searchData, setSearchData] = useState({}); // ✅ 월별 grouped object

    const [selected, setSelected] = useState({
        category: searchParams.get("category") || "식량작물",
        item: searchParams.get("item") || "쌀",
        variety: searchParams.get("variety") || "",
        sggNm: "",
    });
    const [normalData, setNormalData] = useState({}); // item2Grouped 저장

    const [filter, setFilter] = useState(searchParams.get("filter") || "소매가");

    // ✅ 월 선택 + 5개씩 페이지네이션
    const [selectedMonth, setSelectedMonth] = useState("");
    const [page, setPage] = useState(0);
    const pageSize = 5;

    // =========================
    // 차트 데이터 계산 (avg / search 공용)
    // =========================
    const isSearchMode = searchData && Object.keys(searchData).length > 0;
    const monthKeys = isSearchMode ? Object.keys(searchData).sort() : [];

    const selectedMonthRows = isSearchMode && selectedMonth ? (searchData[selectedMonth] || []) : [];

    // 검색 모드(일자 데이터) -> 차트용 변환
    const selectedMonthChartData = selectedMonthRows.map((row) => ({
        name: row.regday, // "03/04"
        price: Number(String(row.price).replaceAll(",", "")),
        date: row.date || `${row.yyyy}-${String(row.regday).replace("/", "-")}`,
        marketname: row.marketname || "",
    }));

    const totalPages = Math.max(1, Math.ceil(selectedMonthChartData.length / pageSize));
    const startIndex = page * pageSize;
    const endIndex = startIndex + pageSize;

    // ✅ 실제 차트에 들어갈 데이터 (검색이면 5개 slice / 아니면 avgData 전체)
    const chartData = isSearchMode
        ? selectedMonthChartData.slice(startIndex, endIndex)
        : avgData;

    const safeChartData = Array.isArray(chartData) ? chartData : [];

    const prices = safeChartData
        .map((v) => Number(v?.price))
        .filter((v) => Number.isFinite(v));

    const marketNames = [...new Set(selectedMonthRows.map((row) => row.marketname).filter(Boolean))];

    const normalRows = Object.values(normalData || {}).flat();

    // 날짜순 정렬 + 숫자 가격 변환
    const normalSorted = [...normalRows]
        .map((row) => ({
            ...row,
            parsedDate: new Date(row.date || `${row.yyyy}-${String(row.regday).replace("/", "-")}`),
            priceNum: Number(String(row.price).replaceAll(",", "")),
        }))
        .filter((row) => !Number.isNaN(row.parsedDate.getTime()) && Number.isFinite(row.priceNum))
        .sort((a, b) => a.parsedDate - b.parsedDate);

    // 시작/끝 데이터
    const normalStart = normalSorted[0] || null;
    const normalEnd = normalSorted[normalSorted.length - 1] || null;

    // 증가율 계산
    const normalChangeRate =
        normalStart && normalEnd && normalStart.priceNum > 0
            ? (((normalEnd.priceNum - normalStart.priceNum) / normalStart.priceNum) * 100)
            : null;

    // 월 범위 표시용
    const normalStartLabel = normalStart
        ? `${normalStart.yyyy}.${String(normalStart.regday).split("/")[0]}`
        : "-";

    const normalEndLabel = normalEnd
        ? `${normalEnd.yyyy}.${String(normalEnd.regday).split("/")[0]}`
        : "-";

    const minPrice = prices.length ? Math.min(...prices) : 0;
    const maxPrice = prices.length ? Math.max(...prices) : 10000;
    const padding = 1000;

    const yMin = Math.max(0, minPrice - padding);
    const yMax = maxPrice + padding;

    useEffect(() => {
        const initLoad = async () => {
            await handleGetSggName();
            await handleGetRetailAvgData();
            await fetchCategories();
        };
        initLoad();
    }, []);

    const handleGetSggName = async () => {
        try {
            const res = await axios.get("/api/retail/sgg-open");
            setSggList(res.data?.content || []);
        } catch (err) {
            console.log("지역 로드 실패", err);
        }
    };

    const handleGetRetailAvgData = async () => {
        try {
            const today = new Date().toISOString().split("T")[0].replace(/-/g, "");
            const cropName = selected.variety || selected.item;

            const res = await axios.get("/api/retail/avg", {
                params: { toDate: today, cropNm: cropName, filter },
            });

            const content = Array.isArray(res.data?.content) ? res.data.content : [];

            const mapped = content.map((v) => ({
                name: v.month,     // "2025-03"
                price: v.avgPrice, // 숫자
            }));

            setAvgData(mapped);
        } catch (err) {
            console.log("가격 데이터 실패");
            console.log("message:", err.message);
            console.log("status:", err.response?.status);
            console.log("data:", err.response?.data);
        }
    };

    const handleGetSearchData = async () => {
        const today = new Date().toISOString().split("T")[0].replace(/-/g, "");
        const cropName = selected.variety || selected.item;

        if (!cropName) return alert("검색할 품목이나 품종을 선택해주세요.");

        try {
            const res = await axios.get("/api/retail/search", {
                params: {
                    toDate: today,
                    regionNm: selected.sggNm,
                    cropNm: cropName,
                    filter: filter,
                },
            });

            // ✅ 백에서 월별 그룹핑된 item 객체를 받는 구조
            const groupedItem = res.data?.content?.data?.item || {};
            setSearchData(groupedItem);

            const monthKeysLocal = Object.keys(groupedItem).sort();
            setSelectedMonth(monthKeysLocal[monthKeysLocal.length - 1] || ""); // 최신 월
            setPage(0);

            const groupedNormal = res.data?.content?.data?.item2Grouped || {};
            setSearchData(groupedItem);
            setNormalData(groupedNormal);
        } catch (err) {
            console.log("검색실패", err);
        }
    };

    const fetchCategories = async () => {
        try {
            const res = await axios.get("/api/retail/open");
            setCategoryList(res.data?.content || []);
        } catch (err) {
            console.error("부류 로드 실패", err);
        }
    };

    const handleCategoryChange = async (e) => {
        const ctgryNm = e.target.value;
        setSelected({ category: ctgryNm, item: "", variety: "", sggNm: selected.sggNm });
        setVarietyList([]);

        try {
            const res = await axios.get("/api/retail/item-open", {
                params: { ctgryNm },
            });
            setItemList(res.data?.content || []);
        } catch (err) {
            console.log("품목 로드 실패", err);
        }
    };

    const handleItemChange = async (e) => {
        const itemNm = e.target.value;
        setSelected((prev) => ({ ...prev, item: itemNm, variety: "" }));

        try {
            const res = await axios.get("/api/retail/variety-open", {
                params: { itemNm },
            });
            setVarietyList(res.data?.content || []);
        } catch (err) {
            console.log("품종 로드 실패", err);
        }
    };

    return (
        <div className={styles.container}>
            <div className={styles.headerWrapper}>
                <h1 className={styles.title}>농산물 가격 상세 분석</h1>
                <p className={styles.description}>지역 및 품목별 실시간 가격 추이를 상세하게 조회합니다.</p>
            </div>

            <Card className={styles.filterCard}>
                <RetailFilterPanel
                    selected={selected}
                    sggList={sggList}
                    categoryList={categoryList}
                    itemList={itemList}
                    varietyList={varietyList}
                    filter={filter}
                    setFilter={setFilter}
                    setSelected={setSelected}
                    handleCategoryChange={handleCategoryChange}
                    handleItemChange={handleItemChange}
                    handleGetSearchData={handleGetSearchData}
                    onReset={() => {
                        setSelected({ category: "", item: "", variety: "", sggNm: "" });
                        setSearchData({});
                        setNormalData({});
                        setSelectedMonth("");
                        setPage(0);
                    }}
                />
            </Card>

            <div className={styles.mainGrid}>
                <div className={styles.chartColumn}>
                    <Card className={styles.contentCard}>
                        <RetailChartPanel
                            isSearchMode={isSearchMode}
                            monthKeys={monthKeys}
                            selectedMonth={selectedMonth}
                            setSelectedMonth={setSelectedMonth}
                            setPage={setPage}
                            selectedMonthChartData={selectedMonthChartData}
                            avgData={avgData}
                            safeChartData={safeChartData}
                            yMin={yMin}
                            yMax={yMax}
                            page={page}
                            totalPages={totalPages}
                            startIndex={startIndex}
                            endIndex={endIndex}
                        />
                    </Card>
                </div>

                <div className={styles.summaryColumn}>
                    <Card className={styles.summaryCard}>
                        <RetailSummaryPanel
                            isSearchMode={isSearchMode}
                            normalStartLabel={normalStartLabel}
                            normalEndLabel={normalEndLabel}
                            normalChangeRate={normalChangeRate}
                            normalStart={normalStart}
                            normalEnd={normalEnd}
                            selectedMonth={selectedMonth}
                            selectedMonthRows={selectedMonthRows}
                            page={page}
                        />
                    </Card>
                </div>
            </div>
        </div>
    );
}