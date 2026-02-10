import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./insectpestsinfo.css";
import { DUMMY_CATEGORIES, DUMMY_CROPS, DUMMY_INSECTS, DUMMY_DISEASES } from "./pestDummy";
// import axios from "axios";

const PAGE_SIZE = 10;

const cropNameMap = new Map(DUMMY_CROPS.map((c) => [c.crop_id, c.crop_name]));

export default function InsectPestsInfoPage() {
  const nav = useNavigate();

  /* 필터 상태 */
  const [categoryId, setCategoryId] = useState("all"); // crop_categories
  const [ptype, setPtype] = useState("all");           // UI 필터: all | insect | disease
  const [q, setQ] = useState("");                      // 검색어 (작물/병해충명)

  /* 데이터 상태 */
  const [categories, setCategories] = useState([]);
  const [rows, setRows] = useState([]);

  /* 페이지 */
  const [page, setPage] = useState(1);

  /* 1) 카테고리 로드 (더미) */
  useEffect(() => {
    // ===== 실제 백엔드 연결 시 (axios 예시) =====
    // (async () => {
    //   const res = await axios.get("/api/information-hub/crop-categories");
    //   // 예: { code, message, data: [...] }
    //   setCategories(res.data.data);
    // })();

    setCategories(DUMMY_CATEGORIES);
  }, []);

  /* 2) categoryId / ptype / q / page 바뀌면 -> 통합목록 로드(axios 자리) */
  useEffect(() => {
    // 지금은 더미 기반이라 API 호출 안 함.

    // ===== 실제 백엔드 연결 시 (axios 예시) =====
    // (async () => {
    //   const res = await axios.get("/api/information-hub/pest-issues", {
    //     params: {
    //       categoryId: categoryId === "all" ? undefined : categoryId,
    //       ptype: ptype === "all" ? undefined : ptype,
    //       q: q.trim() ? q.trim() : undefined, // 검색어
    //       page,
    //       size: PAGE_SIZE,
    //     },
    //   });
    //
    //   // 예: { code, message, data: { items, total, ... } }
    //   // items = [{ pest_type, pest_id, crop_id, pest_name, updated_at }, ...]
    //   const items = res.data?.data?.items ?? [];
    //   setRows(items);
    // })();
  }, [categoryId, ptype, q, page]);

  /* 3) 통합 rows 만들기 (여기서 pest_type을 "프론트에서" 붙임) */
  const mergedAll = useMemo(() => {
    // API로 setRows()가 들어오면, 그걸 그대로 사용
    if (rows && rows.length > 0) return rows;

    // 더미 fallback
    const insects = DUMMY_INSECTS.map((x) => ({
      pest_type: "insect",
      pest_id: x.insect_id,
      crop_id: x.crop_id,
      pest_name: x.pest_name,
      updated_at: x.updated_at,
    }));

    const diseases = DUMMY_DISEASES.map((x) => ({
      pest_type: "disease",
      pest_id: x.disease_id,
      crop_id: x.crop_id,
      pest_name: x.pest_name,
      updated_at: x.updated_at,
    }));

    return [...insects, ...diseases];
  }, [rows]);

  /** 4) 필터 적용(카테고리/유형/검색어) */
  const filtered = useMemo(() => {
    let list = [...mergedAll];

    // (A) categoryId -> 해당 카테고리 crop_id만
    if (categoryId !== "all") {
      const cropIdsInCategory = new Set(
        DUMMY_CROPS
          .filter((c) => String(c.category_id) === String(categoryId))
          .map((c) => c.crop_id)
      );
      list = list.filter((r) => cropIdsInCategory.has(r.crop_id));
    }

    // (B) ptype
    if (ptype !== "all") {
      list = list.filter((r) => r.pest_type === ptype);
    }

    //  (C) 검색어 (작물명 or 병해충명)
    const keyword = q.trim();
    if (keyword) {
      list = list.filter((r) => {
        const cropName = cropNameMap.get(r.crop_id) ?? "";
        const pestName = String(r.pest_name ?? "");
        return cropName.includes(keyword) || pestName.includes(keyword);
      });
    }

    // (D) crop_name순 가나다 정렬
    list.sort((a, b) => {
      const an = cropNameMap.get(a.crop_id) ?? "";
      const bn = cropNameMap.get(b.crop_id) ?? "";

      const c = an.localeCompare(bn, "ko-KR");
      if (c !== 0) return c;

      return (a.pest_name ?? "").localeCompare((b.pest_name ?? ""), "ko-KR");
    });

    return list;
  }, [mergedAll, categoryId, ptype, q]);

  /* 5) 페이징 */
  const total = filtered.length;
  const totalPages = Math.max(1, Math.ceil(total / PAGE_SIZE));
  const pageRows = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  useEffect(() => {
    if (page > totalPages) setPage(totalPages);
  }, [page, totalPages]);

  /* 6) 행 클릭 -> 상세 이동 */
  const onRowClick = (r) => {
    if (r.pest_type === "insect") nav(`/insect-pests-info/insects/${r.pest_id}`);
    else nav(`/insect-pests-info/diseases/${r.pest_id}`);
  };

  /* crop_name 표시용 */
  const cropNameById = useMemo(() => {
    const m = new Map(DUMMY_CROPS.map((c) => [c.crop_id, c.crop_name]));
    return (id) => m.get(id) || "-";
  }, []);

  return (
    <div className="bpi-wrap">
      <div className="bpi-container">
        <div className="bpi-card">
          <div className="bpi-head">
            <h1 className="bpi-title">병해충 정보</h1>
          </div>

          <div className="bpi-divider" />

          <div className="bpi-body">
            {/* 필터 */}
            <div className="bpi-filter-row">
              <SelectLike
                value={categoryId}
                onChange={(v) => {
                  setCategoryId(v);
                  setPage(1);
                }}
                options={[
                  { value: "all", label: "전체 카테고리" },
                  ...categories.map((c) => ({
                    value: String(c.category_id),
                    label: c.category_name,
                  })),
                ]}
              />

              {/*  검색 인풋 (SelectLike 스타일 톤 그대로) */}
              <input
                className="bpi-input"
                value={q}
                onChange={(e) => {
                  setQ(e.target.value);
                  setPage(1);
                }}
                placeholder="작물/병해충명 검색"
              />

              <SelectLike
                value={ptype}
                onChange={(v) => {
                  setPtype(v);
                  setPage(1);
                }}
                className="sm:ml-auto"
                options={[
                  { value: "all", label: "전체" },
                  { value: "insect", label: "해충" },
                  { value: "disease", label: "병" },
                ]}
              />
            </div>

            <div className="bpi-table-wrap">
              <div className="bpi-table-scroll">
                <table className="bpi-table">
                  <thead className="bpi-thead">
                    <tr className="bpi-tr-head">
                      <th className="bpi-th w-[90px]">번호</th>
                      <th className="bpi-th w-[140px]">작물</th>
                      <th className="bpi-th">병해충명</th>
                      <th className="bpi-th w-[120px]">유형</th>
                      <th className="bpi-th w-[140px] hidden sm:table-cell">
                        업데이트
                      </th>
                    </tr>
                  </thead>

                  <tbody className="bpi-tbody">
                    {pageRows.length === 0 ? (
                      <tr>
                        <td colSpan={5} className="bpi-empty">
                          조건에 맞는 데이터가 없어요.
                        </td>
                      </tr>
                    ) : (
                      pageRows.map((r, idx) => (
                        <tr
                          key={`${r.pest_type}-${r.pest_id}`}
                          className="bpi-tr"
                          onClick={() => onRowClick(r)}
                          title="상세 보기"
                        >
                          <td className="bpi-td-muted">
                            {(page - 1) * PAGE_SIZE + idx + 1}
                          </td>
                          <td className="bpi-td">{cropNameById(r.crop_id)}</td>
                          <td className="bpi-td bpi-name">{r.pest_name}</td>
                          <td className="bpi-td">
                            <TypeBadge type={r.pest_type} />
                          </td>
                          <td className="bpi-td-muted hidden sm:table-cell">
                            {String(r.updated_at).slice(0, 10)}
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>

              {/* 페이지네이션 */}
              <div className="bpi-pager">
                <PagerButton
                  disabled={page === 1}
                  onClick={() => setPage((p) => Math.max(1, p - 1))}
                >
                  이전
                </PagerButton>

                {getPageNumbers(page, totalPages, 5).map((n) => (
                  <PagerButton
                    key={n}
                    active={n === page}
                    onClick={() => setPage(n)}
                  >
                    {n}
                  </PagerButton>
                ))}

                <PagerButton
                  disabled={page === totalPages}
                  onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
                >
                  다음
                </PagerButton>
              </div>
            </div>
          </div>

          <div className="bpi-spacer" />
        </div>
      </div>
    </div>
  );
}

function getPageNumbers(current, total, windowSize = 5) {
  const half = Math.floor(windowSize / 2);
  let start = Math.max(1, current - half);
  let end = Math.min(total, start + windowSize - 1);
  start = Math.max(1, end - windowSize + 1);
  return Array.from({ length: end - start + 1 }, (_, i) => start + i);
}

function TypeBadge({ type }) {
  const isInsect = type === "insect";
  return (
    <span className={`bpi-badge ${isInsect ? "bpi-badge-insect" : "bpi-badge-disease"}`}>
      {isInsect ? "해충" : "병"}
    </span>
  );
}

function SelectLike({ value, onChange, options, disabled, className = "" }) {
  return (
    <div className={`bpi-select-wrap ${className}`}>
      <select
        value={value}
        disabled={disabled}
        onChange={(e) => onChange(e.target.value)}
        className={`bpi-select ${disabled ? "bpi-select-disabled" : ""}`}
      >
        {options.map((op) => (
          <option key={op.value} value={op.value}>
            {op.label}
          </option>
        ))}
      </select>
      <span className="bpi-select-arrow">▼</span>
    </div>
  );
}

function PagerButton({ children, onClick, active, disabled }) {
  return (
    <button
      disabled={disabled}
      onClick={onClick}
      className={[
        "bpi-btn",
        active ? "bpi-btn-active" : "",
        disabled ? "bpi-btn-disabled" : "",
      ].join(" ")}
    >
      {children}
    </button>
  );
}
