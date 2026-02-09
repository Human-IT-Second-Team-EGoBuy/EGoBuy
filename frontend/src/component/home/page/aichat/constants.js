// src/page/aichat/constants.js

export const MODEL_BY_CROP = {
  딸기: "model-liriope",
  사과: "model-liriope",
  포도: "model-liriope",
  토마토: "model-liriope",
  감자: "model-wambugu",
  쌀: "model-wambugu",
  밀: "model-wambugu",
  옥수수: "model-wambugu",
};

export const CROP_ITEMS = [
  { key: "딸기", label: "딸기", emoji: "🍓", model: "model-liriope" },
  { key: "사과", label: "사과", emoji: "🍎", model: "model-liriope" },
  { key: "포도", label: "포도", emoji: "🍇", model: "model-liriope" },
  { key: "토마토", label: "토마토", emoji: "🍅", model: "model-liriope" },
  { key: "감자", label: "감자", emoji: "🥔", model: "model-wambugu" },
  { key: "쌀", label: "쌀", emoji: "🌾", model: "model-wambugu" },
  { key: "밀", label: "밀", emoji: "🌿", model: "model-wambugu" },
  { key: "옥수수", label: "옥수수", emoji: "🌽", model: "model-wambugu" },
];

// UI 테스트용 더미 결과
export const MOCK_RESULTS = {
  "model-liriope": [
    { name: "잎곰팡이병(예시)", conf: 0.86 },
    { name: "흰가루병(예시)", conf: 0.09 },
    { name: "정상(예시)", conf: 0.05 },
  ],
  "model-wambugu": [
    { name: "잎마름병(예시)", conf: 0.78 },
    { name: "점무늬병(예시)", conf: 0.15 },
    { name: "정상(예시)", conf: 0.07 },
  ],
};
