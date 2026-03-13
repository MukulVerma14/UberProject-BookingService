package com.example.uberbookingservice.services;

import com.example.uberbookingservice.apis.LocationServiceApi;
import com.example.uberbookingservice.apis.UberSocketApi;
import com.example.uberbookingservice.dto.DriverLocationDto;
import com.example.uberbookingservice.dto.NearByDriversRequestDto;
import com.example.uberbookingservice.dto.RideRequestDto;
import com.example.uberprojectentityservice.models.Booking;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Arrays;
import java.util.List;

@Service
public class DriverMatchingService {

    private final LocationServiceApi locationServiceApi;
    private final UberSocketApi uberSocketApi;

    public DriverMatchingService(LocationServiceApi locationServiceApi, UberSocketApi uberSocketApi) {
        this.locationServiceApi = locationServiceApi;
        this.uberSocketApi = uberSocketApi;
    }

    @Async("bookingTaskExecutor")
    public void findDriverAndNotify(Booking booking, Long passengerId) {
        System.out.println("Processing driver matching in thread: " + Thread.currentThread().getName());

        NearByDriversRequestDto requestDto = NearByDriversRequestDto.builder()
                .latitude(booking.getStartLocation().getLatitude())
                .longitude(booking.getStartLocation().getLongitude())
                .build();

        // 1. Call Location Service
        Call<DriverLocationDto[]> call = locationServiceApi.getNearbyDrivers(requestDto);

        call.enqueue(new Callback<DriverLocationDto[]>() {
            @Override
            public void onResponse(Call<DriverLocationDto[]> call, Response<DriverLocationDto[]> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<DriverLocationDto> driverLocations = Arrays.asList(response.body());

                    // Log the found drivers
                    driverLocations.forEach(d -> System.out.println("Found Driver: " + d.getDriverId()));

                    // 2. Notify via Socket Service
                    raiseRideRequest(RideRequestDto.builder()
                            .passengerId(passengerId)
                            .bookingId(booking.getId())
                            .build());
                }
            }

            @Override
            public void onFailure(Call<DriverLocationDto[]> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void raiseRideRequest(RideRequestDto requestDto) {
        Call<Boolean> call = uberSocketApi.raiseRideRequest(requestDto);
        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful()) {
                    System.out.println("Driver notification sent: " + response.body());
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}