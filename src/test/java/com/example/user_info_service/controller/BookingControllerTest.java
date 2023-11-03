package com.example.user_info_service.controller;

import com.example.user_info_service.model.TestUtil;
import com.example.user_info_service.dto.*;
import com.example.user_info_service.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @MockBean
    BookingService bookingService;

    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }

    @Test
    void testBookingVehicle() throws Exception {
        BookingDto bookingDto = getBookingPojo();
        when(bookingService.bookingVehicle(Mockito.any())).thenReturn(new BookingResponse());
        mvc.perform(post("/booking").content(TestUtil.convertObjectToJsonBytes(bookingDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    void testBookingDetails() throws Exception {

        when(bookingService.getBookingDetails(Mockito.any())).thenReturn(new BookingData());
        mvc.perform(get("/bookingDetails").param("mobile", "1234")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    void testConfirmBooking() throws Exception {
        when(bookingService.confirmBooking("123")).thenReturn("Booking is Confirmed");

        mvc.perform(get("/confirm").param("bookingId", "123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testDeclineBooking() throws Exception {
        when(bookingService.declineBooking("456")).thenReturn("Booking is Declined");

        mvc.perform(get("/decline")
                        .param("bookingId", "456")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    void testGetSlotByVehicleNumber() throws Exception {
        when(bookingService.getBookedSlotsByVehicleNumber(Mockito.any())).thenReturn(new VehicleBooked());

        mvc.perform(get("/getBookedSlotsByVehicleNumber")
                        .param("vehicleNumber", "KA07V1234")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetVehicleAvailability() throws Exception {
        when(bookingService.getVehicleAvailability(Mockito.any())).thenReturn(List.of());

        mvc.perform(post("/getVehicleAvailability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtil.convertObjectToJsonBytes(new VehiclesAvailable())))
                .andExpect(status().isOk());
    }

    @Test
    void getBookingInfoByBookingIdTest() throws Exception {
        when(bookingService.getBookingInfoByBookingId("456")).thenReturn(new BookingInfo());

        mvc.perform(get("/getBookingInfoByBookingId")
                        .param("bookingId", "456")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    void getInTouchTest() throws Exception {
        mvc.perform(post("/getInTouch").content(TestUtil.convertObjectToJsonBytes(getUserData()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    private BookingDto getBookingPojo() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setVehicleNumber("ka02h0886");
        UserDto userDto = new UserDto();
        userDto.setMobile("1234456671");
        userDto.setFirstName("abc");
        userDto.setEmail("abc@gmail.com");
        bookingDto.setUser(userDto);
        return bookingDto;
    }

    private UserData getUserData(){
        UserData userData = new UserData();
        userData.setName("abc");
        userData.setEmail("abc@gmail.com");
        userData.setMessage("success");
        return userData;

    }

}
