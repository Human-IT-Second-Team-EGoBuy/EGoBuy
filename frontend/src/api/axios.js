// Axios 전역 설정. 주로 인증과 관련되어 토큰을 재발급 처리, 에러 핸들링을
import axios from 'axios';
import useUserStore from "@/store/useUserStore";

const apiClient = axios.create({
    /* Production 환경 -> Nginx가 프록시
     * Local 환경 -> Vite.config.js에서 프록시. 
     * 따라서 경로를 비워둠 
    * */
  baseURL: '/',     
  withCredentials: true,
});
let refreshPromise = null;

const isFormDataBody = (data) =>
  typeof FormData !== "undefined" && data instanceof FormData;

const deleteHeader = (headers, key) => {
  if (!headers) return;
  // axios v1: AxiosHeaders 객체일 수도 있음
  if (typeof headers.delete === "function") headers.delete(key);
  else {
    delete headers[key];
    delete headers[key.toLowerCase()];
    delete headers[key.toUpperCase()];
  }
};
// HMR(핫리로드)에서 인터셉터 중복 등록 방지
if (!apiClient.__MF_SETUP__) {
  apiClient.__MF_SETUP__ = true;

  // Request: 쿠키 인증이면 Authorization 헤더를 제거(혹시 어디선가 붙여도 무력화)
  apiClient.interceptors.request.use((config) => {
    config.headers = config.headers ?? {};

    // FormData면 Content-Type 지정하지 말기 (boundary 자동)
    if (isFormDataBody(config.data)) {
      deleteHeader(config.headers, "Content-Type");
    }

    // 쿠키 인증이면 Authorization 필요 없음
    deleteHeader(config.headers, "Authorization");

    return config;
  });

    /** success:false 를 AxiosError(401/403)로 변환 */
  apiClient.interceptors.response.use((res) => {
    const data = res?.data;

    if (data?.success === false) {
      const code = data?.error?.code || data?.error?.errorCode || "API_ERROR";
      const msg = data?.error?.message || "요청 실패";

      // 코드/메시지로 인증 오류 판정(프로젝트 상황에 맞게 넉넉히)
      const isUnauthorized =
        String(code).includes("UNAUTHORIZED") ||
        String(code).includes("AUTH") ||
        /인증|로그인|권한|접근/.test(msg);

      const isForbidden = String(code).includes("FORBIDDEN");

      const status = isForbidden ? 403 : isUnauthorized ? 401 : 400;

      // axios가 이해하는 AxiosError 형태로 만들기
      let err;
      if (axios.AxiosError) {
        err = new axios.AxiosError(
          msg,
          code,
          res.config,
          res.request,
          { ...res, status, data } // <-- 여기 status를 심는 게 핵심!
        );
      } else {
        err = new Error(msg);
        err.isAxiosError = true;
        err.config = res.config;
        err.request = res.request;
        err.response = { ...res, status, data };
        err.code = code;
      }

      if (status === 401) err.__AUTH_REQUIRED__ = true;
      err.data = data;

      return Promise.reject(err);
    }

    return res;
  });

  /** 401이면 refresh 1회 시도 후 원요청 재시도 */
  apiClient.interceptors.response.use(
    (res) => res,
    async (error) => {
      const cfg = error?.config;
      if (!cfg) return Promise.reject(error);

      const url = cfg.url || "";

      // 로그인/리프레시 요청은 재시도 금지
      if (url.includes("/api/login") || url.includes("/api/auth/refresh")) {
        return Promise.reject(error);
      }

      if (error?.response?.status === 401 && !cfg._retry) {
        cfg._retry = true;

        try {
          // 동시에 여러 401이어도 refresh는 1번만
          if (!refreshPromise) {
            refreshPromise = apiClient
              .post("/api/auth/refresh")
              .finally(() => (refreshPromise = null));
          }

          await refreshPromise;
          return apiClient(cfg);
        } catch (refreshError) {
          useUserStore.getState().clearUser();
          refreshError.__AUTH_REQUIRED__ = true;
          return Promise.reject(refreshError);
        }
      }

      return Promise.reject(error);
    }
  );


}

export default apiClient;