package com.example.user_info_service.service;

import com.example.user_info_service.entity.PaymentEntity;
import com.example.user_info_service.exception.BookingException;
import com.example.user_info_service.exception.ResStatus;
import com.example.user_info_service.dto.BookingResponse;
import com.example.user_info_service.dto.PaymentData;
import com.example.user_info_service.dto.PaymentDto;
import com.example.user_info_service.dto.PaymentResponse;
import com.example.user_info_service.repository.BookingRepo;
import com.example.user_info_service.repository.PaymentRepository;
import com.razorpay.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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

    private final String STATUS = "SUCCESS";
    private final String BAD_STATUS = "FAILED";

    @Autowired
    BookingRepo bookingRepo;

    public PaymentServiceImpl() {
    }

    public PaymentServiceImpl(RazorpayClient razorPayClient) {
        this.razorPayClient = razorPayClient;
    }

    @Override
    public PaymentResponse createPayment(PaymentDto paymentDto) {

        PaymentResponse response = new PaymentResponse();

        Boolean validate = bookingRepo.validateUsingIdAndMobile(paymentDto.getBookingId(), paymentDto.getMobile());
        if (validate) {

            try {
                razorPayClient = new RazorpayClient(keyID, keySecret);

                JSONObject orderRequest = new JSONObject();
                orderRequest.put("amount", paymentDto.getAmount());
                orderRequest.put("currency", "INR");
                orderRequest.put("receipt", "payment_receipt_" + System.currentTimeMillis());

                Order razorpayOrder = razorPayClient.orders.create(orderRequest);
                String razorpayOrderId = razorpayOrder.get("id");

                PaymentEntity paymentEntity = new PaymentEntity();
                paymentEntity.setRazorPayOrderId(razorpayOrderId);
                paymentEntity.setAmount(paymentDto.getAmount());
                paymentEntity.setPaymentDate(new Date());
                paymentEntity.setBookingId(paymentDto.getBookingId());
                paymentRepository.save(paymentEntity);

                response.setStatus("success");
                response.setMessage("Payment created successfully");
                response.setRazorPayOrderId(razorpayOrderId);
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

    public String generateRazorpaySignature(String razorPayOrderId, String razorPayPaymentId, String keySecret) {
        String signature = null;
        try {
            String signatureData = razorPayOrderId + "|" + razorPayPaymentId;
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(keySecret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            byte[] bytes = sha256_HMAC.doFinal(signatureData.getBytes());

            StringBuilder builder = new StringBuilder();
            for (byte aByte : bytes) {
                builder.append(String.format("%02x", aByte));
            }

            signature = builder.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.debug("Exception while generating signature " + e.getMessage());
        }
        return signature;
    }

    public ResponseEntity<BookingResponse> verifyRazorpaySignature(PaymentData paymentData) {
        String generatedSignature = generateRazorpaySignature(paymentData.getRazorPayOrderId(), paymentData.getRazorPayPaymentId(), keySecret);
        PaymentEntity paymentEntity = paymentRepository.findBookingIdByRazorPayOrderId(paymentData.getRazorPayOrderId());
        log.info("response:::::::::::{}",paymentEntity);

        BookingResponse bookingResponse = new BookingResponse();
        bookingResponse.setBookingId(paymentEntity.getBookingId());
        if (generatedSignature != null && generatedSignature.equals(paymentData.getRazorPaySignature())) {

            paymentEntity.setRazorPayPaymentId(paymentData.getRazorPayPaymentId());
            paymentEntity.setPaymentStatus(STATUS);

            paymentRepository.save(paymentEntity);
            bookingResponse.setMessage("Payment Successful");
            bookingResponse.setStatusCode(HttpStatus.OK.value());
            return new ResponseEntity<>(bookingResponse, HttpStatus.OK);
        } else {

            paymentEntity.setRazorPayPaymentId(paymentData.getRazorPayPaymentId());
            paymentEntity.setPaymentStatus(BAD_STATUS);

            paymentRepository.save(paymentEntity);
            bookingResponse.setMessage("Payment Failed");
            bookingResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
            return new ResponseEntity<>(bookingResponse, HttpStatus.PAYMENT_REQUIRED);
        }
    }
}