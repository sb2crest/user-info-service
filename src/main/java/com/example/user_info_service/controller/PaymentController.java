package com.example.user_info_service.controller;

import com.example.user_info_service.pojo.PaymentPojo;
import com.example.user_info_service.pojo.PaymentResponse;
import com.example.user_info_service.service.PaymentService;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {

    @Autowired
    PaymentService paymentService;

    @PostMapping("/createPayment")
    ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentPojo paymentPojo) throws RazorpayException {
        return new ResponseEntity<>(paymentService.createPayment(paymentPojo), HttpStatus.OK);
    }

    @PostMapping("/verifySignature")
    public ResponseEntity<String> verifySignature(
            @RequestParam("razorpayOrderId") String razorPayOrderId,
            @RequestParam("razorPayPaymentId") String razorPayPaymentId,
            @RequestParam("signature") String signature) {

        boolean isValid = paymentService.verifyRazorpaySignature(razorPayOrderId, razorPayPaymentId, signature);

        if (isValid) {
            return ResponseEntity.ok("Signature is valid");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Signature is not valid");
        }
    }

}
