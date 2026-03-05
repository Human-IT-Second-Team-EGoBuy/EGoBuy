import { useNavigate } from "react-router-dom";
import { Card } from "../MainPageUi";
import CropSelector from "../../aichat/components/CropSelector";
import { CROP_ITEMS } from "../../aichat/constants";
import "../../aichat/aichat.css";
import { useEffect, useMemo, useRef, useState } from "react";
export default function VisionSection() {
  const navigate = useNavigate();

  const [cropId, setCropId] = useState(null);

  const cropName = useMemo(() => {
    return CROP_ITEMS.find((x) => Number(x.crop_id) === Number(cropId))?.crop_name ?? "";
  }, [cropId]);

  const goDiagnose = () => {
    // 선택 안 해도 이동 가능
    if (cropId == null) return navigate("/ai-chat?mode=vision");

    // 선택했으면 cropId도 함께 넘겨서 진단 페이지에서 기본 선택되게
    navigate(`/ai-chat?mode=vision&cropId=${cropId}`);
  };

  return (
    <Card className="p-5">
      <div className="flex items-start justify-between gap-3 mb-3">
        <div>
          <h2 className="font-bold text-lg">병충해 진단 AI</h2>
          <p className="text-sm text-slate-500 mt-1">
            사진을 업로드하면 작물 병충해를 진단해 드려요.
          </p>
        </div>

        <span className="text-xs px-2 py-1 rounded-full bg-slate-100 text-slate-600">
          Beta
        </span>
      </div>

      {/* 메인에서는 “미리보기”로 일부만 보여주기 */}
      <CropSelector
        cropId={cropId}
        cropName={cropName}
        items={CROP_ITEMS.slice(0, 8)}
        onSelect={(id) => setCropId(Number(id))}
      />

      <div className="mt-4 flex justify-center">
        <button
          type="button"
          className="mx-auto block w-full sm:w-72 rounded-xl py-3 font-semibold bg-emerald-600 text-white hover:opacity-95"
          onClick={goDiagnose}
        >
          {cropId == null ? "바로 진단하러 가기" : `${cropName} 진단하러 가기`}
        </button>
      </div>

      <div className="mt-3 text-xs text-slate-400">
        * 작물 선택은 선택사항이며, 진단 페이지에서 언제든 변경할 수 있어요.
      </div>
    </Card>
  );
}