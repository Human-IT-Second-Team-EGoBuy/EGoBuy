import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./insectpestsinfo.css";
import axios from "axios";

const PAGE_SIZE = 10;

export default function InsectPestsInfoPage() {
  const nav = useNavigate();

  /* 필터 상태 */
  const [categoryId, setCategoryId] = useState("all");
  const [ptype, setPtype] = useState("all");
  const [q, setQ] = useState("");

  /* 데이터 상태 */
  const [categories, setCategories] = useState([]);
  const [rows, setRows] = useState([]);

  /* 서버 페이징 */
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);

  /* 1) 카테고리 로드 */
  useEffect(() => {
    (async () => {
      const res = await axios.get("/api/information-hub/crop-categories");
      setCategories(res.data?.content ?? []);
    })();
  }, []);

  /* 2) 필터/검색/페이지 바뀌면 -> 서버에서 목록 다시 받기 */
  useEffect(() => {
    (async () => {
      const res = await axios.get("/api/information-hub/pest-issues", {
        params: {
          categoryId: categoryId === "all" ? undefined : categoryId,
          ptype: ptype === "all" ? undefined : ptype,
          q: q.trim() ? q.trim() : undefined,
          page,
          size: PAGE_SIZE,
        },
      });

      const data = res.data?.content ?? {};
      setRows(data.items ?? []);
      setTotalPages(data.totalPages ?? 1);
    })();
  }, [categoryId, ptype, q, page]);

  useEffect(() => {
    if (page > totalPages) setPage(totalPages);
  }, [page, totalPages]);

  const onRowClick = (r) => {
    if (r.pest_type === "insect") nav(`/insect-pests-info/insects/${r.pest_id}`);
    else nav(`/insect-pests-info/diseases/${r.pest_id}`);
  };

  return (
    <div className="bpi-wrap select-none">
      <div className="bpi-container">
        <div className="bpi-card">
          <div className="bpi-head">
            <h1 className="bpi-title">병해충 정보</h1>
          </div>

          <div className="bpi-divider" />

          <div className="bpi-body">
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
                    </tr>
                  </thead>

                  <tbody className="bpi-tbody">
                    {rows.length === 0 ? (
                      <tr>
                        <td colSpan={4} className="bpi-empty">
                          조건에 맞는 데이터가 없어요.
                        </td>
                      </tr>
                    ) : (
                      rows.map((r, idx) => (
                        <tr
                          key={`${r.pest_type}-${r.pest_id}`}
                          className="bpi-tr"
                          onClick={() => onRowClick(r)}
                          title="상세 보기"
                        >
                          <td className="bpi-td-muted">
                            {(page - 1) * PAGE_SIZE + idx + 1}
                          </td>
                          <td className="bpi-td">{r.crop_name ?? "-"}</td>
                          <td className="bpi-td bpi-name">{r.pest_name}</td>
                          <td className="bpi-td">
                            <TypeBadge type={r.pest_type} />
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>

              <div className="bpi-pager">
                <PagerButton
                  disabled={page === 1}
                  onClick={() => setPage((p) => Math.max(1, p - 1))}
                >
                  이전
                </PagerButton>

                {getPageNumbers(page, totalPages, 5).map((n) => (
                  <PagerButton key={n} active={n === page} onClick={() => setPage(n)}>
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
