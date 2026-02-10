// src/component/home/page/informationHub/DiseaseDetailPage.jsx
import { useEffect, useMemo, useState, useCallback } from "react";
import { useParams } from "react-router-dom";
import axios from "axios";
import PestDetailLayout from "./PestDetailLayout";
import { DEV_USE_MOCK, DUMMY_CROPS, DUMMY_DISEASES, DUMMY_DISEASE_DETAILS } from "./pestDummy";

function TypeBadge() {
  return <span className="pd-badge pd-badge-disease">병</span>;
}

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
  const updated = base?.updated_at ? String(base.updated_at).slice(0, 10) : "-";
  const subtitle = base
    ? `${cropNameMap.get(base.crop_id) ?? "-"} · 업데이트 ${updated}`
    : "";

  const sections = useMemo(
    () => [
      { key: "route", label: "감염 경로", value: detail?.infection_route },
      { key: "cond", label: "발생 조건", value: detail?.development_condition },
      { key: "symp", label: "증상", value: detail?.symptoms },
      { key: "prevent", label: "예방/관리", value: detail?.prevention_method },
      { key: "bio", label: "생물적 방제", value: detail?.biology_prvnbe_mth },
      { key: "chem", label: "화학적 방제", value: detail?.chemical_prvnbe_mth },
      { key: "virus", label: "바이러스명", value: detail?.virus_name },
      { key: "etc", label: "기타", value: detail?.etc },
    ],
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
