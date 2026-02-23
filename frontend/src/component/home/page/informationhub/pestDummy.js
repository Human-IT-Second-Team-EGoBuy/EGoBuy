// src/component/home/page/informationhub/pestDummy.js
export const DEV_USE_MOCK = false;

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

/** insect (기본) - 최소 */
export const DUMMY_INSECTS = [
  {
    insect_id: 101,
    crop_id: 10,
    tgt_vrmn_name: "담배가루이",
    insect_species_kor: "담배가루이",
    insect_species: "Bemisia tabaci",
    insect_order: "노린재목",
    insect_family: "가루이과",
    insect_genus: "Bemisia",
    
  },
  {
    insect_id: 102,
    crop_id: 11,
    tgt_vrmn_name: "응애",
    insect_species_kor: "점박이응애",
    insect_species: "Tetranychus urticae",
    insect_order: "거미진드기목",
    insect_family: "거미진드기과",
    insect_genus: "Tetranychus",
   
  },
  {
    insect_id: 103,
    crop_id: 20,
    tgt_vrmn_name: "복숭아순나방",
    insect_species_kor: "복숭아순나방",
    insect_species: "Grapholita molesta",
    insect_order: "나비목",
    insect_family: "잎말이나방과",
    insect_genus: "Grapholita",
   
  },
];


/** insect_detail (상세) - 최소 */
export const DUMMY_INSECT_DETAILS = [
  {
    insect_id: 101,
    distrb_info: "주로 시설재배 작물에서 흔히 발생",
    stle_info: "성충은 작은 흰색 날개를 가지며, 잎 뒷면에 밀집",
    ecology_info: "고온·건조 시 급증, 세대가 빠르게 교체됨",
    damage_info: "잎 뒷면 흡즙 → 생육 저하, 그을음병 유발",
    qrant_info: "외래 유입 가능성 있어 모니터링 권장",
    prevent_method: "환기/습도 관리, 발생 초기에 방제",
    biology_prvnbe_mth: "끈끈이트랩, 천적(좀벌류) 이용 가능",
    chemical_prvnbe_mth: "등록 약제 교호 살포",
  },
  {
    insect_id: 102,
    distrb_info: "딸기에서 자주 발생",
    stle_info: "잎 뒷면에 서식하며, 미세한 반점과 거미줄이 관찰될 수 있음,잎 뒷면에 서식하며, 미세한 반점과 거미줄이 관찰될 수 있음,잎 뒷면에 서식하며, 미세한 반점과 거미줄이 관찰될 수 있음,잎 뒷면에 서식하며, 미세한 반점과 거미줄이 관찰될 수 있음",
    ecology_info: "건조할수록 번식 빠름, 고온에서 발생 증가,건조할수록 번식 빠름, 고온에서 발생 증가,건조할수록 번식 빠름, 고온에서 발생 증가,건조할수록 번식 빠름, 고온에서 발생 증가,건조할수록 번식 빠름, 고온에서 발생 증가",
    damage_info: "잎 황화·잎말림, 수량 감소",
    qrant_info: "시설 내 확산이 빠르므로 초기 차단 중요",
    prevent_method: "초기 발견 시 바로 제거/방제, 잎 뒷면 관찰",
    biology_prvnbe_mth: "천적응애 투입",
    chemical_prvnbe_mth: "살비제 계열 교호 살포",
  },
  {
    insect_id: 103,
    distrb_info: "사과·복숭아 등 과수에서 발생",
    stle_info: "유충이 새순/과실 내부로 침입해 피해를 유발",
    ecology_info: "연 3~5회 발생 가능, 온도에 따라 세대수 변동",
    damage_info: "새순 고사, 과실 낙과 및 상품성 저하",
    qrant_info: "과수원 예찰 강화 필요",
    prevent_method: "월동처 제거, 예찰 후 적기 방제",
    biology_prvnbe_mth: "교미교란제, 유인트랩 활용",
    chemical_prvnbe_mth: "등록 약제 적기 살포",
  },
];

/** disease (기본) - 최소 */
export const DUMMY_DISEASES = [
  {
    disease_id: 201,
    crop_id: 10,
    sick_name_kor: "역병",
    sick_name_eng: "Late blight",
    sick_name_chn: "晚疫病",
  },
  {
    disease_id: 202,
    crop_id: 20,
    sick_name_kor: "점무늬낙엽병",
    sick_name_eng: "Marssonina blotch",
    sick_name_chn: "褐斑落葉病",
  },
  {
    disease_id: 203,
    crop_id: 30,
    sick_name_kor: "무름병",
    sick_name_eng: "Soft rot",
    sick_name_chn: "軟腐病",
  },
];

/** disease_detail (상세) - 최소 */
export const DUMMY_DISEASE_DETAILS = [
  {
    disease_id: 201,
    development_condition: "저온다습 환경에서 급속 확산,저온다습 환경에서 급속 확산,저온다습 환경에서 급속 확산,저온다습 환경에서 급속 확산",
    symptoms: "잎·줄기 암갈색 병반, 포자 형성,저온다습 환경에서 급속 확산,저온다습 환경에서 급속 확산,저온다습 환경에서 급속 확산,저온다습 환경에서 급속 확산,저온다습 환경에서 급속 확산",
    prevention_method: "배수 개선, 밀식 피하기, 예방 살포,배수 개선, 밀식 피하기, 예방 살포,배수 개선, 밀식 피하기, 예방 살포,배수 개선, 밀식 피하기, 예방 살포,배수 개선, 밀식 피하기, 예방 살포",
    biology_prvnbe_mth: "건전묘 사용, 윤작",
    chemical_prvnbe_mth: "등록 살균제 교호 살포",
    virus_name: "Phytophthora infestans",
  },
  {
    disease_id: 203,
    development_condition: "고온다습 + 저장 중 발생 가능",
    symptoms: "연화/부패, 악취 동반",
    prevention_method: "상처 방지, 저장 환경 관리",
    biology_prvnbe_mth: "선별 강화, 위생 관리",
    chemical_prvnbe_mth: "등록 약제 처리(필요 시)",
    virus_name: "Pectobacterium spp.",
  },
];
