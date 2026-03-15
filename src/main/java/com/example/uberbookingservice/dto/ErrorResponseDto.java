package com.example.uberbookingservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDto {

    private String message;
    private String errorCode;
    private LocalDateTime timestamp;
}
