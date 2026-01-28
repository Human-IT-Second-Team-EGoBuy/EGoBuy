import { useNavigate, useLocation } from "react-router-dom";
import HeaderUi from "./HeaderUI";
import { useState, useMemo } from "react"
import Dropdown from "../dropdown/Dropdown";

/**
 * 상단 네비게이션의 카테고리 정의
 * - 렌더링은 해당 배열 기준으로 map 처리함
 * - 정의할 카테고리 추가/삭제 시 해당 배열만 변경
 */
const NAV_MENU = [
    { id: "map", label: "농업지도", path: "/map" },
    { id: "crops", label: "작물백과", path: "/crop-tech" },
    { id: "community", label: "커뮤니티", path: "/community" },
]

export default function Header() {
    const navigate = useNavigate();
    // 라우트된 현재 pathname 만 사용, active 판정용
    const { pathname } = useLocation();

    // hover된 메뉴 id값 사용하여 드롭다운
    const [activeMenu, setActiveMenu] = useState(null);

    /**
     * 드롭다운 메뉴 정의
     * - NAV_MENU id와 매칭되어야 함
     */
    const dropdownContents = useMemo(() => ({
        map: [
            { name: "토지 매매가 지도", path: "/map/trade" },
            { name: "야생동물 서식지 지도", path: "/map/animal" },
            { name: "영농 정착 정책 지원 지역", path: "/map/policy" },
            { name: "지역별 토양 분석 지도", path: "/map/soil" },
        ],
        crops: [{ name: "품종별 재배기술", path: "/crop-tech" }],
        community: [{ name: "게시판", path: "/community" }]
    }), []);

    // 메뉴 active 판정 후 스타일 적용
    const getStyleMenu = (path) => {
        const base = "transition-all duration-200";
        const active = pathname.startsWith(path)
            ? "text-emerald-700 dark:text-emerald-300 border-b-2 border-emerald-500 font-bold"
            : "text-slate-600 hover:text-emerald-600";
        return `${base} ${active}`;
    };

    return (
        <nav className="relative bg-white border-b border-slate-200 px-6 py-3 flex justify-between items-center z-50 shadow-sm">
            <div className="flex items-center gap-8">
                <div className="flex items-center gap-2 cursor-pointer" onClick={() => navigate("/")}>
                    <span className="text-xl font-bold text-emerald-600">mateFarm</span>
                </div>

                {/* 로고 영역 - 홈 이동 */}
                <div className="hidden md:flex gap-8">
                    <HeaderUi
                        variant="text"
                        onClick={() => navigate("/")}
                        className={getStyleMenu("/")}
                    >
                        홈
                    </HeaderUi>

                    {/* 네비게이션 메뉴 */}
                    {NAV_MENU.map((menu) => (
                        <div
                            key={menu.id}
                            className="relative group"
                            onMouseEnter={() => setActiveMenu(menu.id)}
                            onMouseLeave={() => setActiveMenu(null)}
                        >
                            <HeaderUi
                                variant="text"
                                className={getStyleMenu(menu.path)}
                                onClick={() => navigate(menu.path)}
                            >
                                {menu.label}
                            </HeaderUi>

                            {activeMenu === menu.id && (
                                <Dropdown
                                    items={dropdownContents[menu.id]}
                                    selectTeb={(path) => {
                                        setActiveMenu(null);
                                        navigate(path);
                                    }}
                                />
                            )}
                        </div>
                    ))}

                </div>

            </div>

             {/* 인증 관련 액션 */}
            <div>
                <HeaderUi onClick={() => navigate("/login")} variant="ghost">로그인</HeaderUi>
                <HeaderUi onClick={() => navigate("/signup")} variant="ghost">회원가입</HeaderUi>
            </div>
        </nav>
    );
}
