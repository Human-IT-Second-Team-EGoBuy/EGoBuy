# app/infer/labels_wambugu.py

WAMBUGU_CLASS_KO = {
    # Corn
    "Corn___Common_Rust": "옥수수 녹병",
    "Corn___Gray_Leaf_Spot": "옥수수 점무늬병(회색잎무늬병)",
    "Corn___Healthy": "옥수수 건강함",

    # Potato
    "Potato___Early_Blight": "감자 겹둥근무늬병(early blight)",
    "Potato___Late_Blight": "감자 역병(late blight)",
    "Potato___Healthy": "감자 건강함",

    # Rice
    "Rice___Brown_Spot": "벼 깨씨무늬병(brown spot)",
    "Rice___Leaf_Blast": "벼 잎도열병(leaf blast)",
    "Rice___Healthy": "벼 건강함",

    # Wheat
    "Wheat___Brown_Rust": "밀 붉은녹병(잎녹병, brown rust)",
    "Wheat___Yellow_Rust": "밀 황색녹병(줄무늬녹병, yellow rust)",
    "Wheat___Healthy": "밀 건강함",

    # Invalid
    "Invalid": "결과 없음",
}

def to_korean(label_en: str) -> str:
    return WAMBUGU_CLASS_KO.get(label_en, label_en)
