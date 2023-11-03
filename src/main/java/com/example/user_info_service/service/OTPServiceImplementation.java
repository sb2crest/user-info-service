package com.example.user_info_service.service;

import com.example.user_info_service.entity.OTPEntity;
import com.example.user_info_service.dto.ValidateOTP;
import com.example.user_info_service.repository.OTPRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    public String validateSMS(ValidateOTP validateOTP) {
        List<OTPEntity> responseOTPEntity = otpRepository.findByPhoneNumber(validateOTP.getMobile(),LocalDateTime.now().minusMinutes(5));
        if (!responseOTPEntity.isEmpty() && responseOTPEntity.stream().anyMatch(o -> o.getOtpPassword().equals(validateOTP.getOtp()))) {
            return "Successfully validated";
        }
        return "Validation Unsuccessful";
    }

    @Override
    public String generateOTP(String mobile) {
        String otp = generateRandomOTP();
        String apiUrl = smsUrl + apiKey +
                "&variables_values=" + otp +
                "&route=otp&numbers=" + mobile;
        try {
            boolean success = sendOtp(apiUrl);
            if (success) {
                saveOTPToDB(mobile, otp);
                return "OTP sent successfully.";
            } else {
                return "Failed to send OTP.";
            }
        } catch (Exception e) {
            log.info("exception :" + e.getMessage());
            return "Exception while sending OTP.. ";
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
