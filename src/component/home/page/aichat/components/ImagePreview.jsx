// src/component/page/aichat/components/ImagePreview.jsx
export default function ImagePreview({ url }) {
  return (
    <div>
      <div className="ip-label">미리보기</div>
      <div className="ip-box">
        <img src={url} alt="preview" className="ip-img" />
      </div>
    </div>
  );
}
