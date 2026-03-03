// UserController API 
import apiClient from '@/api/axios';



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

  // 일반 회원가입
  export const signupNormal = async (formData) => {
  try {
    // CamelCase -> snake_case 로 변환 후 API 호출
  const RequestUserRegistVO = {
    user_auth_id: formData.userAuthId,
    user_name: formData.userName,
    password: formData.password,
    nickname: formData.nickname,
    email: formData.email,
    signup_path: formData.signupPath,
  }
    
    const response = await apiClient.post(
      '/api/users/signup/normal',
      RequestUserRegistVO
    )

    return response.data
  } catch (error) {
    console.error('회원가입 실패:', error)
    throw error
  }
}


//2. 아이디 중복 검사
export const checkAuthIdDuplicate = async (userAuthId) => {
    try {
        const response = await apiClient.get('/api/users/validate/authId',  // RequestParam
        {
            params: { user_auth_id: userAuthId }
        }
    );
        // console.log(response.data.content.exist)  await 을 붙이지 않을 시 promise 객체로 넘어오는 것을 확인하기 위한 로그
        return response.data.content.exist;
    } catch (error) {
        console.error("아이디 중복 검사 실패:", error);
        throw error;
    }
};


/**
 * 3. 닉네임 중복 검사
 * @param {string} nickname 
 */
export const checkNicknameDuplicate = async (nickname) => {
    try {
        const response = await apiClient.get('/api/users/validate/nickname',
        {
            params: { nickname: nickname }
        }
      );
        return response.data.content.exist
    } catch (error) {
        console.error("닉네임 중복 검사 실패:", error);
        throw error;
    }
};
  
  // 아이디 찾기 

  // 비빌먼호 찾기

