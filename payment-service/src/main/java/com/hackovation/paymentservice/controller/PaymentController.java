package com.hackovation.paymentservice.controller;

import com.hackovation.paymentservice.dto.request.FailedPaymentRequest;
import com.hackovation.paymentservice.dto.request.InitiatePaymentRequest;
import com.hackovation.paymentservice.dto.request.VerifyPaymentRequest;
import com.hackovation.paymentservice.model.Payment;
import com.hackovation.paymentservice.service.PaymentService;
import com.razorpay.RazorpayException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/initiate")
    public ResponseEntity<?> initiatePayment(@Valid @RequestBody InitiatePaymentRequest request) throws RazorpayException {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.initiatePayment(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@Valid @RequestBody VerifyPaymentRequest request) throws RazorpayException {
        return ResponseEntity.status(HttpStatus.OK).body(paymentService.verifyPayment(request));
    }

    @PutMapping("/failed")
    public ResponseEntity<?> failurePayment(@Valid @RequestBody FailedPaymentRequest request) {
        return ResponseEntity.ok(paymentService.failPayment(request));
    }
}
