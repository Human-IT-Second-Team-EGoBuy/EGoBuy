package com.avengers.matefarm.common.exception;

import com.avengers.matefarm.common.ResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.avengers.matefarm")
public class MateFarmExceptionHandler {

    @ExceptionHandler(CommonException.class)
    public ResponseEntity<ResponseDTO<?>> handleCommon(CommonException e) {
        ErrorCode ec = e.getErrorCode();
        HttpStatus status = (ec != null && ec.getHttpStatus() != null)
                ? ec.getHttpStatus()
                : HttpStatus.INTERNAL_SERVER_ERROR;

        log.error("CommonException: code={}, status={}, message={}",
                ec != null ? ec.getCode() : null,
                status,
                ec != null ? ec.getMessage() : null,
                e
        );

        // ✅ HTTP status를 실제로 401/403/404/400으로 내려줌
        return ResponseEntity.status(status).body(ResponseDTO.fail(e));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDTO<?>> handleAny(Exception e) {
        log.error("Unhandled Exception", e);

        CommonException ce = new CommonException(ErrorCode.INTERNAL_SERVER_ERROR);
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ResponseDTO.fail(ce));
    }
}
