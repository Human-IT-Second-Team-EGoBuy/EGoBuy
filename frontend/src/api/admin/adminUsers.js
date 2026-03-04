import apiClient from "../axios";
import { unwrapResponseDTO } from "./unwrap";

// 특정 유저 조회
export async function getUserById(userId) {
  const res = await apiClient.get(`/api/users/${userId}/userProfile`);
  return unwrapResponseDTO(res);
}

// 내 프로필(= zustand용)
export async function getMyUserProfile() {
  const res = await apiClient.get(`/api/users/userProfile`);
  return unwrapResponseDTO(res);
}

// Active로 변경
export async function activateUser(userId) {
  const res = await apiClient.patch(`/api/users/${userId}/active`);
  return unwrapResponseDTO(res); // content: "유저 상태가 Active..."
}

// SoftDelete
export async function deleteUser(userId) {
  const res = await apiClient.patch(`/api/users/${userId}/delete`);
  return unwrapResponseDTO(res);
}

// Black 등록
export async function blackUser(userId) {
  const res = await apiClient.patch(`/api/users/${userId}/black`);
  return unwrapResponseDTO(res);
}