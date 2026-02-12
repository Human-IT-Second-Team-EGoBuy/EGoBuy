import ModeToggle from "./ModeToggle";

export default function HeaderBar({ mode, onChangeMode }) {
  return (
    <div className="hb-wrap">
      <div>
        <div className="hb-title">AI 진단/상담</div>
        <div className="hb-desc">
          작물을 선택하고 이미지를 업로드해 진단 UI를 확인해요.
        </div>
      </div>

      <ModeToggle mode={mode} onChange={onChangeMode} />
    </div>
  );
}
