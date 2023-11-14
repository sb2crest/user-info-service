package com.example.user_info_service.service;

import com.example.user_info_service.dto.OTPResponse;
import com.example.user_info_service.entity.OTPEntity;
import com.example.user_info_service.dto.ValidateOTP;
import com.example.user_info_service.exception.BookingException;
import com.example.user_info_service.exception.ResStatus;
import com.example.user_info_service.repository.OTPRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class OTPServiceImplementation implements OTPService {
    OTPRepository otpRepository;
    private final String apiKey;
    private final String smsUrl;
    Random random = new Random();

    @Autowired
    public OTPServiceImplementation(OTPRepository otpRepository,
                                    @Value("${api.key}") String apiKey,
                                    @Value("${sms.url}") String smsUrl) {
        this.apiKey = apiKey;
        this.smsUrl = smsUrl;
        this.otpRepository = otpRepository;

    }

    @Override
    public ResponseEntity<OTPResponse> validateSMS(ValidateOTP validateOTP) {
        OTPResponse otpResponse = new OTPResponse();
        List<OTPEntity> responseOTPEntity = otpRepository.findByPhoneNumber(validateOTP.getMobile(),LocalDateTime.now().minusMinutes(5));
        if (!responseOTPEntity.isEmpty() && responseOTPEntity.stream().anyMatch(o -> o.getOtpPassword().equals(validateOTP.getOtp()))) {
            otpResponse.setMessage("Successfully validated");
            otpResponse.setStatusCode(HttpStatus.OK.value());
            return new ResponseEntity<>(otpResponse, HttpStatus.OK);
        }
        otpResponse.setMessage("Validation Unsuccessful");
        otpResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(otpResponse,HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<OTPResponse>  generateOTP(String mobile) {
        userMobileValidation(mobile);
        OTPResponse otpResponse = new OTPResponse();
        String otp = generateRandomOTP();
        String apiUrl = smsUrl + apiKey +
                "&variables_values=" + otp +
                "&route=otp&numbers=" + mobile;
        try {
            boolean success = sendOtp(apiUrl);
            if (success) {
                saveOTPToDB(mobile, otp);
                otpResponse.setMessage("OTP sent successfully.");
                otpResponse.setStatusCode(HttpStatus.OK.value());
                return new ResponseEntity<>(otpResponse, HttpStatus.OK);
            } else {
                otpResponse.setMessage("Failed to send OTP.");
                otpResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
                return new ResponseEntity<>(otpResponse, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            log.info("exception :" + e.getMessage());
            otpResponse.setMessage("Exception while sending OTP.. ");
            otpResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
            return new ResponseEntity<>(otpResponse, HttpStatus.BAD_REQUEST);
        }
    }

    private void userMobileValidation(String mobile) {
        if (mobile == null || mobile.isEmpty()) {
            throw new BookingException(ResStatus.ENTER_NUMBER);
        }
        if (mobile.length() != 10) {
            throw new BookingException(ResStatus.MOBILE_DIGIT);
        }
    }

    private boolean sendOtp(String apiUrl) throws IOException, URISyntaxException {
        URI url = new URI(apiUrl);
        HttpURLConnection connection = createConnection(url.toURL());
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        return responseCode == 200;
    }

    protected HttpURLConnection createConnection(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection();
    }

    private String generateRandomOTP() {
        // Generate a random 6-digit number
        int randomNumber = random.nextInt(999999 - 100000 + 1) + 100000;
        return String.valueOf(randomNumber);
    }

    private void saveOTPToDB(String mobileNumber, String generatedOTP) {
        OTPEntity otpEntity = new OTPEntity();
        otpEntity.setMobile(mobileNumber);
        otpEntity.setOtpPassword(generatedOTP);
        otpEntity.setGeneratedTime((LocalDateTime.now()));
        otpRepository.save(otpEntity);
    }

}