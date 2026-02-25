// src/component/home/page/informationHub/InsectDetailPage.jsx
import { useEffect, useMemo, useState, useCallback } from "react";
import { useParams } from "react-router-dom";
import axios from "axios";
import PestDetailLayout from "./PestDetailLayout";

function TypeBadge() {
  return <span className="pd-badge pd-badge-insect">해충</span>;
}

const cleanText = (v) => {
  if (v == null) return null;
  const s = String(v).trim();
  return s.length ? s : null;
};

// <br> / <br/> / <br /> 제거(줄바꿈으로 변환) + 불필요한 공백 정리
const normalizeText = (v) => {
  const s = cleanText(v);
  if (!s) return null;

  const out = s
    .replace(/<br\s*\/?>/gi, "\n") // <br>, <br/>, <br /> -> \n
    .replace(/&nbsp;/gi, " ")      
    .replace(/\n{3,}/g, "\n\n")    
    .trim();

  return out.length ? out : null;
};

const joinLines = (...lines) => lines.map(cleanText).filter(Boolean).join("\n");


export default function InsectDetailPage() {
  // 라우터가 :insectId 이므로 키도 맞춤
  const { insectId } = useParams();
  const id = Number(insectId);

  const [state, setState] = useState("loading"); // loading | ok | nf | error
  const [base, setBase] = useState(null);
  const [detail, setDetail] = useState(null);

  const cropNameMap = useMemo(
    () => new Map(DUMMY_CROPS.map((c) => [c.crop_id, c.crop_name])),
    []
  );

  /**  분기 1곳 */
  const fetchDetail = useCallback(async () => {
    setState("loading");

    try {
      const res = await axios.get(`/api/information-hub/insects/${id}`);
      const body = res.data;

      if (!body?.success || !body?.content?.base) {
        setState("nf");
        setBase(null);
        setDetail(null);
        return;
      }

      setBase(body.content.base);
      setDetail(body.content.detail ?? null);
      setState("ok");
    } catch (e) {
      console.error(e);
      setState("error");
    }
  }, [id]);

  useEffect(() => {
    if (!Number.isFinite(id)) {
      setState("nf");
      return;
    }
    fetchDetail();
  }, [id, fetchDetail]);

  const title = base?.pest_name ?? base?.tgt_vrmn_name ?? "해충 상세";

  const subtitle = base
    ? joinLines(
        `작물: ${base?.crop_name ?? "-"}`,
        base?.insect_species_kor ? `종(국문): ${base.insect_species_kor}` : null,
        base?.insect_species ? `학명: ${base.insect_species}` : null,
        base?.insect_order ? `목: ${base.insect_order}` : null,
        base?.insect_family ? `과: ${base.insect_family}` : null,
        base?.insect_genus ? `속: ${base.insect_genus}` : null
      )
    : "";

  const sections = useMemo(
    () => [
      { key: "distrb", label: "분포 정보", value: normalizeText(detail?.distrb_info) },
      { key: "stle", label: "형태 정보", value: normalizeText(detail?.stle_info) },
      { key: "ecology", label: "생태 정보", value: normalizeText(detail?.ecology_info) },
      { key: "damage", label: "피해 정보", value: normalizeText(detail?.damage_info) },
      { key: "qrant", label: "검역 정보", value: normalizeText(detail?.qrant_info) },
      { key: "prevent", label: "예방/관리", value: normalizeText(detail?.prevent_method) },
      { key: "bio", label: "생물적 방제", value: normalizeText(detail?.biology_prvnbe_mth) },
      { key: "chem", label: "화학적 방제", value: normalizeText(detail?.chemical_prvnbe_mth) },
    ].filter((s) => s.value),
    [detail]
  );

  return (
    <PestDetailLayout
      title={state === "ok" ? title : state === "loading" ? "불러오는 중…" : "해충 상세"}
      subtitle={state === "ok" ? subtitle : ""}
      badge={<TypeBadge />}
      sections={sections}
      state={state}
    />
  );
}
