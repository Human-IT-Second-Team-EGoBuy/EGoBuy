export const DEV_USE_MOCK = true;


export const DUMMY_CATEGORIES = [
  { category_id: 1, category_name: "식량작물" },
  { category_id: 2, category_name: "과수" },
  { category_id: 3, category_name: "채소" },
  { category_id: 4, category_name: "화훼" },
  { category_id: 5, category_name: "특용작물" },
  { category_id: 7, category_name: "기타작물" },

];

export const DUMMY_CROPS = [
  { crop_id: 10, category_id: 3, crop_name: "토마토" },
  { crop_id: 11, category_id: 2, crop_name: "딸기" },
  { crop_id: 20, category_id: 2, crop_name: "사과" },
  { crop_id: 30, category_id: 1, crop_name: "감자" },
];

export const DUMMY_INSECTS = [
  // insect 테이블에서 왔다고 가정
  { insect_id: 101, crop_id: 10, pest_name: "담배가루이", updated_at: "2026-02-08" },
  { insect_id: 102, crop_id: 11, pest_name: "응애", updated_at: "2026-02-06" },
  { insect_id: 103, crop_id: 20, pest_name: "복숭아순나방", updated_at: "2026-02-05" },
];

export const DUMMY_DISEASES = [
  // disease 테이블에서 왔다고 가정
  { disease_id: 201, crop_id: 10, pest_name: "역병", updated_at: "2026-02-07" },
  { disease_id: 202, crop_id: 20, pest_name: "점무늬낙엽병", updated_at: "2026-02-04" },
  { disease_id: 203, crop_id: 30, pest_name: "무름병", updated_at: "2026-02-03" },
];

export const DUMMY_INSECT_DETAILS = [
  {
    insect_id: 101,
    distrb_info: "주로 시설재배 작물에서 흔히 발생",
    ecology_info: "고온·건조 시 급증",
    damage_info: "잎 뒷면 흡즙 → 생육 저하, 그을음병 유발",
    prevent_method: "환기/습도 관리, 발생 초기에 방제",
    biology_prvnbe_mth: "끈끈이트랩, 천적 이용 가능",
    chemical_prvnbe_mth: "등록 약제 교호 살포",
  },
  {
    insect_id: 102,
    distrb_info: "딸기에서 자주 발생",
    ecology_info: "건조할수록 번식 빠름",
    damage_info: "잎 황화·잎말림, 수량 감소",
    prevent_method: "초기 발견 시 바로 제거/방제",
    biology_prvnbe_mth: "천적응애 투입",
    chemical_prvnbe_mth: "살비제 계열 교호 살포",
  },
];

export const DUMMY_DISEASE_DETAILS = [
  {
    disease_id: 201,
    infection_route: "토양/감염 잔재에서 전염",
    development_condition: "저온다습 환경에서 급속 확산",
    symptoms: "잎·줄기 암갈색 병반, 포자 형성",
    prevention_method: "배수 개선, 밀식 피하기, 예방 살포",
    biology_prvnbe_mth: "건전묘 사용, 윤작",
    chemical_prvnbe_mth: "등록 살균제 교호 살포",
  },
  {
    disease_id: 203,
    infection_route: "상처 부위 통해 침입",
    development_condition: "고온다습 + 저장 중 발생 가능",
    symptoms: "연화/부패, 악취 동반",
    prevention_method: "상처 방지, 저장 환경 관리",
    biology_prvnbe_mth: "선별 강화, 위생 관리",
    chemical_prvnbe_mth: "등록 약제 처리(필요 시)",
  },
];

