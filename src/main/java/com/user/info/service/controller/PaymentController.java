package com.user.info.service.controller;

import com.user.info.service.dto.BookingResponse;
import com.user.info.service.dto.PaymentData;
import com.user.info.service.dto.PaymentDto;
import com.user.info.service.dto.PaymentResponse;
import com.user.info.service.service.PaymentService;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = {"http://localhost:8100","http://nandubus.in"})
public class PaymentController {

    @Autowired
    PaymentService paymentService;

    @PostMapping("/createPayment")
    ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentDto paymentDto) throws RazorpayException {
        return new ResponseEntity<>(paymentService.createPayment(paymentDto), HttpStatus.OK);
    }

    @PostMapping("/verifySignature")
    public ResponseEntity<BookingResponse> verifySignature(
            @RequestBody PaymentData paymentData) {

        return paymentService.verifyRazorpaySignature(paymentData);
    }

}
