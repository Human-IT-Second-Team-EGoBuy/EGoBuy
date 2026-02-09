// src/component/page/aichat/components/ExampleGuide.jsx
export default function ExampleGuide() {
  return (
    <div className="eg-wrap">
      <div className="eg-title">촬영 예시</div>
      <div className="eg-desc">
        잎이 선명하게 보이도록 가까이, 흔들림 없이 촬영해 주세요.
      </div>

      <div className="eg-grid">
        <div className="eg-card">
          <img src="/examples/good_leaf.jpg" alt="good example" className="eg-img" />
          <div className="eg-caption">✅ 좋은 예시</div>
        </div>

        <div className="eg-card">
          <img src="/examples/bad_leaf.jpg" alt="bad example" className="eg-img" />
          <div className="eg-caption">⚠️ 나쁜 예시(흐림/멀리)</div>
        </div>
      </div>

      <div className="eg-tip">
        팁: 잎 전체가 프레임 안에 들어오게, 배경은 단순하게 찍으면 좋아요.
      </div>
    </div>
  );
}
