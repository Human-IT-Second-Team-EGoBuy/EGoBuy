package com.avengers.matefarm.files.enums;

// S3를 사용할 도메인 정의
public enum OwnerType {
    COMMUNITY_POST,
    NOTICE,
    FARM_LOG,
    EDITOR_TEMP // 게시글 생성 전, 본문에 이미지 URL 반환 시 사용됨

    // EDITOR_TEMP 이면서 OwnerId가 null인 경우는 배치를 돌려 주기적으로 삭제 처리.
}
