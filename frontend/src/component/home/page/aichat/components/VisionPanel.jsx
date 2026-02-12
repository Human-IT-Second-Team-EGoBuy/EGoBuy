import CropSelector from "./CropSelector";
import ImageUploader from "./ImageUploader";
import ImagePreview from "./ImagePreview";
import ExampleGuide from "./ExampleGuide";
import ResultPanel from "./ResultPanel";

export default function VisionPanel({
  cropId,
  cropName,
  cropItems,
  onSelectCropId,

  file,
  previewUrl,
  result,

  isDragging,
  fileInputRef,
  onPickFile,
  onReset,
  onFileChange,
  onDragEnter,
  onDragOver,
  onDragLeave,
  onDrop,
  onDiagnose,
  diagnosing,
}) {
  const leftSpan = result ? "lg:col-span-5" : "lg:col-span-12";

  //  진단 가능 조건(안전)
  const canDiagnose = !!file && !!cropId && !diagnosing;

  return (
    <div className="vp-grid">
      <div className={leftSpan}>
        <div className="vp-left-box">
          <CropSelector
            cropId={cropId}
            cropName={cropName}
            items={cropItems}
            onSelect={onSelectCropId}
          />

          <ImageUploader
            file={file}
            fileInputRef={fileInputRef}
            isDragging={isDragging}
            onPickFile={onPickFile}
            onReset={onReset}
            onFileChange={onFileChange}
            onDragEnter={onDragEnter} 
            onDragOver={onDragOver}
            onDragLeave={onDragLeave}
            onDrop={onDrop}
          />

          {previewUrl ? <ImagePreview url={previewUrl} /> : null}
          {!result ? <ExampleGuide /> : null}

          <div className="vp-actions">
            <button
              type="button"
              onClick={onDiagnose}
              disabled={!canDiagnose}
              className="vp-primary"
            >
              {diagnosing ? "진단중..." : "진단하기"}
            </button>

            <button
              type="button"
              onClick={onPickFile}
              disabled={diagnosing}
              className="vp-secondary"
            >
              파일 선택
            </button>

            {/* (선택) 결과가 있을 때만 리셋 버튼 추가하고 싶으면 */}
            {/* <button type="button" onClick={onReset} disabled={diagnosing} className="vp-ghost">
              초기화
            </button> */}
          </div>
        </div>
      </div>

      {result ? (
        <div className="lg:col-span-7">
          <ResultPanel crop={result?.cropName ?? cropName} result={result} />
        </div>
      ) : null}
    </div>
  );
}
