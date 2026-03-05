import { useState, useEffect } from "react";
import { Card } from "../MainPageUi";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { useNavigate } from "react-router-dom";
import CustomSelect from "../customselect/CustomSelect";
import axios from "axios";

export default function RetailSection() {
    const [avgData, setAvgData] = useState([]);
    const [categoryList, setCategoryList] = useState([]);
    const [itemList, setItemList] = useState([]);
    const [varietyList, setVarietyList] = useState([]);
    const [selected, setSelected] = useState({
        category: "식량작물",
        item: "쌀",
        variety: ""
    })
    const [filter, setFilter] = useState("소매가");
    const navigate = useNavigate();

    const mainChartData = avgData.slice(-4);

    useEffect(() => {
        const initLoad = async () => {
            try {
                const res = await axios.get("/api/retail/open");
                if (res.data.success) setCategoryList(res.data.content || []);
                handleGetRetailAvgData(); // 초기 데이터 로드 (쌀)
            } catch (err) { console.log("초기 로드 실패", err); }
        };
        initLoad();
    }, []);

    const handleGetRetailAvgData = async () => {
        try {
            const today = new Date().toISOString().split('T')[0].replace(/-/g, '');
            const cropName = selected.variety || selected.item;
            const res = await axios.get("/api/retail/avg", {
                params: { toDate: today, cropNm: cropName, filter }
            });
            if (res.data?.content) setAvgData(res.data.content);
        } catch (err) { console.error("가격 데이터 실패", err); }
    };

    const handleCategoryChange = async (e) => {
        const ctgryNm = e.target.value;
        setSelected({ category: ctgryNm, item: "", variety: "" });
        const res = await axios.get("/api/retail/item-open", { params: { ctgryNm } });
        setItemList(res.data.content || []);
    };

    const handleItemChange = async (e) => {
        const itemNm = e.target.value;
        setSelected(prev => ({ ...prev, item: itemNm, variety: "" }));
        const res = await axios.get("/api/retail/variety-open", { params: { itemNm } });
        setVarietyList(res.data.content || []);
    };

    const handleMoreClick = () => {
        if (!selected.item) return alert("품목을 선택해주세요.");
        const params = new URLSearchParams({ ...selected, filter }).toString();
        navigate(`/retail-detail-info?${params}`);
    };

    return (
        <Card className="price-card">
            {/* 헤더 부분 */}
            <div className="price-card-header">
                <h2 className="font-bold text-lg">농산물 가격 추이 검색</h2>
                <button onClick={handleMoreClick} className="btn-more">더보기 +</button>
            </div>

            {/* 선택 정보 표시 (배지) */}
            <div className="badge-container">
                <span className="status-badge badge-item">📍 {selected.item || "쌀"}</span>
                <span className={`status-badge ${filter === "소매가" ? "badge-retail" : "badge-wholesale"}`}>
                    💰 {filter}
                </span>
            </div>

            <div className="space-y-3 mb-6">
                {/* 필터 탭 */}
                <div className="filter-tab-container">
                    {["소매가", "도매가"].map((type) => (
                        <button
                            key={type}
                            onClick={() => setFilter(type)}
                            className={`filter-tab-btn ${filter === type ? "filter-tab-btn-active" : "filter-tab-btn-inactive"}`}
                        >
                            {type}
                        </button>
                    ))}
                </div>

                {/* 커스텀 셀렉트 그룹 */}
                <div className="select-gorup">
                    <CustomSelect
                        label="부류 선택"
                        value={selected.category}
                        option={categoryList}
                        onChange={(val) => handleCategoryChange({ target: { value: val } })}
                    />
                    <CustomSelect
                        label="품목 선택"
                        value={selected.item}
                        option={itemList}
                        onChange={(val) => handleItemChange({ target: { value: val } })}
                        disabled={!selected.category}
                    />
                    <CustomSelect
                        label="품종 선택"
                        value={selected.variety}
                        option={varietyList}
                        onChange={(val) => setSelected(prev => ({ ...prev, variety: val }))}
                        disabled={!selected.item}
                    />
                </div>

                {/* 조회 버튼 */}
                <button onClick={handleGetRetailAvgData} className="btn-search-main">
                    가격 추이 조회
                </button>
            </div>

            {/* 차트 영역 */}
            <div className="chart-wrapper">
                {avgData.length > 0 ? (
                    <ResponsiveContainer width="100%" height="100%">
                        <BarChart data={mainChartData}>
                            <CartesianGrid strokeDasharray="3 3" vertical={false} />
                            <XAxis dataKey="month" tick={{ fontSize: 12 }} axisLine={false} tickLine={false} />
                            <YAxis hide domain={['dataMin - 500', 'dataMax + 500']} />
                            <Tooltip
                                cursor={{ fill: '#f8fafc' }}
                                content={({ active, payload, label }) => (
                                    active && payload && payload.length && (
                                        <div className="tootip-div">
                                            <p className="chart-text-1">{label} 평균가</p>
                                            <p className="chart-text-2">{payload[0].value.toLocaleString()}원</p>
                                        </div>
                                    )
                                )}
                            />
                            <Bar dataKey="avgPrice" fill="#3b82f6" radius={[6, 6, 0, 0]} barSize={30} />
                        </BarChart>
                    </ResponsiveContainer>
                ) : (
                    <div className="chart-empty-msg">데이터를 분석 중입니다...</div>
                )}
            </div>
        </Card>
    );
}