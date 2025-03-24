package com.example.coupangclone.global.exception;

import com.example.coupangclone.global.dto.BasicResponseDto;
import com.example.coupangclone.global.dto.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<BasicResponseDto> signValidException(MethodArgumentNotValidException exception) {
        BindingResult bindingResult = exception.getBindingResult();

        StringBuilder sb = new StringBuilder();

        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            sb.append("[");
            sb.append(fieldError.getField());
            sb.append("]");
            sb.append(fieldError.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(BasicResponseDto.addBadRequest(sb.toString()));
    }

    @ExceptionHandler(ErrorException.class)
    public ResponseEntity<ErrorResponseDto> handleErrorException(ErrorException e) {
        log.warn("[{} 예외]: {}", e.getExceptionEnum().getMsg());

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(e.getExceptionEnum().getStatus(), e.getExceptionEnum().getMsg());
        return ResponseEntity.status(e.getExceptionEnum().getStatus()).body(errorResponseDto);
    }

}
