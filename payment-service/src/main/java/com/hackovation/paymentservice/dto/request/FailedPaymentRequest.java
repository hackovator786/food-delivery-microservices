package com.hackovation.paymentservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FailedPaymentRequest {
    private String orderId;
    private String razorpayOrderId;
}
