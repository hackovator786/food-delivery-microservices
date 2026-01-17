package com.hackovation.paymentservice.service;

import com.hackovation.paymentservice.dto.request.FailedPaymentRequest;
import com.hackovation.paymentservice.dto.request.InitiatePaymentRequest;
import com.hackovation.paymentservice.dto.request.VerifyPaymentRequest;
import com.hackovation.paymentservice.model.Payment;
import com.hackovation.paymentservice.model.PaymentStatus;
import com.hackovation.paymentservice.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    @Value("${razorpay.api.key}")
    private String razorpayKeyId;

    @Value("${razorpay.api.secret}")
    private String razorpayKeySecret;

    @Autowired
    private PaymentRepository paymentRepository;

    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> initiatePayment(InitiatePaymentRequest request) throws RazorpayException {
        try {
            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", request.getAmount() * 100);
            orderRequest.put("currency", request.getCurrency());
            orderRequest.put("receipt", request.getOrderId());

            Order order = razorpay.orders.create(orderRequest);

            Payment payment = new Payment();
            payment.setTransactionId(UUID.randomUUID().toString());
            payment.setUserId("test-user");
            payment.setOrderId(request.getOrderId());
            payment.setRazorpayOrderId(order.get("id"));
            payment.setAmountPaid(request.getAmount());
            paymentRepository.save(payment);

            return Map.of(
                    "razorpayOrderId", order.get("id"),
                    "currency", order.get("currency")
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> verifyPayment(VerifyPaymentRequest request) throws RazorpayException {
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", request.getRazorpayOrderId());
            options.put("razorpay_payment_id", request.getRazorpayPaymentId());
            options.put("razorpay_signature", request.getRazorpaySignature());

            boolean isVerified = com.razorpay.Utils.verifyPaymentSignature(options, razorpayKeySecret);

            Payment payment = paymentRepository.findByOrderIdAndRazorpayOrderId(request.getOrderId(), request.getRazorpayOrderId())
                    .orElseThrow(()-> new RuntimeException("Payment info not found for order ID: " + request.getOrderId()));

            payment.setRazorpayPaymentId(request.getRazorpayPaymentId());

            if (isVerified) {
                payment.setPaymentStatus(PaymentStatus.SUCCESS);
                payment.setSignatureVerified(true);
                paymentRepository.save(payment);
                return Map.of("status", "SUCCESS", "message", "Payment successful");
            } else {
                payment.setPaymentStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                return Map.of("status", "FAILED", "message", "Payment failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> failPayment(FailedPaymentRequest request) {
        try {
            Payment payment = paymentRepository.findByOrderIdAndRazorpayOrderId(request.getOrderId(), request.getRazorpayOrderId())
                    .orElseThrow(()-> new RuntimeException("Payment info not found for order ID: " + request.getOrderId()));

            payment.setPaymentStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Map.of("status", "FAILED", "message", "Payment failed");
    }

}
