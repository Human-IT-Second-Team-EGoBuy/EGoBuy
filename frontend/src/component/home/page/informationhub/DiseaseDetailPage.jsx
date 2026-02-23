// src/component/home/page/informationHub/DiseaseDetailPage.jsx
import { useEffect, useMemo, useState, useCallback } from "react";
import { useParams } from "react-router-dom";
import axios from "axios";
import PestDetailLayout from "./PestDetailLayout";
import { DEV_USE_MOCK, DUMMY_CROPS, DUMMY_DISEASES, DUMMY_DISEASE_DETAILS } from "./pestDummy";

function TypeBadge() {
  return <span className="pd-badge pd-badge-disease">병</span>;
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


export default function DiseaseDetailPage() {

  //  라우터가 :diseaseId 이므로 키도 맞춤
  const { diseaseId } = useParams();
  const id = Number(diseaseId);

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
      if (DEV_USE_MOCK) {
        const b = DUMMY_DISEASES.find((x) => x.disease_id === id) || null;
        if (!b) {
          setBase(null);
          setDetail(null);
          setState("nf");
          return;
        }
        const d = DUMMY_DISEASE_DETAILS.find((x) => x.disease_id === id) || null;
        setBase(b);
        setDetail(d);
        setState("ok");
        return;
      }

      const res = await axios.get(`/api/information-hub/diseases/${id}`);
      const body = res.data;

      if (body?.code !== "SU" || !body?.data) {
        setState(body?.code === "NF" ? "nf" : "error");
        return;
      }

      const data = body.data;
      const b = data.base || data.disease || data;
      const d = data.detail || data.diseaseDetail || null;

      setBase(b);
      setDetail(d);
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

  const title = base?.pest_name ?? base?.sick_name_kor ?? "병 상세";
  const subtitle = base
    ? joinLines(
        `작물: ${base.crop_name ?? cropNameMap.get(base.crop_id) ?? "-"}`,
        base?.sick_name_eng ? `영문: ${base.sick_name_eng}` : null,
        base?.sick_name_chn ? `한자: ${base.sick_name_chn}` : null,
        detail?.virus_name ? `병원체: ${detail.virus_name}` : null
      )
    : "";

  const sections = useMemo(
    () =>
      [
        { key: "cond", label: "발생상태", value: normalizeText(detail?.development_condition) },
        { key: "symp", label: "증상", value: normalizeText(detail?.symptoms) },
        { key: "prevent", label: "방제방법", value: normalizeText(detail?.prevention_method) },
        { key: "bio", label: "생물학적 방제", value: normalizeText(detail?.biology_prvnbe_mth) },
        { key: "chem", label: "화학적 방제", value: normalizeText(detail?.chemical_prvnbe_mth) },
      ].filter((s) => s.value),
    [detail]
  );

  return (
    <PestDetailLayout
      title={state === "ok" ? title : state === "loading" ? "불러오는 중…" : "병 상세"}
      subtitle={state === "ok" ? subtitle : ""}
      badge={<TypeBadge />}
      sections={sections}
      state={state}
    />
  );
}
