package com.example.user_info_service.service;

import com.example.user_info_service.dto.OTPResponse;
import com.example.user_info_service.entity.OTPEntity;
import com.example.user_info_service.dto.ValidateOTP;
import com.example.user_info_service.exception.BookingException;
import com.example.user_info_service.repository.OTPRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@ExtendWith(SpringExtension.class)
class OTPEntityServiceImplementationTest {
    @InjectMocks
    OTPServiceImplementation otpServiceImplementation;
    @Mock
    OTPRepository otpRepository;

    @Test
    void validateSMS() {
        Mockito.when(otpRepository.findByPhoneNumber(Mockito.anyString(),Mockito.any())).thenReturn(getOTPs());
        assertEquals("Successfully validated", otpServiceImplementation.validateSMS(getValidateOTP()));
    }

    @Test
    void validateSMSForUnSuccessfulValidation() {
        Mockito.when(otpRepository.findByPhoneNumber(Mockito.anyString(),Mockito.any())).thenReturn(List.of());
        assertEquals("Validation Unsuccessful", otpServiceImplementation.validateSMS(getValidateOTP()));
    }

    @Test
    void validateSMSForUnSuccessfulValidationWhenNoMatchingRecordFound() {
        List<OTPEntity> list=getOTPs();
        list.get(0).setOtpPassword("16544");
        Mockito.when(otpRepository.findByPhoneNumber(Mockito.anyString(),Mockito.any())).thenReturn(list);
        assertEquals("Validation Unsuccessful", otpServiceImplementation.validateSMS(getValidateOTP()));
    }

    @Test
    void testGenerateOTP_SuccessfulSend() throws IOException {
        ReflectionTestUtils.setField(otpServiceImplementation,"apiKey","Nu9NlFqe7Q3kRQRv1a168kfYqu6aDx9y6Wxy8kUpOyddGolHsw9xtEtd3xWw");
        ReflectionTestUtils.setField(otpServiceImplementation,"smsUrl","https://www.fast2sms.com/dev/bulkV2?authorization=");
        String mobileNumber = "1234567890";

        OTPServiceImplementation spyService = Mockito.spy(otpServiceImplementation);

        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockConnection.getResponseCode()).thenReturn(200);
        String mockResponse = "Mock response";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(mockResponse.getBytes(StandardCharsets.UTF_8));
        when(mockConnection.getInputStream()).thenReturn(inputStream);
        doReturn(mockConnection).when(spyService).createConnection(any());

        ResponseEntity<OTPResponse> result = spyService.generateOTP(mobileNumber);

        verify(otpRepository, times(1)).save(any(OTPEntity.class));
        assertEquals("OTP sent successfully.", Objects.requireNonNull(result.getBody()).getMessage());
    }
    @Test
    void testGenerateOTP_FailedSend() throws IOException {
        ReflectionTestUtils.setField(otpServiceImplementation,"apiKey","abc");
        ReflectionTestUtils.setField(otpServiceImplementation,"smsUrl","https://www.fast2sms.com/dev/bulkV2?authorization=");
        String mobileNumber = "9535858675";

        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockConnection.getResponseCode()).thenReturn(401);

        ResponseEntity<OTPResponse> result = otpServiceImplementation.generateOTP(mobileNumber);

        verifyNoInteractions(otpRepository);
        assertEquals("Failed to send OTP.", Objects.requireNonNull(result.getBody()).getMessage());
    }
    @Test
    void testGenerateOTP_whenMobileIsEmpty_throwException(){
        assertThrows(BookingException.class , ()-> otpServiceImplementation.generateOTP(""));
    }

    @Test
    void testGenerateOTP_whenMobileIsInvalid_throwException() {
        assertThrows(BookingException.class , ()-> otpServiceImplementation.generateOTP("1234"));
    }

    @Test
    void testGenerateOTP_whenMobileIsNull_throwException() {
        assertThrows(BookingException.class , ()-> otpServiceImplementation.generateOTP(null));
    }
    @Test
    void testGenerateOTP_whenMobileIsInvalid_throwException1() throws IOException {
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockConnection.getResponseCode()).thenReturn(401);
        ResponseEntity<OTPResponse> response = otpServiceImplementation.generateOTP("1234567890");
        assertEquals("Exception while sending OTP.. ", Objects.requireNonNull(response.getBody()).getMessage());

    }

    List<OTPEntity> getOTPs(){
        List<OTPEntity> list=new ArrayList<>();

        OTPEntity otpEntity =new OTPEntity();
        otpEntity.setOtpPassword("12345");
        otpEntity.setMobile("1234567890");
        otpEntity.setId(1L);
        otpEntity.setGeneratedTime(LocalDateTime.now());
        list.add(otpEntity);

        OTPEntity otpEntity1 =new OTPEntity();
        otpEntity1.setOtpPassword("13345");
        otpEntity1.setMobile("123448554644");
        otpEntity1.setId(1L);
        otpEntity1.setGeneratedTime(LocalDateTime.now());
        list.add(otpEntity1);
        return list;
    }
    ValidateOTP getValidateOTP() {
        ValidateOTP validateOTP = new ValidateOTP();
        validateOTP.setOtp("12345");
        validateOTP.setMobile("1234567890");
        return validateOTP;
    }
}