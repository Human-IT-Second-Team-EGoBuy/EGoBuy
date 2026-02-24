// Axios 전역 설정. 주로 인증과 관련되어 토큰을 재발급 처리, 에러 핸들링을
import axios from 'axios';

const apiClient = axios.create({
    /* Production 환경 -> Nginx가 프록시
     * Local 환경 -> Vite.config.js에서 프록시. 
     * 따라서 경로를 비워둠 
    * */
  baseURL: '/',     
  withCredentials: true,
});

// Interceptor - 401 에러 시 토큰 갱신
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401 && !error.config._retry) {
      error.config._retry = true;
      
      try {
        // RefreshToken으로 새 AccessToken 받기
        await apiClient.post('/api/auth/refresh');
        
        // 원래 요청 재시도
        return apiClient(error.config);
      } catch (refreshError) {
        // Refresh 실패 시 로그인 페이지로
        useUserStore.getState().clearUser();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }
    return Promise.reject(error);
  }
);


export default apiClient;