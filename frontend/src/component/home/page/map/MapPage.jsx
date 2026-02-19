import React, { useState, useEffect } from "react";
import { Map, Polygon } from "react-kakao-maps-sdk";
import axios from "axios";
import "./MapPage.css";

export default function MapPage() {
  const [center, setCenter] = useState({ lat: 37.566826, lng: 126.9786567 });
  const [level, setLevel] = useState(10);

  const [panelOpen, setPanelOpen] = useState(false);

  const [tradeInfo, setTradeInfo] = useState(null); // 토지매매 응답 저장
  const [soilInfo, setSoilInfo] = useState(null);   // 토양정보 응답 저장


  const [boundaryPath, setBoundaryPath] = useState([]);

  const [filter, setFilter] = useState("trade");
  const [path, setPath] = useState("landprice-search");

  const [sidoList, setSidoList] = useState([]);
  const [sggList, setSggList] = useState([]);
  const [umdList, setUmdList] = useState([]);
  const [riList, setRiList] = useState([]);

  const [selected, setSelected] = useState({ sido: "", sgg: "", umd: "", ri: "" });

  // 1) 시도 로드: 최초 1회만
  useEffect(() => {
    axios
      .get("/api/map/sido-open")
      .then((res) => {
        if (res.data && res.data.success) {
          setSidoList(res.data.content || []);
        } else {
          console.warn("sido-open 응답이 success가 아님:", res.data);
          setSidoList([]);
        }
      })
      .catch((err) => console.error("Sido 로드 실패:", err));
  }, []);

  const handleFilterChange = (type) => {
    setFilter(type);
    setPath(type === "trade" ? "landprice-search" : "landinfo-search");
    setBoundaryPath([]);
    setPanelOpen(false);
  };


  // 2) 지역 선택 핸들러 (parentRegionCd 키 사용)
  const handleSidoChange = (e) => {
    const val = e.target.value;
    setSelected({ sido: val, sgg: "", umd: "", ri: "" });
    setSggList([]);
    setUmdList([]);
    setRiList([]);

    if (val) {
      axios
        .get(`/api/map/sgg-open?parentRegionCd=${val}`)
        .then((res) => setSggList(res.data.content || []))
        .catch((err) => console.error("sgg-open 로드 실패:", err));
    }
  };

  const handleSggChange = (e) => {
    const val = e.target.value;
    setSelected((prev) => ({ ...prev, sgg: val, umd: "", ri: "" }));
    setUmdList([]);
    setRiList([]);

    if (val) {
      axios
        .get(`/api/map/umd-open?parentRegionCd=${val}`)
        .then((res) => setUmdList(res.data.content || []))
        .catch((err) => console.error("umd-open 로드 실패:", err));
    }
  };

  const handleUmdChange = (e) => {
    const val = e.target.value;
    setSelected((prev) => ({ ...prev, umd: val, ri: "" }));
    setRiList([]);

    if (val) {
      axios
        .get(`/api/map/ri-open?parentRegionCd=${val}`)
        .then((res) => setRiList(res.data.content || []))
        .catch((err) => console.error("ri-open 로드 실패:", err));
    }
  };

  const handleRiChange = (e) => {
    const val = e.target.value;
    setSelected((prev) => ({ ...prev, ri: val }));
  };

  // boundaryPath가 실제로 변경되었는지 확인(디버깅용)
  useEffect(() => {
    console.log("boundaryPath state 변경됨. 길이:", boundaryPath.length);
    if (boundaryPath.length > 0) {
      console.log("boundaryPath 첫값:", boundaryPath[0], "lat 타입:", typeof boundaryPath[0].lat);
    }
  }, [boundaryPath]);

  // 검색 버튼 클릭 시 실행 (리 이름 조건부 포함)
  const handleSearch = () => {
    const sidoName = sidoList.find((i) => i.regionCd === selected.sido)?.name || "";
    const sggName = sggList.find((i) => i.regionCd === selected.sgg)?.name || "";
    const umdName = umdList.find((i) => i.regionCd === selected.umd)?.name || "";
    const riName = riList.find((i) => i.regionCd === selected.ri)?.name || "";

    const addressParts = [sidoName, sggName, umdName];
    if (riName) addressParts.push(riName);

    const fullAddress = addressParts.filter((part) => part !== "").join(" ").trim();

    if (!fullAddress) {
      alert("지역을 선택해주세요.");
      return;
    }

    // 1) 지도 중심 이동
    const geocoder = new window.kakao.maps.services.Geocoder();
    geocoder.addressSearch(fullAddress, (result, status) => {
      if (status === window.kakao.maps.services.Status.OK) {
        setCenter({ lat: parseFloat(result[0].y), lng: parseFloat(result[0].x) });
        setLevel(5);
      } else {
        console.warn("geocoder 실패:", status, fullAddress);
      }
    });

    // 2) 경계 좌표 조회
    axios
      .get(`/api/map/${path}`, { params: { locatadd_nm: fullAddress, hasRi: Boolean(selected.ri), } })
      .then((res) => {
        const response = res.data?.content ? res.data.content : res.data;

        // 1) 경계 좌표 파싱
        const formatRes = Array.isArray(response?.boundary)
          ? response.boundary
            .map((pos) => {
              const lat = Number(pos.lat);
              const lng = Number(pos.lng);
              if (!Number.isFinite(lat) || !Number.isFinite(lng)) return null;
              return { lat, lng };
            })
            .filter(Boolean)
          : [];

        if (formatRes.length >= 3) setBoundaryPath(formatRes);
        else setBoundaryPath([]);

        // 2) 검색 결과(토지매매 or 토양정보) 저장
        if (filter === "trade") {
          setTradeInfo({
            address: response?.centerAddress || fullAddress,
            // tradeList 배열 전체를 저장 (없으면 빈 배열)
            list: response?.tradeList || []
          });
          setSoilInfo(null);
          // ... handleSearch 내부 .then(res => { ... })
        } else {
          const data = response;

          // 데이터가 실질적으로 있는지 확인 (예: ph 정보가 있거나 regionCd가 있는지)
          const hasData = data.regionCd && data.examPhInfo;

          setSoilInfo({
            address: data.centerAddress || fullAddress,
            regionCd: data.regionCd || null,
            ph: data.examPhInfo || null,
            om: data.examOmInfo || null,
            ap: data.examApInfo || null,
            kal: data.examKalInfo || null,
            cal: data.examCalInfo || null,
            mg: data.examMgInfo || null,
            sal: data.examSalInfo || null,
            isMissing: !hasData // 데이터가 없는 경우를 표시하는 플래그 추가
          });
          setTradeInfo(null);
        }

        setPanelOpen(true);
      })
      .catch((err) => console.error("검색 요청 실패:", err));

  };

  return (
    <div className="map-container">
      <div className="selection-panel">
        {/* 이 group 안에 제목과 select들을 모아서 관리합니다 */}
        <div className="select-group">
          <h3>📍 지역 상세 선택</h3>

          <select className="custom-select" value={selected.sido} onChange={handleSidoChange}>
            <option value="">시/도 선택</option>
            {sidoList.map((item) => (
              <option key={item.regionCd} value={item.regionCd}>{item.name}</option>
            ))}
          </select>

          <select className="custom-select" value={selected.sgg} onChange={handleSggChange} disabled={!selected.sido}>
            <option value="">시/군/구 선택</option>
            {sggList.map((item) => (
              <option key={item.regionCd} value={item.regionCd}>{item.name}</option>
            ))}
          </select>

          <select className="custom-select" value={selected.umd} onChange={handleUmdChange} disabled={!selected.sgg}>
            <option value="">읍/면/동 선택</option>
            {umdList.map((item) => (
              <option key={item.regionCd} value={item.regionCd}>{item.name}</option>
            ))}
          </select>

          <select className="custom-select" value={selected.ri} onChange={handleRiChange} disabled={!selected.umd}>
            <option value="">리 선택 (전체)</option>
            {riList.map((item) => (
              <option key={item.regionCd} value={item.regionCd}>{item.name}</option>
            ))}
          </select>

          <button onClick={handleSearch} className="search-button">
            검색하기
          </button>
        </div>
      </div>

      <div className="filter-container">
        <button
          onClick={() => handleFilterChange("trade")}
          className={`filter-btn ${filter === "trade" ? "active" : "inactive"}`}
        >
          토지매매
        </button>

        <button
          onClick={() => handleFilterChange("soil")}
          className={`filter-btn ${filter === "soil" ? "active" : "inactive"}`}
        >
          토양정보
        </button>
      </div>

      <Map center={center} className="map-layout" level={level}>
        {boundaryPath.length >= 3 && (
          <Polygon
            path={boundaryPath}
            strokeWeight={3}
            strokeColor={"#FF0000"}
            strokeOpacity={0.8}
            strokeStyle={"solid"}
            fillColor={"#FF0000"}
            fillOpacity={0.2}
          />
        )}
      </Map>

      {panelOpen && (
        <div className="info-panel">
          <button className="close-btn" onClick={() => setPanelOpen(false)}>
            ✕
          </button>

          {filter === "trade" && tradeInfo && (
            <>
              <span className="badge">토지 매매 정보 ({tradeInfo.list.length}건)</span>
              <h2>{tradeInfo.address}</h2>

              {/* 리스트가 많을 경우를 대비한 스크롤 영역 */}
              <div className="trade-list-container">
                {tradeInfo.list.length > 0 ? (
                  tradeInfo.list.map((item) => (
                    <div key={item.id} className="trade-item-card">
                      <div className="trade-item-header">
                        <span className="trade-date">{item.dealYear}.{item.dealMonth}.{item.dealDay}</span>
                        <span className="trade-gbn">{item.dealingGbn}</span>
                      </div>

                      <div className="trade-item-body">
                        <div className="trade-loc">
                          <strong>{item.umdNm} {item.jibun}</strong>
                          <p>{item.landUse} | {item.jimok}</p>
                        </div>

                        <div className="trade-price-info">
                          <div className="price-amount">
                            ₩ {item.dealAmount.toLocaleString()} 만원
                          </div>
                          <div className="price-area">
                            {item.dealArea} ㎡
                          </div>
                        </div>
                      </div>

                      <div className="price-calc">
                        {/* 평당 단가 계산 (단위: 만원/㎡) */}
                        약 {(item.dealAmount / item.dealArea).toFixed(1)} 만원/㎡
                      </div>
                    </div>
                  ))
                ) : (
                  <p className="no-data">최근 거래 내역이 없습니다.</p>
                )}
              </div>
            </>
          )}

          {filter === "soil" && soilInfo && (
            <div className="soil-info-container">
              <span className="badge soil-badge">토양 환경 분석 정보</span>
              <h2 className="info-title">{soilInfo.address}</h2>

              {soilInfo.isMissing ? (
                /* 데이터가 없을 때 보여줄 화면 */
                <div className="no-soil-data">
                  <div className="warning-icon">⚠️</div>
                  <p className="main-msg">해당 장소는 아직 조사를 완료하지 못했습니다.</p>
                  <p className="sub-msg">공공데이터 포털에 등록된 토양 검정 정보가 없습니다.</p>
                </div>
              ) : (
                /* 데이터가 있을 때 보여줄 화면 */
                <>
                  <p className="region-code">법정동코드: {soilInfo.regionCd}</p>
                  <div className="soil-grid">
                    <SoilItem label="산도 (pH)" value={soilInfo.ph} unit="" />
                    <SoilItem label="유기물 (OM)" value={soilInfo.om} unit="g/kg" />
                    <SoilItem label="유효인산 (P₂O₅)" value={soilInfo.ap} unit="mg/kg" />
                    <SoilItem label="치환성 칼륨 (K)" value={soilInfo.kal} unit="cmolc/kg" />
                    <SoilItem label="치환성 칼슘 (Ca)" value={soilInfo.cal} unit="cmolc/kg" />
                    <SoilItem label="치환성 마그네슘 (Mg)" value={soilInfo.mg} unit="cmolc/kg" />
                    <SoilItem label="전기전도도 (EC)" value={soilInfo.sal} unit="dS/m" />
                  </div>
                  <div className="soil-footer">
                    * 위 정보는 최근 분석된 필지의 대표 데이터입니다.
                  </div>
                </>
              )}
            </div>
          )}
        </div>
      )}

    </div>
  );
}
// 컴포넌트 내부 혹은 외부에 선언할 작은 컴포넌트
function SoilItem({ label, value, unit }) {
  return (
    <div className="soil-card">
      <span className="soil-label">{label}</span>
      <div className="soil-value-group">
        <span className="soil-value">{value || "-"}</span>
        <span className="soil-unit">{unit}</span>
      </div>
    </div>
  );
}
