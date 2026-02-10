// src/component/page/aichat/components/ExampleGuide.jsx
import { useState } from "react";

// S3 URL은 나중에 여기만 바꾸면 됨(또는 constants로 이동)
// .env에  VITE_EXAMPLE_GOOD_URL=https://YOUR_CDN/examples/good_leaf.jpg
//        VITE_EXAMPLE_BAD_URL=https://YOUR_CDN/examples/bad_leaf.jpg

const GOOD_IMG_URL = import.meta.env.VITE_EXAMPLE_GOOD_URL; // 예: https://.../good_leaf.jpg
const BAD_IMG_URL = import.meta.env.VITE_EXAMPLE_BAD_URL;   // 예: https://.../bad_leaf.jpg

function ExampleCard({ src, label }) {
  const [failed, setFailed] = useState(false);

  return (
    <div className="eg-card">
      {failed || !src ? (
        <div className="eg-fallback">
          <div className="eg-fallback-title">예시 이미지 준비중</div>
          <div className="eg-fallback-desc">이미지를 불러올 수 없어요.</div>
        </div>
      ) : (
        <img
          src={src}
          alt={label}
          className="eg-img"
          onError={() => setFailed(true)}
          loading="lazy"
        />
      )}
      <div className="eg-caption">{label}</div>
    </div>
  );
}

export default function ExampleGuide() {
  return (
    <div className="eg-wrap">
      <div className="eg-title">촬영 예시</div>
      <div className="eg-desc">
        잎이 선명하게 보이도록 가까이, 흔들림 없이 촬영해 주세요.
      </div>

      <div className="eg-grid">
        <ExampleCard src={GOOD_IMG_URL} label="✅ 좋은 예시" />
        <ExampleCard src={BAD_IMG_URL} label="⚠️ 나쁜 예시(흐림/멀리)" />
      </div>

      <div className="eg-tip">
        팁: 잎 전체가 프레임 안에 들어오게, 배경은 단순하게 찍으면 좋아요.
      </div>
    </div>
  );
}
