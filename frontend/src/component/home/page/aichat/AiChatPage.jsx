// src/component/page/aichat/AiChatPage.jsx
import { useRef, useState } from "react";
import { CROP_ITEMS, MODEL_BY_CROP, MOCK_RESULTS } from "./constants";
import "./aichat.css";

import HeaderBar from "./components/HeaderBar";
import ChatPanel from "./components/ChatPanel";
import VisionPanel from "./components/VisionPanel";

export default function AiChatPage() {
  const [mode, setMode] = useState("chat");
  const fileInputRef = useRef(null);

  const [crop, setCrop] = useState("딸기");
  const selectedModel = MODEL_BY_CROP[crop];

  const [file, setFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState("");
  const [result, setResult] = useState(null);
  const [isDragging, setIsDragging] = useState(false);

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

    if (!f.type?.startsWith("image/")) {
      alert("이미지 파일만 업로드할 수 있어요 (jpg/png 등)");
      return;
    }

    setFile(f);
    setResult(null);

    clearPreviewUrl();
    setPreviewUrl(URL.createObjectURL(f));
  };

  const onFileChange = (e) => setFileAndPreview(e.target.files?.[0]);

  const onDragOver = (e) => {
    e.preventDefault();
    setIsDragging(true);
  };
  const onDragLeave = () => setIsDragging(false);
  const onDrop = (e) => {
    e.preventDefault();
    setIsDragging(false);
    setFileAndPreview(e.dataTransfer.files?.[0]);
  };

  // Top-1만
  const onDiagnose = () => {
    if (!file) return alert("이미지를 업로드해 주세요!");
    const top1 = MOCK_RESULTS[selectedModel]?.[0];

    setResult({
      crop,
      model: selectedModel,
      top1,
      advice: [
        "사진이 흐리면 결과가 불안정할 수 있어요. 잎을 가까이 촬영해 보세요.",
        "잎의 앞/뒷면, 줄기, 전체 개체 사진을 추가로 찍으면 정확도가 올라가요.",
      ],
    });
  };

  return (
    <div className="ap-wrap">
      <div className="ap-card">
        <HeaderBar mode={mode} onChangeMode={setMode} />

        <div className="ap-body">
          {mode === "chat" ? (
            <ChatPanel />
          ) : (
            <VisionPanel
              crop={crop}
              cropItems={CROP_ITEMS}
              selectedModel={selectedModel}
              onSelectCrop={(k) => {
                setCrop(k);
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
              onDragOver={onDragOver}
              onDragLeave={onDragLeave}
              onDrop={onDrop}
              onDiagnose={onDiagnose}
            />
          )}
        </div>
      </div>
    </div>
  );
}
