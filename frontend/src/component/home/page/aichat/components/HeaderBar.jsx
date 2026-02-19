import ModeToggle from "./ModeToggle";

const HEADER_TEXT = {
  chat: {
    title: "AI 챗봇 상담",
    desc: "증상과 재배 환경을 입력하면 원인과 조치 방법을 안내해요.",
  },
  vision: {
    title: "이미지 진단",
    desc: "작물을 선택하고 이미지를 업로드해 진단 결과를 확인해요.",
  },
};

export default function HeaderBar({ mode, setMode }) {
   const { title, desc } = HEADER_TEXT[mode] ?? HEADER_TEXT.chat;

  return (
    <div className="hb-wrap">
      <div>
        <div className="hb-title">{title}</div>
        <div className="hb-desc">{desc}</div>
      </div>

      <ModeToggle mode={mode} setMode={setMode} />
    </div>
  );
}
