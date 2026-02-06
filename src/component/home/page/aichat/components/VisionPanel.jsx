// src/component/page/aichat/components/VisionPanel.jsx
import CropSelector from "./CropSelector";
import ImageUploader from "./ImageUploader";
import ImagePreview from "./ImagePreview";
import ExampleGuide from "./ExampleGuide";
import ResultPanel from "./ResultPanel";

export default function VisionPanel({
  crop,
  cropItems,
  selectedModel,
  onSelectCrop,

  file,
  previewUrl,
  result,

  isDragging,
  fileInputRef,
  onPickFile,
  onReset,
  onFileChange,
  onDragOver,
  onDragLeave,
  onDrop,
  onDiagnose,
}) {
  const leftSpan = result ? "lg:col-span-5" : "lg:col-span-12";

  return (
    <div className="vp-grid">
      {/* 좌측 */}
      <div className={leftSpan}>
        <div className="vp-left-box">
          <CropSelector
            crop={crop}
            items={cropItems}
            selectedModel={selectedModel}
            onSelect={onSelectCrop}
          />

          <ImageUploader
            file={file}
            fileInputRef={fileInputRef}
            isDragging={isDragging}
            onPickFile={onPickFile}
            onReset={onReset}
            onFileChange={onFileChange}
            onDragOver={onDragOver}
            onDragLeave={onDragLeave}
            onDrop={onDrop}
          />

          {previewUrl && <ImagePreview url={previewUrl} />}

          {!result && <ExampleGuide />}

          <div className="vp-actions">
            <button onClick={onDiagnose} disabled={!file} className="vp-primary">
              진단하기
            </button>

            <button onClick={onPickFile} className="vp-secondary">
              파일 선택
            </button>
          </div>
        </div>
      </div>

      {/* 우측 */}
      {result && (
        <div className="lg:col-span-7">
          <ResultPanel crop={crop} result={result} />
        </div>
      )}
    </div>
  );
}
