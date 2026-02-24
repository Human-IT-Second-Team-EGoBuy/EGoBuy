// useUserStore.js
import { create } from 'zustand';

const useUserStore = create((set) => ({
  user: null, // user 정보 | 초기값은 null (로그아웃 상태)
  isAuthenticated: false, // 로그인 상태
  isLoaded: false, //  초기 로딩 상태 추가

  // 로그인 성공 시 유저 정보를 저장하는 함수
  setUser: (userData) => set({ 
    user: userData, 
    isAuthenticated: true,
    isLoaded: true
  }),

  // 로그아웃 시 유저 정보를 지우는 함수
  clearUser: () => set({ 
    user: null, 
    isAuthenticated: false,
    isLoaded: true
  }),
}));

export default useUserStore;