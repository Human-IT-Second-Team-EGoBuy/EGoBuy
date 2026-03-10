CLASSES = [
  "Apple___alternaria_leaf_spot",
  "Apple___black_rot",
  "Apple___brown_spot",
  "Apple___gray_spot",
  "Apple___healthy",
  "Apple___rust",
  "Apple___scab",
  "Corn___common_rust",
  "Corn___gray_leaf_spot",
  "Corn___healthy",
  "Corn___northern_leaf_blight",
  "Grape__black_rot",
  "Grape__esca",
  "Grape__healthy",
  "Grape__leaf_blight",
  "Potato___bacterial_wilt",
  "Potato___early_blight",
  "Potato___healthy",
  "Potato___late_blight",
  "Potato___leafroll_virus",
  "Potato___mosaic_virus",
  "Potato___nematode",
  "Potato___pests",
  "Potato___phytophthora",
  "Rice___bacterial_blight",
  "Rice___blast",
  "Rice___brown_spot",
  "Rice___tungro",
  "Strawberry___healthy",
  "Strawberry___leaf_scorch",
  "Strawberry__angular_leafspot",
  "Strawberry__anthracnose_fruit_rot",
  "Strawberry__blossom_blight",
  "Strawberry__gray_mold",
  "Strawberry__leaf_spot",
  "Strawberry__powdery_mildew_fruit",
  "Strawberry__powdery_mildew_leaf",
  "Tomato__bacterial_spot",
  "Tomato__early_blight",
  "Tomato__healthy",
  "Tomato__late_blight",
  "Tomato__leaf_mold",
  "Tomato__mosaic_virus",
  "Tomato__septoria_leaf_spot",
  "Tomato__target_spot",
  "Tomato__twospotted_spider_mite",
  "Tomato__yellow_leaf_curl_virus",
  "Wheat__brown_rust",
  "Wheat__healthy",
  "Wheat__loose_smut",
  "Wheat__septoria",
  "Wheat__yellow_rust",
  ]



CLASSES_KO = {
    # Apple
  "Apple___alternaria_leaf_spot": "사과 알터나리아 잎반점병",
  "Apple___black_rot": "사과 검은썩음병(Black rot)",
  "Apple___brown_spot": "사과 갈색무늬병(brown spot)",
  "Apple___gray_spot": "사과 회색무늬병(gray spot)",
  "Apple___healthy": "사과 건강함",
  "Apple___rust": "사과 녹병(rust)",
  "Apple___scab": "사과 검은별무늬병(scab)",

  # Corn
  "Corn___common_rust": "옥수수 녹병(common rust)",
  "Corn___gray_leaf_spot": "옥수수 점무늬병(회색잎무늬병, gray leaf spot)",
  "Corn___healthy": "옥수수 건강함",
  "Corn___northern_leaf_blight": "옥수수 북부잎마름병(northern leaf blight)",

  # Grape
  "Grape__black_rot": "포도 검은썩음병(black rot)",
  "Grape__esca": "포도 에스카병(블랙미즐, esca)",
  "Grape__healthy": "포도 건강함",
  "Grape__leaf_blight": "포도 잎마름병(leaf blight)",

  # Potato
  "Potato___bacterial_wilt": "감자 풋마름병(세균성 시들음병, bacterial wilt)",
  "Potato___early_blight": "감자 겹무늬병(early blight)",
  "Potato___healthy": "감자 건강함",
  "Potato___late_blight": "감자 역병(late blight)",
  "Potato___leafroll_virus": "감자 잎말림바이러스병(leafroll virus)",
  "Potato___mosaic_virus": "감자 모자이크바이러스병(mosaic virus)",
  "Potato___nematode": "감자 선충 피해(nematode)",
  "Potato___pests": "감자 해충 피해(pests)",
  "Potato___phytophthora": "감자 피토프토라(Phytophthora) 피해",

  # Rice
  "Rice___bacterial_blight": "벼 흰잎마름병(bacterial blight)",
  "Rice___blast": "벼 도열병(blast)",
  "Rice___brown_spot": "벼 깨씨무늬병(brown spot)",
  "Rice___tungro": "벼 퉁그로병(tungro)",

  # Strawberry
  "Strawberry___healthy": "딸기 건강함",
  "Strawberry___leaf_scorch": "딸기 잎마름병(leaf scorch)",
  "Strawberry__angular_leafspot": "딸기 각무늬병(angular leaf spot)",
  "Strawberry__anthracnose_fruit_rot": "딸기 탄저병(과실썩음, anthracnose fruit rot)",
  "Strawberry__blossom_blight": "딸기 꽃마름병(blossom blight)",
  "Strawberry__gray_mold": "딸기 잿빛곰팡이병(gray mold)",
  "Strawberry__leaf_spot": "딸기 잎반점병(leaf spot)",
  "Strawberry__powdery_mildew_fruit": "딸기 흰가루병(과실, powdery mildew fruit)",
  "Strawberry__powdery_mildew_leaf": "딸기 흰가루병(잎, powdery mildew leaf)",

  # Tomato
  "Tomato__bacterial_spot": "토마토 세균성반점병(bacterial spot)",
  "Tomato__early_blight": "토마토 겹무늬병(early blight)",
  "Tomato__healthy": "토마토 건강함",
  "Tomato__late_blight": "토마토 역병(late blight)",
  "Tomato__leaf_mold": "토마토 잎곰팡이병(leaf mold)",
  "Tomato__mosaic_virus": "토마토 모자이크바이러스병(mosaic virus)",
  "Tomato__septoria_leaf_spot": "토마토 점무늬병(septoria leaf spot)",
  "Tomato__target_spot": "토마토 표적무늬병(target spot)",
  "Tomato__twospotted_spider_mite": "토마토 점박이응애 피해(two-spotted spider mite)",
  "Tomato__yellow_leaf_curl_virus": "토마토 황화잎말림바이러스병(YLCV)",

  # Wheat
  "Wheat__brown_rust": "밀 붉은녹병(brown rust)",
  "Wheat__healthy": "밀 건강함",
  "Wheat__loose_smut": "밀 헛깜부기병(loose smut)",
  "Wheat__septoria": "밀 셉토리아 잎마름병(septoria)",
  "Wheat__yellow_rust": "밀 황색녹병(yellow rust)",

  # Invalid fallback key (혹시 REJECT 등에서 쓸 때)
  "Invalid": "결과 없음",

}

def to_korean(label_en: str) -> str:
    return CLASSES_KO.get(label_en, label_en)

CROP_ID_PREFIXES: dict[int, list[str]] = {
    24: ["Apple___"],
    59: ["Corn___"],
    106: ["Grape__"],
    64: ["Potato___"],
    20: ["Rice___"],
    4: ["Strawberry__"],   
    16: ["Tomato__"],
    14: ["Wheat__"],
}

def allowed_indices_for_crop_id(crop_id: int | None) -> list[int] | None:
    """
    crop_id에 해당하는 라벨 인덱스 목록 반환.
    - 매핑이 없거나 crop_id가 None이면 None(=필터링 안함)
    """
    if crop_id is None:
        return None
    prefixes = CROP_ID_PREFIXES.get(int(crop_id))
    if not prefixes:
        return None

    out: list[int] = []
    for i, label in enumerate(CLASSES):
        if any(label.startswith(p) for p in prefixes):
            out.append(i)
    return out or None

#  라벨 누락/오타 방지 체크
_missing = [c for c in CLASSES if c not in CLASSES_KO]
if _missing:
    print(f"[WARN] CLASSES_KO에 한글 매핑이 없는 라벨 {len(_missing)}개:", _missing[:5], "...")