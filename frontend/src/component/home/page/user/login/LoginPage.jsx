import React, { useState } from 'react';
import { User, Lock, Eye, EyeOff } from 'lucide-react'; // 아이콘 라이브러리 (npm install lucide-react)
import { useNavigate } from 'react-router-dom'; // 로그인 성공 시 페이지 이동을 위해 추가
import './LoginPage.css';
import axios from 'axios';
import useUserStore from '../../../../../store/useUserStore'; // 로그인 시 Zustand로 전역 상태 관리를 하기 위해 추가.
import { getUserProfile } from '../../../../../api/users';  // 사용할 함수를 users.js에서 추가
import { socialLogin } from '../../../../../api/social';

export default function LoginPage() {
  const [userId, setUserId] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);

  const navigate = useNavigate(); // ("/")로 가도록 하기 위해 추가
  const { setUser } = useUserStore(); // Zustand에서 setUser 함수 꺼내오기

  // 1. 일반 로그인
  const handleLogin = async (e) => {
    e.preventDefault(); 
    
    try {
      // 로그인 정보 전달
      const response = await axios.post('/api/login', {
        user_auth_id: userId,
        password: password
      }, {
        // 백엔드에서 내려주는 HttpOnly 쿠키를 브라우저에 저장하기 위해 추가.
        withCredentials: true 
      });

      // 로그인 시 Zustand에 User 객체 정보를 담기 위해 API 추가 호출
      const profileResponse = await getUserProfile();

      if (profileResponse.success) {
        const userData = profileResponse.content; // ResponseDTO의 실제 데이터 필드

        // 받아온 필드를 모두 추가
        setUser(userData); 

        console.log('상세 프로필 저장 완료:', userData);
        navigate('/');
      }

    } catch (error) {
      console.error('로그인/프로필 로드 에러:', error);
      alert('로그인 정보가 올바르지 않거나 로그인 처리중 에러가 발생하였습니다.');
    }
  };



  // 소셜 로그인 핸들러
  const handleSocialLogin = (provider) => {
    // 각 소셜 로그인 제공자의 OAuth2 인증 URL로 리다이렉트
    socialLogin(provider)
    console.log(`${provider} 소셜 로그인 시도`);
  };

  return (
    <div className="login-container">
      <div className="login-wrapper">
        
        {/* 헤더 영역 */}
        <div className="login-header">
          <h1 className="login-logo">MateFarm</h1>
          <h2 className="login-title">환영합니다!</h2>
          <p className="login-subtitle">서비스 이용을 위해 로그인을 해주세요.</p>
        </div>

        {/* 폼 영역 */}
        <div className="login-card">
          <form className="login-form" onSubmit={handleLogin}>
            
            {/* 이메일 입력 */}
            <div className="input-group">
              <label htmlFor="userId">아이디</label>
              <div className="input-wrapper">
                <div className="icon-left">
                  <User size={18} />
                </div>
                <input
                  type="text" // email로 형식 검사 하지 않음.
                  id="userId"
                  className="input-field"
                  placeholder="example@email.com"
                  value={userId}
                  onChange={(e) => setUserId(e.target.value)}
                  required
                />
              </div>
            </div>

            {/* 비밀번호 입력 */}
            <div className="input-group">
              <label htmlFor="password">비밀번호</label>
              <div className="input-wrapper">
                <div className="icon-left">
                  <Lock size={18} />
                </div>
                <input
                  type={showPassword ? 'text' : 'password'}
                  id="password"
                  className="input-field has-right-icon"
                  placeholder="비밀번호를 입력하세요"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
                <button
                  type="button"
                  className="password-toggle"
                  onClick={() => setShowPassword(!showPassword)}
                  aria-label={showPassword ? "비밀번호 숨기기" : "비밀번호 표시"}
                >
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
            </div>

            {/* 로그인 옵션 */}
            <div className="login-options">
              <div className="forgot-id">
                <a href="/forgot-id" className="forgot-id">
                아이디 찾기
                </a>

              </div>
              <a href="/forgot-password" className="forgot-password">
                비밀번호 찾기
              </a>
            </div>

            {/* 로그인 버튼 */}
            <button type="submit" className="btn-primary">
              로그인
            </button>
          </form>

          {/* 소셜 로그인 구분선 */}
          <div className="social-divider">
            <div className="divider-line"></div>
            <div className="divider-text-wrapper">
              <span className="divider-text">또는 다음으로 로그인</span>
            </div>
          </div>

{/* 소셜 로그인 버튼 그룹 */}
          <div className="social-group">
            {/* 카카오 로그인 */}
            <button 
              type="button" 
              className="btn-social btn-kakao"
              onClick={() => handleSocialLogin('kakao')}  // 클릭 시 handleSocialLogin에 파라미터 값으로 전달
              aria-label="카카오로 로그인"
            >
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 3C6.477 3 2 6.477 2 10.778c0 2.768 1.83 5.176 4.582 6.477-.184.693-.668 2.55-.765 2.932-.122.482.17.472.36.34 0 0 2.457-1.63 3.424-2.28 1.442.223 3.05.342 4.4.342 5.523 0 10-3.477 10-7.778C24 6.477 19.523 3 12 3z" fill="#000000"/>
              </svg>
            </button>

            {/* 네이버 로그인 */}
            <button 
              type="button" 
              className="btn-social btn-naver"
              onClick={() => handleSocialLogin('naver')}
              aria-label="네이버로 로그인"
            >
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M16.273 12.845 7.376 0H0v24h7.727V11.155L16.624 24H24V0h-7.727v12.845z" fill="#FFFFFF"/>
              </svg>
            </button>

            {/* 구글 로그인 */}
            <button 
              type="button" 
              className="btn-social btn-google"
              onClick={() => handleSocialLogin('google')}
              aria-label="구글로 로그인"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
                <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
                <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
                <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
              </svg>
            </button>
          </div>
        </div>

        {/* 푸터 영역 */}
        <div className="login-footer">
          계정이 없으신가요? 
          <a href="/signup" className="signup-link">
            회원가입하기
          </a>
        </div>
      </div>
    </div>
  );
}