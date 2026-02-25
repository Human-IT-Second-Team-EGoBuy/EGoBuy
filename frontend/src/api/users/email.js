// 회원가입 및 아이디 비밀번호 찾기 시 사용될 함수. ( Email 인증 관련 )
import apiClient from '@/api/axios';

/**
 * 1. 이메일 중복 검사
 * @param {string} email 
 * @returns {Promise<boolean>} 중복 여부 (true: 중복, false: 사용가능)
 */
export const checkEmailDuplicate = async (email) => {
    try {
        const response = await apiClient.get('/api/users/validate/email', 
        {
             params: { email: email }
        }
    );
        // ResponseDTO<BooleanResponseDTO> 구조에 따라 결과 추출
        console.log(response.data.content.exist)
        return response.data.content.exist
    } catch (error) {
        console.error("이메일 중복 검사 실패:", error);
        throw error;
    }
};

/**
 * 2. 인증번호 이메일 전송
 * @param {string} email 
 */
export const sendVerificationEmail = async (email) => {
    try {
        const response = await apiClient.post('/api/users/email-verification', 
        {
            email: email
        }
    );
        return response.data.success;
    } catch (error) {
        console.error("인증 이메일 전송 실패:", error);
        throw error;
    }
};

/**
 * 3. 인증번호 검증
 * @param {string} email 
 * @param {string} code 
 */
export const verifyEmailCode = async (email, code) => {
    try {
        const response = await apiClient.post('/api/users/code-verification', {
            email: email,
            verification_code: code // @JsonProperty("verification_code")
        });
        return response.data.success;
    } catch (error) {
        console.error("인증번호 검증 실패:", error);
        throw error;
    }
};