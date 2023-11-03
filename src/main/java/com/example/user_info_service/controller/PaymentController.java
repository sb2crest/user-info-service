package com.example.user_info_service.controller;

import com.example.user_info_service.pojo.BookingResponse;
import com.example.user_info_service.pojo.PaymentData;
import com.example.user_info_service.pojo.PaymentPojo;
import com.example.user_info_service.pojo.PaymentResponse;
import com.example.user_info_service.service.PaymentService;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:8100")
public class PaymentController {

    @Autowired
    PaymentService paymentService;

    @PostMapping("/createPayment")
    ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentPojo paymentPojo) throws RazorpayException {
        return new ResponseEntity<>(paymentService.createPayment(paymentPojo), HttpStatus.OK);
    }

    @PostMapping("/verifySignature")
    public ResponseEntity<BookingResponse> verifySignature(
            @RequestBody PaymentData paymentData) {

        return new ResponseEntity<>(paymentService.verifyRazorpaySignature(paymentData), HttpStatus.OK);
    }

}
