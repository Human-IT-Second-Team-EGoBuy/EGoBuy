package com.avengers.matefarm.common.exception;


import com.avengers.matefarm.common.ResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
//필기. 해당 패키지에서 에러 발생시 작동하는 CustomHandler
@RestControllerAdvice(basePackages = "com.learn.securitytest")

public class GlobalExceptionHandler {

    // 지원되지 않는 HTTP 메소드를 사용할 때 발생하는 예외
    @ExceptionHandler(value = {NoHandlerFoundException.class, HttpRequestMethodNotSupportedException.class})
    public ResponseDTO<?> handleNoPageFoundException(Exception e) {
        log.error("handleNoPageFoundException() in GlobalExceptionHandler throw NoHandlerFoundException : {}"
                , e.getMessage());
        return ResponseDTO.fail(new CommonException(ErrorCode.WRONG_ENTRY_POINT));
    }

    // 메소드의 인자 타입이 일치하지 않을 때 발생하는 예외
    @ExceptionHandler(value = {MethodArgumentTypeMismatchException.class})
    public ResponseDTO<?> handleArgumentNotValidException(MethodArgumentTypeMismatchException e) {
        log.error("handleArgumentNotValidException() in GlobalExceptionHandler throw MethodArgumentTypeMismatchException : {}"
                , e.getMessage());
        return ResponseDTO.fail(e);
    }

    // 필수 파라미터가 누락되었을 때 발생하는 예외
    @ExceptionHandler(value = {MissingServletRequestParameterException.class})
    public ResponseDTO<?> handleArgumentNotValidException(MissingServletRequestParameterException e) {
        log.error("handleArgumentNotValidException() in GlobalExceptionHandler throw MethodArgumentNotValidException : {}"
                , e.getMessage());
        return ResponseDTO.fail(e);
    }

    /* CommonException 전역 예외 처리기 */
    @ExceptionHandler(value = {CommonException.class})
    public ResponseDTO<?> handleCustomException(CommonException e) {
        log.error("handleCustomException() in GlobalExceptionHandler: {}", e.getMessage());
        return ResponseDTO.fail(e);
    }

    //필기. 서버 내부 오류시 작동 ( 의도되지 않고, 명시하지 않은 모든 예외, 서버 내부 오류 은폐  )
    @ExceptionHandler(value = {Exception.class})
    public ResponseDTO<?> handleServerException(Exception e) {
        log.info("occurred exception in handleServerError = {}", e.getMessage());
        return ResponseDTO.fail(new CommonException(ErrorCode.INTERNAL_SERVER_ERROR));
    }

    // 데이터 무결성 위반 예외 처리기 추가 ( PK / FK / UNIQUE / NOT NULL etc.. DB가 던진 무결성 예외 )
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseDTO<?> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("handleDataIntegrityViolationException() in GlobalExceptionHandler : {}", e.getMessage());
        return ResponseDTO.fail(new CommonException(ErrorCode.DATA_INTEGRITY_VIOLATION));
    }


}

/*
 위 명시된 예외처리에 해당되지 않은 예외들은
 HttpStatus : 500
 "error" : Internal Server Error
 등의 구조로 Frontend 에서 응답받게 된다.

 Frontend/Backend 둘 다 동일인이 작업하는 경우 상관 없지만,
 협업 시, Internal Server Error 만으로는 frontend 개발자는
 어떤 Error 인지 몰라 대응이 불가능하므로 전역 예외 처리를 통해 예외를 명시해준다.

 또한 Spring 기본 에러 응답 사용을 하지 않고
 @ExceptionHandler (@ControllerAdvice) 를 사용하여 StackTrace 를 감춤으로써
 서버 내부 정보 :
 ( SQL, Table name, column 명, 그 외 Http 메소드 실행 시 오류 순서, 내용, 서비스 구조 등 )
 이 휴먼 에러로 유출되지 않도록 한다.

* */