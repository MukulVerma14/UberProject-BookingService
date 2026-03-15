package com.example.uberbookingservice.services;

import com.example.uberbookingservice.apis.LocationServiceApi;
import com.example.uberbookingservice.apis.UberSocketApi;
import com.example.uberbookingservice.dto.*;
import com.example.uberbookingservice.repositories.BookingRepository;
import com.example.uberbookingservice.repositories.DriverRepository;
import com.example.uberbookingservice.repositories.PassengerRepository;
import com.example.uberprojectentityservice.models.Booking;
import com.example.uberprojectentityservice.models.BookingStatus;
import com.example.uberprojectentityservice.models.Driver;
import com.example.uberprojectentityservice.models.Passenger;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class BookingServiceImpl implements BookingService{
    private final PassengerRepository passengerRepository;
    private final BookingRepository bookingRepository;
    private final DriverRepository driverRepository;
    private final RestTemplate restTemplate;
    private final DriverMatchingService driverMatchingService;

//    private static final String LOCATION_SERVICE = "http://localhost:7777";

    private final LocationServiceApi locationServiceApi;

    private final UberSocketApi uberSocketApi;

    public BookingServiceImpl(PassengerRepository passengerRepository,
                              BookingRepository bookingRepository,
                              LocationServiceApi locationServiceApi,
                              DriverRepository driverRepository,
                              UberSocketApi uberSocketApi,
                              DriverMatchingService driverMatchingService) {
        this.passengerRepository = passengerRepository;
        this.bookingRepository = bookingRepository;
        this.restTemplate = new RestTemplate();
        this.locationServiceApi = locationServiceApi;
        this.driverRepository = driverRepository;
        this.uberSocketApi = uberSocketApi;
        this.driverMatchingService = driverMatchingService;
    }

    @Override
    public CreateBookingResponseDto createBooking(CreateBookingDto bookingDetails) {
        Optional<Passenger> passenger = passengerRepository.findById(bookingDetails.getPassengerId());
        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.ASSIGNING_DRIVER)
                .startLocation(bookingDetails.getStartLocation())
                .endLocation(bookingDetails.getEndLocation())
                .passenger(passenger.get())
                .build();
        Booking newBooking = bookingRepository.save(booking);

        driverMatchingService.findDriverAndNotify(newBooking, bookingDetails.getPassengerId());

//        NearByDriversRequestDto request = NearByDriversRequestDto
//                                        .builder()
//                                        .latitude(bookingDetails.getStartLocation().getLatitude())
//                                        .longitude(bookingDetails.getStartLocation().getLongitude())
//                                        .build();
//
//        processNearbyDriversAsync(request, bookingDetails.getPassengerId(), newBooking.getId());
//
//        //make an api call to location service to fetch nearby drivers
//         ResponseEntity<DriverLocationDto[]> result = restTemplate.postForEntity(LOCATION_SERVICE + "/api/location/nearby/drivers",request ,DriverLocationDto[].class);
//
//         if (result.getStatusCode().is2xxSuccessful() && result.getBody() != null) {
//             List<DriverLocationDto> driverLocations = Arrays.asList(result.getBody());
//             driverLocations.forEach(driverLocationDto -> {
//                 System.out.println(driverLocationDto.getDriverId() + "" + "lat:" + driverLocationDto.getLatitude() + "long:" + driverLocationDto.getLongitude());
//             });
//         }

        return CreateBookingResponseDto.builder()
                .bookingId(newBooking.getId())
                .bookingStatus(newBooking.getBookingStatus().toString())
                .build();
    }

    @Override
    @Transactional
    public UpdateBookingResponseDto updateBooking(UpdateBookingRequestDto bookingRequestDto, Long bookingId) {
        Driver driver = driverRepository.findById(bookingRequestDto.getDriverId().get())
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        //Todo : if (driver.isPresent() && driver.get().isAvailable()
        //bookingRepository.updateBookingStatusAndDriverById(bookingId, BookingStatus.SCHEDULED, driver.get());
        //Todo : driverRepository.update -> make it unavailable
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getBookingStatus() != BookingStatus.ASSIGNING_DRIVER) {
            throw new RuntimeException("This ride has already been accepted.");
        }

        if(!driver.getIsAvailable()) {
            throw new RuntimeException("Driver is not available");
        }

        booking.setBookingStatus(BookingStatus.SCHEDULED);
        booking.setDriver(driver);
        bookingRepository.save(booking);

        return  UpdateBookingResponseDto.builder()
                .bookingId(bookingId)
                .status(booking.getBookingStatus())
                .driver(Optional.ofNullable(booking.getDriver()))
                .build();
    }
}
