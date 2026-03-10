// src/component/page/aichat/AiChatPage.jsx
import { useEffect, useMemo, useRef, useState } from "react";
import "./aichat.css";
import axios from "axios";
import { useSearchParams } from "react-router-dom";

import HeaderBar from "./components/HeaderBar";
import ChatPanel from "./components/ChatPanel";
import VisionPanel from "./components/VisionPanel";

import { CROP_ITEMS } from "./constants";

const DEFAULT_ADVICE = [
  "사진이 흐리면 결과가 불안정할 수 있어요. 잎을 가까이 촬영해 보세요.",
  "잎의 앞/뒷면, 줄기, 전체 개체 사진을 추가로 찍으면 정확도가 올라가요.",
];

const MAX_IMAGE_BYTES = 10 * 1024 * 1024;

export default function AiChatPage() {
  const [mode, setMode] = useState("chat");
  const fileInputRef = useRef(null);
  const dragCounterRef = useRef(0);

  const [cropItems, setCropItems] = useState([]);
  const [cropId, setCropId] = useState(null);

  const [file, setFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState("");
  const [result, setResult] = useState(null);

  const [isDragging, setIsDragging] = useState(false);
  const [diagnosing, setDiagnosing] = useState(false);

  const [searchParams] = useSearchParams();

  const cropName = useMemo(() => {
    return cropItems.find((x) => Number(x.crop_id) === Number(cropId))?.crop_name ?? "";
  }, [cropItems, cropId]);

  useEffect(() => {
    setCropItems(CROP_ITEMS);

    const qMode = searchParams.get("mode");
    if (qMode === "vision") setMode("vision");

    const qCropId = searchParams.get("cropId");
    // cropId가 없으면 기존처럼 첫번째로
    const initialCropId = qCropId
      ? Number(qCropId)
      : (CROP_ITEMS.length ? Number(CROP_ITEMS[0].crop_id) : null);

    setCropId(initialCropId);
    }, []);

  useEffect(() => {
    return () => {
      if (previewUrl) URL.revokeObjectURL(previewUrl);
    };
  }, [previewUrl]);

  const clearPreviewUrl = () => {
    if (previewUrl) URL.revokeObjectURL(previewUrl);
    setPreviewUrl("");
  };

  const resetAll = () => {
    setFile(null);
    clearPreviewUrl();
    setResult(null);
    if (fileInputRef.current) fileInputRef.current.value = "";
  };

  const pickFile = () => fileInputRef.current?.click();

  const setFileAndPreview = (f) => {
  if (!f) return;

  // 이미지 타입 체크
  if (!f.type?.startsWith("image/")) {
    alert("이미지 파일만 업로드할 수 있어요 (jpg/png 등)");
    return;
  }

  // 파일 크기 체크 (프론트 UX)
  if (f.size > MAX_IMAGE_BYTES) {
    alert("이미지 용량이 너무 커요. 10MB 이하로 업로드해 주세요.");
    return;
  }

  setFile(f);
  setResult(null);

  clearPreviewUrl();
  setPreviewUrl(URL.createObjectURL(f));
};

  const onFileChange = (e) => setFileAndPreview(e.target.files?.[0]);

  const onDragEnter = (e) => {
    e.preventDefault();
    e.stopPropagation();
    dragCounterRef.current += 1;
    setIsDragging(true);
  };

  const onDragOver = (e) => {
    e.preventDefault();
    e.stopPropagation();
  };

  const onDragLeave = (e) => {
    e.preventDefault();
    e.stopPropagation();
    dragCounterRef.current -= 1;

    if (dragCounterRef.current <= 0) {
      dragCounterRef.current = 0;
      setIsDragging(false);
    }
  };

  const onDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();

    dragCounterRef.current = 0;
    setIsDragging(false);
    setFileAndPreview(e.dataTransfer.files?.[0]);
  };

  const onDiagnose = async () => {
    if (diagnosing) return;

    if (!file) return alert("이미지를 업로드해 주세요!");
    if (cropId == null) return alert("작물을 선택해 주세요!");

    setDiagnosing(true);
    try {
      const form = new FormData();
      form.append("cropId", String(cropId));
      form.append("topK", "5");
      form.append("image", file);

      const res = await axios.post("/api/ai-chat/vision/diagnose", form);

      const payload = res.data?.data ?? res.data?.content ?? res.data;

      const normItem = (x) => ({
        label: x?.label ?? null,
        labelKo: x?.labelKo ?? x?.label_ko ?? null,
        prob: Number(x?.prob ?? 0),
      });

      const best = payload?.best ? normItem(payload.best) : null;
      const topK = Array.isArray(payload?.topK) ? payload.topK.map(normItem) : [];

      setResult({
        cropId: payload?.cropId ?? cropId,
        cropName,
        model: payload?.modelKey ?? null,
        top1: best,
        topK,
        summary: payload?.ragAnswer ?? "",
        advice: DEFAULT_ADVICE,
        meta: payload?.meta ?? null,
        raw: payload,
      });
    } catch (e) {
      console.error("status:", e.response?.status);
      console.error("response:", e.response?.data);
      alert("진단 중 오류가 발생했어요.");
    } finally {
      setDiagnosing(false);
    }
  };

  return (
    <div className="ap-wrap">
      <div className="ap-card">
        <HeaderBar mode={mode} setMode={setMode} />

        <div className="ap-body">
          {mode === "chat" ? (
            <ChatPanel />
          ) : (
            <VisionPanel
              cropId={cropId}
              cropName={cropName}
              cropItems={cropItems}
              onSelectCropId={(id) => {
                setCropId(Number(id));
                setResult(null);
              }}
              file={file}
              previewUrl={previewUrl}
              result={result}
              isDragging={isDragging}
              fileInputRef={fileInputRef}
              onPickFile={pickFile}
              onReset={resetAll}
              onFileChange={onFileChange}
              onDragEnter={onDragEnter}
              onDragOver={onDragOver}
              onDragLeave={onDragLeave}
              onDrop={onDrop}
              onDiagnose={onDiagnose}
              diagnosing={diagnosing}
            />
          )}
        </div>
      </div>
    </div>
  );
}