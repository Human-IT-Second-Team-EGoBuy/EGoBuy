export function unwrapResponseDTO(res) {
  const dto = res?.data;
  if (!dto) throw new Error("서버 응답이 비어있습니다.");
  if (!dto.success) {
    const msg = dto?.error?.message || "요청에 실패했습니다.";
    const code = dto?.error?.code;
    const err = new Error(msg);
    err.code = code;
    throw err;
  }
  return dto.content;
}