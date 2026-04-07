package com.example.chattingroom.config;

import com.example.chattingroom.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

// 이 클래스는 모든 컨트롤러에서 발생하는 에러를 감시하고 캐치합니다.
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * ErrorResponse 내부에 Status 를 담았음에도, ResponseEntity.status()에 status를 반복해서 적은
     * 이유는. 수신자가 다르기 때문.
     * ResponseEntity.status()의 status는 이 통신을 브라우저가 받았을때 '브라우저'가 통신의 상태를 파악하기 위함이고.
     *
     * ErrorResponse에 담았던 status는 해당 통신을 받아서 처리할 JavaScript가 읽기 편하도록 JSON형식으로 담아준것.
     *
     * 따라서, 실제로는 이런 반복적인 타이핑을 해결하기위해 "정적 팩토리 메소드"를 사용.
     * 
     * @param exception
     * @return
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxSizeException(MaxUploadSizeExceededException exception) {
        log.warn("파일 용량 초과: {}", exception.getMessage());

        return ErrorResponse.toResponseEntity(HttpStatus.PAYLOAD_TOO_LARGE, "파일 용량은 5MB를 초과할 수 없습니다.");
    }

    // 외부 API 통신 폭발 방어
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorResponse> handleWebClientException(WebClientException exception) {
        log.error("Gemini API 통신 오류 발생: ", exception.getMessage());

        return ErrorResponse.toResponseEntity(HttpStatus.SERVICE_UNAVAILABLE, "AI 서버가 혼잡합니다,, 잠시 후 시도해주세요.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
        log.warn("잘못된 요청: ", exception.getMessage());

        return ErrorResponse.toResponseEntity(HttpStatus.BAD_REQUEST, "잘못된 요청입니다.");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException exception) {
        log.warn("자원을 찾을 수 없습니다: ", exception.getMessage());

        return ErrorResponse.toResponseEntity(HttpStatus.NOT_FOUND, "요청하신 페이지를 찾을 수 없습니다.");
    }

    // 깔때기구조, 세세한 범위의 오류를 잡다가 마지막에는 큰 범위로.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllException(Exception exception) {
        log.error("서버 내부 오류 발생: ", exception);

        return ErrorResponse.toResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 일시적인 오류가 발생했습니다.");
    }
}
