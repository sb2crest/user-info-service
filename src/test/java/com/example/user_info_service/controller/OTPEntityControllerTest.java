package com.example.user_info_service.controller;

import com.example.user_info_service.dto.OTPResponse;
import com.example.user_info_service.model.TestUtil;
import com.example.user_info_service.dto.ValidateOTP;
import com.example.user_info_service.service.OTPService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@WebMvcTest(OTPController.class)
class OTPEntityControllerTest {
    @MockBean
    OTPService otpService;
    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }

    @Test
    void validateSMS() throws Exception {
        Mockito.when(otpService.validateSMS(Mockito.any())).thenReturn("Successfully validated");
        mvc.perform(post("/validateOTP").content(TestUtil.convertObjectToJsonBytes(getValidateOTP()))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    @Test
    void validateSMSWithUnSuccess() throws Exception {
        Mockito.when(otpService.validateSMS(Mockito.any())).thenReturn("Validation Unsuccessful");
        mvc.perform(post("/validateOTP").content(TestUtil.convertObjectToJsonBytes(getValidateOTP()))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    @Test
     void testGetOTP() throws Exception {
        String mobileNumber = "1234567890";

        Mockito.when(otpService.generateOTP(mobileNumber)).thenReturn(new ResponseEntity<>(new OTPResponse(), HttpStatus.OK));

        mvc.perform(post("/sendOTP")
                        .param("mobile", mobileNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    ValidateOTP getValidateOTP() {
        ValidateOTP validateOTP = new ValidateOTP();
        validateOTP.setOtp("12345");
        validateOTP.setMobile("1234567890");
        return validateOTP;
    }
}