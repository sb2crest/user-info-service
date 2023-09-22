package com.example.user_info_service.controller;

import com.example.user_info_service.model.TestUtil;
import com.example.user_info_service.pojo.BookingDetails;
import com.example.user_info_service.pojo.BookingPojo;
import com.example.user_info_service.pojo.UserPojo;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(BookingController.class)
class     BookingControllerTest {

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
    void testBookingVehicle_ValidMobileNumber() throws Exception {
        BookingPojo bookingPojo = getBookingPojo();
        when(bookingService.bookingVehicle(Mockito.any())).thenReturn("successful");
        mvc.perform(post("/booking").content(TestUtil.convertObjectToJsonBytes(bookingPojo))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    void testBookingVehicle_InvalidMobileNumber() throws Exception {
        BookingPojo bookingPojo = getBookingPojo();
        bookingPojo.getUserPojo().setMobile("1234");
        when(bookingService.bookingVehicle(Mockito.any())).thenReturn("error");
        mvc.perform(post("/booking").content(TestUtil.convertObjectToJsonBytes(bookingPojo))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }

    @Test
    void testBookingVehicle_EmptyMobileNumber() throws Exception {
        BookingPojo bookingPojo = getBookingPojo();
        bookingPojo.getUserPojo().setMobile("");
        when(bookingService.bookingVehicle(Mockito.any())).thenReturn("error");
        mvc.perform(post("/booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtil.convertObjectToJsonBytes(bookingPojo)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testBookingDetails() throws Exception {
        BookingDetails bookingDetails = new BookingDetails();

        when(bookingService.getBookingDetails(Mockito.any())).thenReturn(bookingDetails);
        mvc.perform(get("/bookingDetails").param("bookingId", "1234")
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

    private BookingPojo getBookingPojo() {
        BookingPojo bookingPojo = new BookingPojo();
        bookingPojo.setVehicleNumber("ka02h0886");
        UserPojo userPojo = new UserPojo();
        userPojo.setMobile("1234456671");
        userPojo.setName("abc");
        bookingPojo.setUserPojo(userPojo);
        return bookingPojo;
    }

}
