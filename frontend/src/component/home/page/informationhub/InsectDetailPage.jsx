// src/component/home/page/informationHub/InsectDetailPage.jsx
import { useEffect, useMemo, useState, useCallback } from "react";
import { useParams } from "react-router-dom";
import axios from "axios";
import PestDetailLayout from "./PestDetailLayout";
import { DEV_USE_MOCK, DUMMY_CROPS, DUMMY_INSECTS, DUMMY_INSECT_DETAILS } from "./pestDummy";

function TypeBadge() {
  return <span className="pd-badge pd-badge-insect">해충</span>;
}

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
      if (DEV_USE_MOCK) {
        const b = DUMMY_INSECTS.find((x) => x.insect_id === id) || null;
        if (!b) {
          setBase(null);
          setDetail(null);
          setState("nf");
          return;
        }
        const d = DUMMY_INSECT_DETAILS.find((x) => x.insect_id === id) || null;
        setBase(b);
        setDetail(d);
        setState("ok");
        return;
      }

      const res = await axios.get(`/api/information-hub/insects/${id}`);
      const body = res.data;

      if (body?.code !== "SU" || !body?.data) {
        setState(body?.code === "NF" ? "nf" : "error");
        return;
      }

      const data = body.data;
      const b = data.base || data.insect || data;
      const d = data.detail || data.insectDetail || null;

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

  const title = base?.pest_name ?? base?.tgt_vrmn_name ?? "해충 상세";
  const updated = base?.updated_at ? String(base.updated_at).slice(0, 10) : "-";
  const subtitle = base
    ? `${cropNameMap.get(base.crop_id) ?? "-"} · 업데이트 ${updated}`
    : "";

  const sections = useMemo(
    () => [
      { key: "distrb", label: "분포 정보", value: detail?.distrb_info },
      { key: "stle", label: "형태 정보", value: detail?.stle_info },
      { key: "ecology", label: "생태 정보", value: detail?.ecology_info },
      { key: "damage", label: "피해 정보", value: detail?.damage_info },
      { key: "qrant", label: "검역/기타", value: detail?.qrant_info },
      { key: "prevent", label: "예방/관리", value: detail?.prevent_method },
      { key: "bio", label: "생물적 방제", value: detail?.biology_prvnbe_mth },
      { key: "chem", label: "화학적 방제", value: detail?.chemical_prvnbe_mth },
      { key: "etc", label: "기타", value: detail?.etc },
    ],
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
