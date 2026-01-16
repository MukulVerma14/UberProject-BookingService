package com.example.uberbookingservice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearByDriversRequestDto {

    Double latitude;
    Double longitude;
}
