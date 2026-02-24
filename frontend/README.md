로그인 요청: apiClient.post('/login') 호출.

서버 응답: 서버가 Set-Cookie로 토큰을 구워줌 + 바디에 유저 정보(JSON)를 담아 보냄.

성공 처리: 프론트엔드에서 응답 바디에 있는 유저 정보를 **Zustand(setUser)**에 저장.

권한 요청: 이후 게시글 작성 등을 할 때, Axios 인터셉터는 아무것도 안 해도 쿠키를 실어 보냄.

새로고침 시: Zustand 데이터가 날아가므로, App.jsx의 useEffect 등에서 "현재 내 정보 조회 API"(/api/users/me)를 한 번 호출해서 Zustand를 다시 채워줌.


---

필요한 라이브러리 
npm install lucide-react
npm unstall zustand