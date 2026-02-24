// UserController API 
import apiClient from './axios';



// userAuthId로 회원 정보 조회
export const getUserProfile = async () => {
  const response = await apiClient.get('/api/users/userProfile', {
    withCredentials: true // 쿠키 사용을 위해 추가
  });
  return response.data; // ResponseDTO 객체 반환
};

 // 일반 로그아웃 ( Cookie 삭제 )
  export const logout = async () => {
    const response = await apiClient.post('/api/users/auth/logout', {}, {
        withCredentials: true 
    });
    return response.data;
};
  // api/login ( 일반 로그인 )

  // 네이버 로그인

  // 카카오 로그인

  // 구글 로그인 



  
  // 아이디 찾기 

  // 비빌먼호 찾기

