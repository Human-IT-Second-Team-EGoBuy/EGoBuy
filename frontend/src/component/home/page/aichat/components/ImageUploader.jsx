export default function ImageUploader({
  file,
  fileInputRef,
  isDragging,
  onPickFile,
  onReset,
  onFileChange,
  onDragEnter,
  onDragOver,
  onDragLeave,
  onDrop,
}) {
  const handleReset = (e) => {
    e.preventDefault();
    e.stopPropagation();
    onReset();
  };

  return (
    <div>
      <div className="iu-head">
        <div className="iu-title">이미지 업로드</div>

        {file && (
          <button type="button" onClick={handleReset} className="iu-reset">
            초기화
          </button>
        )}
      </div>

      <div
        role="button"
        tabIndex={0}
        onClick={onPickFile}
        onKeyDown={(e) => e.key === "Enter" && onPickFile()}
        onDragEnter={onDragEnter}
        onDragOver={onDragOver}
        onDragLeave={onDragLeave}
        onDrop={onDrop}
        className={`iu-drop ${isDragging ? "iu-drop-drag" : "iu-drop-idle"}`}
      >
        <input
          ref={fileInputRef}
          type="file"
          accept="image/*"
          className="iu-input"
          onChange={onFileChange}
        />

        <div className="iu-main">클릭 또는 드래그해서 업로드</div>
        <div className="iu-sub">잎(앞/뒷면)이 잘 보이게 촬영하면 좋아요</div>

        {file && (
          <div className="iu-file">
            업로드됨: <span className="iu-file-strong">{file.name}</span>
          </div>
        )}
      </div>
    </div>
  );
}
