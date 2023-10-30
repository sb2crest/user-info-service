

package com.example.user_info_service.service;

import com.example.user_info_service.entity.PaymentEntity;
import com.example.user_info_service.exception.BookingException;
import com.example.user_info_service.exception.ResStatus;
import com.example.user_info_service.pojo.PaymentPojo;
import com.example.user_info_service.pojo.PaymentResponse;
import com.example.user_info_service.repository.BookingRepo;
import com.example.user_info_service.repository.PaymentRepository;
import com.razorpay.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    RazorpayClient razorPayClient;

    @Value("${razorpay.api.key.id}")
    private String keyID;

    @Value("${razorpay.api.key.secret}")
    private String keySecret;

    @Autowired
    BookingRepo bookingRepo;

    public PaymentServiceImpl() {
    }

    public PaymentServiceImpl(RazorpayClient razorPayClient) {
        this.razorPayClient = razorPayClient;
    }

    @Override
    public PaymentResponse createPayment(PaymentPojo paymentPojo) {

        PaymentResponse response = new PaymentResponse();

        Boolean validate = bookingRepo.validateUsingIdAndMobile(paymentPojo.getBookingId(), paymentPojo.getMobile());
        if (validate) {

            try {
                razorPayClient = new RazorpayClient(keyID, keySecret);

                JSONObject orderRequest = new JSONObject();
                orderRequest.put("amount", paymentPojo.getAmount());
                orderRequest.put("currency", "INR");
                orderRequest.put("receipt", "payment_receipt_" + System.currentTimeMillis());

                Order razorpayOrder = razorPayClient.orders.create(orderRequest);
                String razorpayOrderId = razorpayOrder.get("id");

                PaymentEntity paymentEntity = new PaymentEntity();
                paymentEntity.setRazorPayOrderId(razorpayOrderId);
                paymentEntity.setAmount(paymentPojo.getAmount());
                paymentEntity.setPaymentDate(new Date());
                paymentEntity.setBookingId(paymentPojo.getBookingId());
                paymentRepository.save(paymentEntity);

                response.setStatus("success");
                response.setMessage("Payment created successfully");
                response.setRazorpayPaymentId(razorpayOrderId);
                response.setPaymentDate(paymentEntity.getPaymentDate());
            } catch (RazorpayException e) {
                response.setStatus("error");
                response.setMessage("Error creating Razorpay order: " + e.getMessage());
            }
        } else {
            throw new BookingException(ResStatus.NO_RECORD_FOUND_WITH_ID_AND_MOBILE);
        }
        return response;

    }

    public String generateRazorpaySignature(String razorPayOrderId, String razorPayPaymentId) {
        String signature = null;
        try {
            String secret = keySecret;
            String data = razorPayOrderId + "|" + razorPayPaymentId;
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);

            byte[] hmacData = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            signature = Base64.getEncoder().encodeToString(hmacData);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.debug("Exception while generating signature " + e.getMessage());
        }
        return signature;
    }

    public boolean verifyRazorpaySignature(String razorPayOrderId, String razorPayPaymentId, String razorPaySignature) {
        String generatedSignature = generateRazorpaySignature(razorPayOrderId, razorPayPaymentId);
        return generatedSignature != null && generatedSignature.equals(razorPaySignature);
    }


}