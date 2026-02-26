import { Routes, Route } from "react-router-dom"
import Home from "./Home"
import MainPage from "./page/main/MainPage"
import MapPage from "./page/map/MapPage"
import PestsPage from "./page/pests/PestsPage"
import CropsPage from "./page/crops/CropsPage"
import CommunityPage from "./page/community/CommunityPage"
import AiChatPage from "./page/aichat/AiChatPage";
import InsectPestsInfoPage from "./page/informationhub/InsectPestsInfoPage";
import InsectDetailPage from "./page/informationhub/InsectDetailPage";
import DiseaseDetailPage from "./page/informationhub/DiseaseDetailPage";
import RetailDetailPage from "./page/informationhub/retaildetailpage/RetailDetailPage"
import LoginPage from "./page/user/auth/login/loginPage"
import MyPage from "./page/user/mypage/MyPage"
import SignupPage from "./page/user/auth/signup/SignUpPage"



/**
 * 페이지 라우터
 * - Home 컴포넌트는 레이아웃으로 사용
 * - 헤더 하위 페이지는 <Outlet /> 통해 렌더링함 
 */
export default function PageRouter() {
    return (
        <Routes>
            {/* 공통 레이아웃 라우트 */}
            <Route element={<Home />}>

                {/* 메인 페이지 */}
                <Route index element={<MainPage />} />

                {/* 카테고리별 페이지 */}
                <Route path="/map" element={<MapPage />} />
                <Route path="/pests" element={<PestsPage />} />
                <Route path="/crops" element={<CropsPage />} />
                <Route path="/community" element={<CommunityPage />} />
                <Route path="/insect-pests-info" element={<InsectPestsInfoPage />} />
                <Route path="/insect-pests-info/insects/:insectId" element={<InsectDetailPage />} />
                <Route path="/insect-pests-info/diseases/:diseaseId" element={<DiseaseDetailPage />} />
                <Route path="/ai-chat" element={<AiChatPage />} />
                <Route path="/retail-detail-info" element={<RetailDetailPage />} />
                <Route path="/ai-chat" element={<AiChatPage />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/mypage" element={<MyPage />} />
                <Route path="/signup" element={<SignupPage />} />

                </Route>
        </Routes>
    )
}
