package com.example.uberbookingservice.exceptions;

import com.example.uberbookingservice.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponseDto> handleConflict(ObjectOptimisticLockingFailureException ex) {
        ErrorResponseDto error =  ErrorResponseDto.builder()
                .message("This ride has already beenn accepted by another driver. Better luck next time!")
                .errorCode("RIDE_ALREADY_TAKEN")
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDto> handleRuntime(RuntimeException ex) {
        ErrorResponseDto error = ErrorResponseDto.builder()
                .message(ex.getMessage())
                .errorCode("BUSSINESS_LOGIC_ERROR")
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
