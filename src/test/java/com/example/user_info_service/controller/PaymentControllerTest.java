package com.example.user_info_service.controller;

import com.example.user_info_service.model.TestUtil;
import com.example.user_info_service.pojo.PaymentData;
import com.example.user_info_service.pojo.PaymentPojo;
import com.example.user_info_service.pojo.PaymentResponse;
import com.example.user_info_service.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

   @MockBean
    PaymentService paymentService;

    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }

    @Test
    void createPayment() throws Exception {
        PaymentPojo paymentPojo = getPaymentPojo();
        when(paymentService.createPayment(Mockito.any())).thenReturn(new PaymentResponse());
        mvc.perform(post("/createPayment").content(TestUtil.convertObjectToJsonBytes(paymentPojo))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    void verifySignatureWhenSignatureIsValid() throws Exception {
        PaymentData paymentData = getPaymentData();
        when(paymentService.verifyRazorpaySignature(Mockito.any())).thenReturn(true);
        mvc.perform(post("/verifySignature").content(TestUtil.convertObjectToJsonBytes(paymentData))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void verifySignatureWhenSignatureIsInvalid() throws Exception {
        PaymentData paymentData = getPaymentData();
        when(paymentService.verifyRazorpaySignature(Mockito.any())).thenReturn(false);
        mvc.perform(post("/verifySignature").content(TestUtil.convertObjectToJsonBytes(paymentData))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private PaymentPojo getPaymentPojo() {
        PaymentPojo paymentPojo = new PaymentPojo();
        paymentPojo.setBookingId("NB34ye");
        paymentPojo.setAmount(400);
        return paymentPojo;
    }

    private PaymentData getPaymentData(){
        PaymentData  paymentData = new PaymentData();
        paymentData.setRazorPayOrderId("abc");
        paymentData.setRazorPayPaymentId("abd");
        paymentData.setRazorPayPaymentId("xyz");
        return paymentData;
    }
}