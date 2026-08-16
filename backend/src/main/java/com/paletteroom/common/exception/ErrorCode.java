																																												package com.paletteroom.common.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 회원
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"),

    // 인증
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다"),
    
    // 작품
    ARTWORK_NOT_FOUND(HttpStatus.NOT_FOUND, "작품을 찾을 수 없습니다"),
    INVALID_COLOR_FORMAT(HttpStatus.BAD_REQUEST, "색상 형식이 올바르지 않습니다"),
    
    // 컬렉션
    COLLECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "컬렉션을 찾을 수 없습니다"),
    COLLECTION_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "컬렉션은 최대 20개까지 만들 수 있습니다"),
    COLLECTION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다"),
    ARTWORK_ALREADY_IN_COLLECTION(HttpStatus.CONFLICT, "이미 저장된 작품입니다"),
    ARTWORK_NOT_IN_COLLECTION(HttpStatus.NOT_FOUND, "컬렉션에 없는 작품입니다"),
    
    // 노트
    NOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "노트를 찾을 수 없습니다"),
    NOTE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다");
    
    

    private final HttpStatus status;
    private final String message;
    
}