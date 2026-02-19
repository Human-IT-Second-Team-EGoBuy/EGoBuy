

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
  { crop_id: 4, crop_name: "딸기", emoji: "🍓", model: "model-liriope" },
  { crop_id: 2, crop_name: "사과", emoji: "🍎", model: "model-liriope" },
  { crop_id: 1, crop_name: "포도", emoji: "🍇", model: "model-liriope" },
  { crop_id: 3, crop_name: "토마토", emoji: "🍅", model: "model-liriope" },
  { crop_id: 102, crop_name: "감자", emoji: "🥔", model: "model-wambugu" },
  { crop_id: 101, crop_name: "쌀", emoji: "🌾", model: "model-wambugu" },
  { crop_id: 104, crop_name: "밀", emoji: "🌿", model: "model-wambugu" },
  { crop_id: 103, crop_name: "옥수수", emoji: "🌽", model: "model-wambugu" },
];


export const DEV_USE_MOCK = false;


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

// src/component/page/aichat/components/constants.js

export const DUMMY_CHATS = [
  {
    id: "c1",
    title: "토마토 잎 말림 문의",
    messages: [
      { role: "bot", text: "토마토 잎이 말리는 원인은 수분 스트레스/해충/바이러스 등 다양해요." },
      { role: "user", text: "최근 잎이 위로 말리고 노랗게 변해요." },
      { role: "bot", text: "관수/온도 변화 여부와 잎 뒷면 해충(진딧물) 유무를 먼저 확인해볼까요?" },
    ],
  },
  {
    id: "c2",
    title: "사과 점무늬 증상",
    messages: [
      { role: "user", text: "사과 잎에 갈색 점이 많이 생겼어요." },
      { role: "bot", text: "탄저병/겹무늬썩음병/점무늬낙엽병 가능성이 있어요. 사진이 있으면 더 정확해요." },
    ],
  },
  {
    id: "c3",
    title: "감자 잎 반점",
    messages: [
      { role: "user", text: "감자 잎에 반점이 퍼져요." },
      { role: "bot", text: "역병/겹무늬병 등을 의심할 수 있어요. 발생 시기와 날씨도 중요해요." },
    ],
  },
  {
    id: "c4",
    title: "벼(쌀) 생육 상담",
    messages: [
      { role: "user", text: "벼 잎 끝이 마르는 것 같아요." },
      { role: "bot", text: "수분/염류/비료 과다/병해 가능성이 있어요. 논 물관리 상태를 알려주세요." },
    ],
  },
];
