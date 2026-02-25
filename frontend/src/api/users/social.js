// Social Login/Logout 에 사용될 백엔드 API 모음
import { RouterProvider, useParams } from 'react-router-dom';
import apiClient from '../axios';
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

/**
 * provider에 따라 소셜 로그인 페이지로 이동
 * @param {string} provider - naver, kakao, google
* */
export const socialLogin = (provider) => {
    // Backend의 /api/user/oauth2/naver,kakao,google 로 이동
    window.location.href = `${API_BASE_URL}/api/users/oauth2/${provider}`;
}
