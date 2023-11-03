package com.example.user_info_service.service;

import com.example.user_info_service.entity.OTPEntity;
import com.example.user_info_service.dto.ValidateOTP;
import com.example.user_info_service.repository.OTPRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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

        String result = spyService.generateOTP(mobileNumber);

        verify(otpRepository, times(1)).save(any(OTPEntity.class));
        assertEquals("OTP sent successfully.", result);
    }
    @Test
     void testGenerateOTP_FailedSend() throws IOException {
        ReflectionTestUtils.setField(otpServiceImplementation,"apiKey","abc");
        ReflectionTestUtils.setField(otpServiceImplementation,"smsUrl","https://www.fast2sms.com/dev/bulkV2?authorization=");
        String mobileNumber = "9535858675";

        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockConnection.getResponseCode()).thenReturn(401);

        String result = otpServiceImplementation.generateOTP(mobileNumber);

        verifyNoInteractions(otpRepository);
        assertEquals("Failed to send OTP.", result);
    }
    @Test
     void testGenerateOTP_whenException_throwException() throws IOException {
           HttpURLConnection mockConnection = mock(HttpURLConnection.class);
           when(mockConnection.getResponseCode()).thenReturn(401);
           assertEquals("Exception while sending OTP.. ",otpServiceImplementation.generateOTP(null));
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