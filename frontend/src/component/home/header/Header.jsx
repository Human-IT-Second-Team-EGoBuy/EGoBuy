import { useNavigate, useLocation } from "react-router-dom";
import HeaderUi from "./HeaderUi";
import { useState, useMemo, useEffect } from "react"
import Dropdown from "../dropdown/Dropdown";
import useUserStore from "../../../store/useUserStore";
import apiClient from "@/api/axios";
import { getUserProfile } from "@/api/users/users"; // 함수 추가

/**
 * 상단 네비게이션의 카테고리 정의
 * - 렌더링은 해당 배열 기준으로 map 처리함
 * - 정의할 카테고리 추가/삭제 시 해당 배열만 변경
 */
const NAV_MENU = [
    { id: "map", label: "농업지도", path: "/map" },
    { id: "community", label: "커뮤니티", path: "/community" },
    { id: "informationhub", label: "정보허브", path: "/insect-pests-info" }
]

export default function Header() {
    const navigate = useNavigate();
    // 라우트된 현재 pathname 만 사용, active 판정용
    const { pathname } = useLocation();

    // hover된 메뉴 id값 사용하여 드롭다운
    const [activeMenu, setActiveMenu] = useState(null);

    // 인증 여부에 따라 보여줄 UI를 동적으로 구현하기 위해 Zustand에서 상태와 로그아웃 함수 추가
    const { isAuthenticated, clearUser, setUser, user, isLoaded } = useUserStore();


    // 앱 로드 시 로그인 상태 복구 ( 새로고침 시 Zustand 메모리 초기화 방어 )
    useEffect(() => {
        const initAuth = async () => {
            try {
                // 파라미터 없이 호출 (백엔드 SecurityContextHolder에서 처리)
                const response = await getUserProfile(); 
                
                if (response.success) {
                    setUser(response.content);
                    console.log("로그인 정보 복구 성공:", response.content.nickname);
                }
            } catch (error) {
                console.log("비로그인 상태 또는 세션 만료");
                clearUser();
            }
        };

        initAuth();
    }, [setUser, clearUser]); // 의존성 배열 추가 ( 해당 값이 변경될 때 실행 )


    // 로그아웃 핸들러
    const handleLogout = async () => {
        try {
            // 쿠키 삭제
            await apiClient.post('/api/users/auth/logout',
                 {},
                 { withCredentials: true });
        } catch (error) {
            console.error("로그아웃 실패:", error);
        } finally {
            // 무조건 프론트엔드 상태는 초기화
            clearUser(); 
            alert('로그아웃 되었습니다.');
            navigate('/');
        }
    };

    /**
     * 드롭다운 메뉴 정의
     * - NAV_MENU id와 매칭되어야 함
     */
    const dropdownContents = useMemo(() => ({
        map: [
           
        ],
        community: [{ name: "게시판", path: "/community" }],
        informationhub: [
            { name: "병충해 정보", path: "/insect-pests-info" },
            { name: "농산물 가격 정보", path: "/retail-detail-info"}
        ],
        

    }), []);

    // 메뉴 active 판정 후 스타일 적용
    const getStyleMenu = (path) => {
        const base = "transition-all duration-200";
        const active = pathname.startsWith(path)
            ? "text-emerald-700 dark:text-emerald-300 border-b-2 border-emerald-500 font-bold"
            : "text-slate-600 hover:text-emerald-600";
        return `${base} ${active}`;
    };
    // 챗봇
    const getHeaderStyleAiChat = () => {
        const base = "transition-all duration-200";
        const active = pathname.startsWith("/aiChat")
            ? "text-emerald-700 border-b-2 border-emerald-500 font-bold"
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
                    <HeaderUi variant="text" onClick={() => navigate("/ai-chat")} className={getHeaderStyleAiChat()}>
                        AI챗봇
                    </HeaderUi>         
                </div>
                

            </div>

             {/*  로그인 여부에 따른 UI (조건부 렌더링) */}
            <div className="flex items-center gap-3 min-w-[150px] justify-end">
                {/* 로딩이 완료(isLoaded === true)되었을 때만 UI 나타냄 */}
                {isLoaded && (
                    isAuthenticated ? (
                        <>
                            <span className="text-xs text-slate-500 font-medium">
                                <b>{user?.nickname}</b>님
                            </span>
                            {user?.user_role === "ADMIN" ? (
                            <HeaderUi onClick={() => navigate("/admin")} variant="ghost">관리자페이지</HeaderUi>
                            ) : (
                            <HeaderUi onClick={() => navigate("/mypage")} variant="ghost">마이페이지</HeaderUi>
                            )}
                            <HeaderUi onClick={handleLogout} variant="ghost">로그아웃</HeaderUi>
                        </>
                    ) : (
                        <>
                            <HeaderUi onClick={() => navigate("/login")} variant="ghost">로그인</HeaderUi>
                            <HeaderUi onClick={() => navigate("/signup")} variant="ghost">회원가입</HeaderUi>
                        </>
                    )
                )}
                
                {/* 로딩 중(isLoaded === false)일 때는 아무것도 렌더링하지 않거나 
                    레이아웃 깨짐 방지를 위해 빈 div만 유지합니다. */}
                {!isLoaded && <div className="h-9" />} 
            </div>
        </nav>
    );
}
