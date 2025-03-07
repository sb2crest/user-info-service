package com.user.info.service.service;

import com.user.info.service.entity.BookingEntity;
import com.user.info.service.entity.PaymentEntity;
import com.user.info.service.entity.SlotsEntity;
import com.user.info.service.exception.BookingException;
import com.user.info.service.exception.ResStatus;
import com.user.info.service.dto.BookingResponse;
import com.user.info.service.dto.PaymentData;
import com.user.info.service.dto.PaymentDto;
import com.user.info.service.dto.PaymentResponse;
import com.user.info.service.model.BookingStatusEnum;
import com.user.info.service.repository.BookingRepo;
import com.user.info.service.repository.PaymentRepository;
import com.user.info.service.repository.SlotsRepo;
import com.user.info.service.util.Mapper;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    private RazorpayClient razorPayClient;

    @Value("${razorpay.api.key.id}")
    private String keyID;

    @Value("${razorpay.api.key.secret}")
    private String keySecret;

    private final String STATUS = "SUCCESS";
    private final String BAD_STATUS = "FAILED";

    @Autowired
    BookingRepo bookingRepo;

    @Autowired
    SlotsRepo slotsRepo;

    @Autowired
    Mapper mapper;

    private final DateTimeFormatter localDateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss");

    public PaymentServiceImpl() {
    }

    public PaymentServiceImpl(RazorpayClient razorPayClient) {
        this.razorPayClient = razorPayClient;
    }

    @Override
    public PaymentResponse createPayment(PaymentDto paymentDto) {
        PaymentResponse response = new PaymentResponse();

        BookingEntity bookingEntity = bookingRepo.validateUsingIdAndMobile(paymentDto.getBookingId(), paymentDto.getMobile());
        if (bookingEntity != null) {
            try {

                SlotsEntity slotsEntity = slotsRepo.findByBookingId(paymentDto.getBookingId());
                if (slotsEntity == null) {
                    PaymentEntity isPaidForBooking = paymentRepository.isSlotBooked(bookingEntity.getFromDate(), bookingEntity.getToDate(), bookingEntity.getVehicleNumber());
                    if(isPaidForBooking != null){
                        throw new BookingException(ResStatus.SLOT_ALREADY_BOOKED);
                    }
                    mapper.saveSlot(bookingEntity);
                }

                razorPayClient = new RazorpayClient(keyID, keySecret);

                JSONObject orderRequest = new JSONObject();
                orderRequest.put("amount", paymentDto.getAmount() * 100);
                orderRequest.put("currency", "INR");
                orderRequest.put("receipt", "payment_receipt_" + System.currentTimeMillis());

                Order razorpayOrder = razorPayClient.orders.create(orderRequest);
                String razorpayOrderId = razorpayOrder.get("id");

                PaymentEntity paymentEntity = new PaymentEntity();
                paymentEntity.setRazorPayOrderId(razorpayOrderId);
                paymentEntity.setAmount(paymentDto.getAmount());
                paymentEntity.setPaymentDate(LocalDateTime.now());
                paymentEntity.setBookingId(paymentDto.getBookingId());
                paymentRepository.save(paymentEntity);

                response.setStatus("success");
                response.setMessage("Payment created successfully");
                response.setRazorPayOrderId(razorpayOrderId);
                response.setPaymentDate(localDateFormat.format(paymentEntity.getPaymentDate()));
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
        validationPaymentEntity(paymentEntity);

        BookingResponse bookingResponse = new BookingResponse();
        bookingResponse.setBookingId(paymentEntity.getBookingId());
        if (generatedSignature != null && generatedSignature.equals(paymentData.getRazorPaySignature())) {

            paymentEntity.setRazorPayPaymentId(paymentData.getRazorPayPaymentId());
            paymentEntity.setPaymentStatus(STATUS);

            BookingEntity bookingEntity = bookingRepo.getByBookingId(paymentEntity.getBookingId());
            bookingEntity.setBookingStatus(BookingStatusEnum.BOOKED.getCode());
            double paid = bookingEntity.getAdvanceAmountPaid() != null ? bookingEntity.getAdvanceAmountPaid() : 0.00;
            bookingEntity.setAdvanceAmountPaid(paid + paymentEntity.getAmount());
            bookingEntity.setRemainingAmount(bookingEntity.getTotalAmount() - bookingEntity.getAdvanceAmountPaid());
            bookingRepo.save(bookingEntity);
            paymentRepository.save(paymentEntity);
            bookingResponse.setMessage("Payment Successful");
            bookingResponse.setStatusCode(HttpStatus.OK.value());
            return new ResponseEntity<>(bookingResponse, HttpStatus.OK);
        } else {

            paymentEntity.setRazorPayPaymentId(paymentData.getRazorPayPaymentId());
            paymentEntity.setPaymentStatus(BAD_STATUS);
            SlotsEntity slotsEntity = slotsRepo.findByBookingId(paymentEntity.getBookingId());
            slotsRepo.delete(slotsEntity);
            paymentRepository.save(paymentEntity);
            bookingResponse.setMessage("Payment Failed");
            bookingResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
            return new ResponseEntity<>(bookingResponse, HttpStatus.PAYMENT_REQUIRED);
        }
    }

    private void validationPaymentEntity(PaymentEntity paymentEntity) {
        if (paymentEntity == null) {
            throw new BookingException(ResStatus.PAYMENT_DETAILS_NOT_FOUND);
        }
    }
}