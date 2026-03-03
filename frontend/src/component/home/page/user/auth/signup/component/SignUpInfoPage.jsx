import React, { useState, useEffect } from 'react';
import { User, Lock, Mail, Sparkles, CheckCircle, AlertCircle, Clock } from 'lucide-react';
import InputRow from '@/component/home/page/user/auth/signup/common/InputRow';
import StatusBadge from '@/component/home/page/user/auth/signup/common/StatusBadge';
import { signupNormal, checkAuthIdDuplicate, checkNicknameDuplicate } from '@/api/users/users';
import { 
  checkEmailDuplicate,
  sendVerificationEmail, 
  verifyEmailCode 
} from '@/api/users/email'; 

export default function SignUpInfoPage({ onNext, onBack }) {
  const [infoData, setInfoData] = useState({
    userAuthId: '',
    userName: '',
    password: '',
    confirmPassword: '',
    nickname: '',
    email: '',
    signupPath: 'NORMAL'
  });

  // 중복 검사 상태
  const [validation, setValidation] = useState({
    userAuthId: null,
    nickname: null,
    email: null
  });

  // 이메일 인증 관련 상태
  const [emailVerification, setEmailVerification] = useState({
    codeSent: false,
    code: '',
    isVerified: false,
    timer: 0,
    isTimerActive: false
  });

  const [loading, setLoading] = useState({
    userAuthId: false,
    nickname: false,
    email: false,
    emailCode: false,
    submit: false
  });

  // 타이머 (5분 = 300초)
  useEffect(() => {
    let interval;
    if (emailVerification.isTimerActive && emailVerification.timer > 0) {
      interval = setInterval(() => {
        setEmailVerification(prev => ({
          ...prev,
          timer: prev.timer - 1
        }));
      }, 1000);
    } else if (emailVerification.timer === 0 && emailVerification.isTimerActive) {
      setEmailVerification(prev => ({
        ...prev,
        isTimerActive: false,
        codeSent: false
      }));
      alert('인증 시간이 만료되었습니다. 다시 시도해주세요.');
    }
    return () => clearInterval(interval);
  }, [emailVerification.isTimerActive, emailVerification.timer]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setInfoData((prev) => ({ ...prev, [name]: value }));

    // 입력값이 변경되면 해당 필드의 검증 상태 초기화
    if (name === 'userAuthId') {
      setValidation(prev => ({ ...prev, userAuthId: null }));
    } else if (name === 'nickname') {
      setValidation(prev => ({ ...prev, nickname: null }));
    } else if (name === 'email') {
      setValidation(prev => ({ ...prev, email: null }));
      setEmailVerification({
        codeSent: false,
        code: '',
        isVerified: false,
        timer: 0,
        isTimerActive: false
      });
    }
  };

  // 아이디 중복 확인
  const handleCheckUserAuthId = async () => {
    if (!infoData.userAuthId || infoData.userAuthId.length < 4) {
      alert('아이디는 4자 이상 입력해주세요.');
      return;
    }

    setLoading(prev => ({ ...prev, userAuthId: true }));
    try {
      const result = await checkAuthIdDuplicate(infoData.userAuthId);
      // result가 true면 중복, false면 사용가능
      const isAvailable = !result;
      
      setValidation(prev => ({
        ...prev,
        userAuthId: isAvailable
      }));

      if (isAvailable) {  
        alert('사용 가능한 아이디입니다.');
      } else {
        alert('이미 사용 중인 아이디입니다.');
      }
    } catch (error) {
      console.error('아이디 중복 확인 오류:', error);
      alert('아이디 중복 확인에 실패했습니다.');
    } finally {
      setLoading(prev => ({ ...prev, userAuthId: false }));
    }
  };

  // 닉네임 중복 확인
  const handleCheckNickname = async () => {
    if (!infoData.nickname || infoData.nickname.length < 1) {
      alert('닉네임은 한 글자 이상 입력해주세요.');
      return;
    }

    setLoading(prev => ({ ...prev, nickname: true }));
    try {
      const result = await checkNicknameDuplicate(infoData.nickname);
      const isAvailable = !result;
      
      setValidation(prev => ({
        ...prev,
        nickname: isAvailable
      }));

      if (isAvailable) {
        alert('사용 가능한 닉네임입니다.');
      } else {
        alert('이미 사용 중인 닉네임입니다.');
      }
    } catch (error) {
      console.error('닉네임 중복 확인 오류:', error);
      alert('닉네임 중복 확인에 실패했습니다.');
    } finally {
      setLoading(prev => ({ ...prev, nickname: false }));
    }
  };

  // 이메일 중복 확인
  const handleCheckEmail = async () => {
    if (!infoData.email || !infoData.email.includes('@')) {
      alert('올바른 이메일 주소를 입력해주세요.');
      return;
    }

    setLoading(prev => ({ ...prev, email: true }));
    try {
      const result = await checkEmailDuplicate(infoData.email);
      const isAvailable = !result;
      
      setValidation(prev => ({
        ...prev,
        email: isAvailable
      }));

      if (isAvailable) {
        alert('사용 가능한 이메일입니다.');
      } else {
        alert('이미 사용 중인 이메일입니다.');
      }
    } catch (error) {
      console.error('이메일 중복 확인 오류:', error);
      alert('이메일 중복 확인에 실패했습니다.');
    } finally {
      setLoading(prev => ({ ...prev, email: false }));
    }
  };

  // 인증코드 전송
  const handleSendVerificationCode = async () => {
    if (validation.email !== true) {
      alert('먼저 이메일 중복 확인을 해주세요.');
      return;
    }

    setLoading(prev => ({ ...prev, emailCode: true }));
    try {
      const success = await sendVerificationEmail(infoData.email);
      
      if (success) {
        setEmailVerification(prev => ({
          ...prev,
          codeSent: true,
          timer: 300,
          isTimerActive: true,
          code: '',
          isVerified: false
        }));
        alert('인증코드가 발송되었습니다. 이메일을 확인해주세요.');
      } else {
        alert('인증코드 전송에 실패했습니다.');
      }
    } catch (error) {
      console.error('인증코드 전송 오류:', error);
      alert('인증코드 전송에 실패했습니다.');
    } finally {
      setLoading(prev => ({ ...prev, emailCode: false }));
    }
  };

  // 인증코드 검증
  const handleVerifyCode = async () => {
    if (!emailVerification.code || emailVerification.code.length < 6) {
      alert('인증코드를 입력해주세요.');
      return;
    }

    setLoading(prev => ({ ...prev, emailCode: true }));
    try {
      const isValid = await verifyEmailCode(infoData.email, emailVerification.code);
      
      if (isValid) {
        setEmailVerification(prev => ({
          ...prev,
          isVerified: true,
          isTimerActive: false
        }));
        alert('이메일 인증이 완료되었습니다.');
      } else {
        alert('인증코드가 올바르지 않습니다.');
      }
    } catch (error) {
      console.error('인증코드 검증 오류:', error);
      alert('인증코드 검증에 실패했습니다.');
    } finally {
      setLoading(prev => ({ ...prev, emailCode: false }));
    }
  };


  // 회원가입 제출
  const handleSubmit = async (e) => {
    e.preventDefault();

    // 모든 검증 확인
    if (validation.userAuthId !== true) {
      alert('아이디 중복 확인을 해주세요.');
      return;
    }

    if (validation.nickname !== true) {
      alert('닉네임 중복 확인을 해주세요.');
      return;
    }

    if (!emailVerification.isVerified) {
      alert('이메일 인증을 완료해주세요.');
      return;
    }

    if (infoData.password !== infoData.confirmPassword) {
      alert('비밀번호가 일치하지 않습니다.');
      return;
    }

    if (infoData.password.length < 8) {
      alert('비밀번호는 8자 이상 입력해주세요.');
      return;
    }

    setLoading(prev => ({ ...prev, submit: true }));
    try {
      await signupNormal(infoData);
      alert('회원가입이 완료되었습니다!');
      onNext(infoData);
    } catch (error) {
      console.error('회원가입 오류:', error);
      alert('회원가입에 실패했습니다. 다시 시도해주세요.');
    } finally {
      setLoading(prev => ({ ...prev, submit: false }));
    }
  };

  // 타이머 표시 형식 (MM:SS)
  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  // 회원가입 버튼 활성화 조건
  const isFormValid = 
    validation.userAuthId === true &&
    validation.nickname === true &&
    emailVerification.isVerified &&
    !loading.submit;

  return (
    <form
      onSubmit={handleSubmit}
      className="animate-in fade-in slide-in-from-right-4 duration-500"
    >
      <h2 className="text-2xl font-extrabold text-slate-800 mb-2 text-center">
        정보 입력
      </h2>
      <p className="text-slate-500 text-center mb-8 text-sm">
        회원가입에 필요한 정보를 입력해주세요.
      </p>

      <div className="space-y-4 mb-8">
        {/* 이름 */}
        <InputRow
          name="userName"
          icon={<User size={18} />}
          label="이름"
          placeholder="이름을 입력해주세요"
          value={infoData.userName}
          onChange={handleChange}
        />
        
        {/* 아이디 + 중복확인 */}
        <div className="flex items-end gap-2">
          <div className="flex-1">
            <InputRow
              name="userAuthId"
              icon={<User size={18} />}
              label="아이디"
              placeholder="사용할 아이디를 입력해주세요"
              value={infoData.userAuthId}
              onChange={handleChange}
              disabled={validation.userAuthId === true}
            />
          </div>
          <div className="mb-0.5">
            <StatusBadge
              isValid={validation.userAuthId}
              onCheck={handleCheckUserAuthId}
              loading={loading.userAuthId}
            />
          </div>
        </div>

        {/* 비밀번호 */}
        <InputRow
          name="password"
          icon={<Lock size={18} />}
          label="비밀번호"
          type="password"
          placeholder="비밀번호를 입력해주세요"
          value={infoData.password}
          onChange={handleChange}
        />

        {/* 비밀번호 확인 */}
        <InputRow
          name="confirmPassword"
          icon={<Lock size={18} />}
          label="비밀번호 확인"
          type="password"
          placeholder="비밀번호를 다시 입력해주세요"
          value={infoData.confirmPassword}
          onChange={handleChange}
        />

        {/* 이메일 + 중복확인 */}
        <div className="flex items-end gap-2">
          <div className="flex-1">
            <InputRow
              name="email"
              icon={<Mail size={18} />}
              label="이메일"
              type="email"
              placeholder="example@matefarm.com"
              value={infoData.email}
              onChange={handleChange}
              disabled={emailVerification.isVerified || validation.email === true}
            />
          </div>
          <div className="mb-0.5">
            <StatusBadge
              isValid={validation.email}
              onCheck={handleCheckEmail}
              loading={loading.email}
            />
          </div>
        </div>

        {/* 인증코드 전송 버튼 */}
        {validation.email === true && !emailVerification.isVerified && (
          <button
            type="button"
            onClick={handleSendVerificationCode}
            disabled={loading.emailCode || emailVerification.isTimerActive}
            className="w-full py-3 bg-blue-500 text-white rounded-xl font-semibold hover:bg-blue-600 disabled:bg-slate-300 disabled:cursor-not-allowed transition-all"
          >
            {emailVerification.codeSent ? '인증코드 재전송' : '인증코드 전송'}
          </button>
        )}

        {/* 인증코드 입력 */}
        {emailVerification.codeSent && !emailVerification.isVerified && (
          <div className="space-y-2">
            <div className="flex gap-2 items-center">
              
              <InputRow
                name="verificationCode"
                icon={<Mail size={18} />}
                label="인증코드"
                placeholder="인증코드 6자리"
                value={emailVerification.code}
                onChange={(e) =>
                  setEmailVerification(prev => ({
                    ...prev,
                    code: e.target.value
                  }))
                }
              />

              <button
                type="button"
                onClick={handleVerifyCode}
                disabled={loading.emailCode}
                className="px-6 py-3 bg-emerald-500 text-white rounded-xl font-semibold hover:bg-emerald-600 disabled:bg-slate-300 transition-all whitespace-nowrap"
              >
                확인
              </button>
            </div>
            {emailVerification.isTimerActive && (
              <div className="flex items-center gap-2 text-red-500 text-sm">
                <Clock size={16} />
                <span>남은 시간: {formatTime(emailVerification.timer)}</span>
              </div>
            )}
          </div>
        )}

        {/* 이메일 인증 완료 표시 */}
        {emailVerification.isVerified && (
          <div className="flex items-center gap-2 text-emerald-500 text-sm font-semibold bg-emerald-50 p-3 rounded-xl">
            <CheckCircle size={18} />
            <span>이메일 인증이 완료되었습니다</span>
          </div>
        )}

        {/* 닉네임 + 중복확인 */}
        <div className="flex items-end gap-2">
          <div className="flex-1">
            <InputRow
              name="nickname"
              icon={<Sparkles size={18} />}
              label="닉네임"
              placeholder="멋진 닉네임을 지어주세요 (2자 이상)"
              value={infoData.nickname}
              onChange={handleChange}
              disabled={validation.nickname === true}
            />
          </div>
          <div className="mb-0.5">
            <StatusBadge
              isValid={validation.nickname}
              onCheck={handleCheckNickname}
              loading={loading.nickname}
            />
          </div>
        </div>
      </div>

      {/* 버튼 */}
      <div className="flex gap-3">
        <button
          type="button"
          className="flex-1 py-4 rounded-xl border border-slate-200 text-slate-500 font-semibold hover:bg-slate-50 transition-colors"
          onClick={onBack}
        >
          이전
        </button>
        <button
          type="submit"
          disabled={!isFormValid}
          className={`flex-[2] py-4 rounded-xl font-bold transition-all ${
            isFormValid
              ? 'bg-emerald-500 text-white shadow-md hover:bg-emerald-600'
              : 'bg-slate-200 text-slate-400 cursor-not-allowed'
          }`}
        >
          {loading.submit ? '처리 중...' : '회원가입'}
        </button>
      </div>
    </form>
  );
}