// src/component/page/aichat/AiChatPage.jsx
import { useEffect, useMemo, useRef, useState } from "react";
import "./aichat.css";
import axios from "axios"; //  실API 붙일 때만 주석 해제

import HeaderBar from "./components/HeaderBar";
import ChatPanel from "./components/ChatPanel";
import VisionPanel from "./components/VisionPanel";

import { DEV_USE_MOCK, CROP_ITEMS , MOCK_RESULTS } from "./constants";

//  기본 안내문(백엔드 advice 없을 때 fallback)
const DEFAULT_ADVICE = [
  "사진이 흐리면 결과가 불안정할 수 있어요. 잎을 가까이 촬영해 보세요.",
  "잎의 앞/뒷면, 줄기, 전체 개체 사진을 추가로 찍으면 정확도가 올라가요.",
];

export default function AiChatPage() {
  const [mode, setMode] = useState("chat");
  const fileInputRef = useRef(null);
  const dragCounterRef = useRef(0);

  // 작물 목록/선택
  const [cropItems, setCropItems] = useState([]);
  const [cropId, setCropId] = useState(null);

  // 파일 업로드/프리뷰/진단 결과
  const [file, setFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState("");
  const [result, setResult] = useState(null);

  // UI 상태
  const [isDragging, setIsDragging] = useState(false);
  const [diagnosing, setDiagnosing] = useState(false);

  //  선택된 작물명 계산 (cropId 바뀔 때마다 즉시 반영)
  const cropName = useMemo(() => {
    return cropItems.find((x) => Number(x.crop_id) === Number(cropId))?.crop_name ?? "";
  }, [cropItems, cropId]);

  //  1) 작물 목록 로딩 (CROP_ITEMS만 사용)
  // - CROP_ITEMS가 비어있는 경우 cropId는 null로 안전 처리
  useEffect(() => {
    setCropItems(CROP_ITEMS);
    setCropId(CROP_ITEMS.length ? Number(CROP_ITEMS[0].crop_id) : null);
  }, []);

  //  2) previewUrl 메모리 누수 방지
  // - previewUrl이 바뀌거나 컴포넌트 언마운트 될 때 revoke
  useEffect(() => {
    return () => {
      if (previewUrl) URL.revokeObjectURL(previewUrl);
    };
  }, [previewUrl]);

  //  previewUrl 제거
  const clearPreviewUrl = () => {
    if (previewUrl) URL.revokeObjectURL(previewUrl);
    setPreviewUrl("");
  };

  //  전체 리셋
  const resetAll = () => {
    setFile(null);
    clearPreviewUrl();
    setResult(null);
    if (fileInputRef.current) fileInputRef.current.value = "";
  };

  //  파일 픽커 열기
  const pickFile = () => fileInputRef.current?.click();

  //  파일 세팅 + 프리뷰 생성
  const setFileAndPreview = (f) => {
    if (!f) return;

    // 이미지 파일만 허용
    if (!f.type?.startsWith("image/")) {
      alert("이미지 파일만 업로드할 수 있어요 (jpg/png 등)");
      return;
    }

    // 새 파일 들어오면 기존 결과는 무조건 초기화
    setFile(f);
    setResult(null);

    // 기존 URL 정리 후 새 URL 생성
    clearPreviewUrl();
    setPreviewUrl(URL.createObjectURL(f));
  };

  const onFileChange = (e) => setFileAndPreview(e.target.files?.[0]);

  //  drag & drop
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

  //  진단 요청 (목업/실API 분기)
  const onDiagnose = async () => {
    // 중복 클릭 방지
    if (diagnosing) return;

    // 필수값 체크
    if (!file) return alert("이미지를 업로드해 주세요!");
    if (!cropId) return alert("작물을 선택해 주세요!");

    setDiagnosing(true);
    try {
      // ===== 목업 모드 =====
      if (DEV_USE_MOCK) {
        const selected = cropItems.find((x) => Number(x.crop_id) === Number(cropId));
        const model = selected?.model ?? "model-liriope";
        const cropNameLocal = selected?.crop_name ?? ""; 


        //  constants.js의 MOCK_RESULTS에서 해당 모델 결과 가져오기
        const list = Array.isArray(MOCK_RESULTS?.[model]) ? MOCK_RESULTS[model] : [];

        //  MOCK_RESULTS는 { name, conf } 형태 → ResultPanel이 바로 먹도록 top1/topK 구성
        const topK = list.map((x, idx) => ({
          name: x?.name ?? "결과",
          conf: Number(x?.conf ?? 0),
          index: idx,
        }));

        const top1 = topK[0] ?? { name: "결과 없음", conf: 0, index: 0 };

        setResult({
          cropId,
          cropName: cropNameLocal,
          model,
          top1,
          topK,
          summary: `${cropNameLocal}에서 ${top1.name} 가능성이 높아요.`,
          advice: DEFAULT_ADVICE,
        });
        return;
      }

      // ===== 실API 모드 (현재 주석) =====
      const form = new FormData();
      form.append("cropId", String(cropId));
      form.append("topK", "5");
      form.append("image", file);
      
      const res = await axios.post("/api/ai-chat/vision/diagnose", form);
      
const data = res.data?.content;

// best(Top-1) 구조를 ResultPanel이 읽기 쉬운 형태로 정규화
const best = data?.best
  ? {
      // ResultPanel은 label_ko를 우선으로 보니까 그 키로 맞춰줌
      label_ko: data.best.labelKo ?? data.best.label_ko ?? null,
      // 기존 키도 보존(디버깅/호환)
      label: data.best.label ?? null,
      prob: Number(data.best.prob ?? 0),
    }
  : null;

    const topK = Array.isArray(data?.topK)
      ? data.topK.map((x) => ({
          label_ko: x.labelKo ?? x.label_ko ?? null,
          label: x.label ?? null,
          prob: Number(x.prob ?? 0),
        }))
      : [];

    setResult({
      cropId: data?.cropId ?? cropId,
      cropName,
      model: data?.modelKey ?? null,

      // ResultPanel은 result.top1 또는 result.best를 보니까
      // top1으로 best 객체를 넣어주면 pickTop1이 그대로 읽음
      top1: best,

      // topK도 정규화된 배열로
      topK,

      // summary는 문자열이니까 그대로
      summary: data?.ragAnswer ?? "",

      advice: DEFAULT_ADVICE,
      raw: data,
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
                setResult(null); // 작물 바꾸면 기존 결과 초기화
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
