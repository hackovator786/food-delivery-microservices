package com.hackovation.paymentservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "payments",
        uniqueConstraints = {
                @UniqueConstraint(name = "transaction_id_unique", columnNames = "transaction_id"),
        },
        indexes = {
                @Index(name = "transaction_id_index", columnList = "transaction_id"),
        }
)
public class Payment extends BaseModel{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Transaction ID cannot be blank")
    private String transactionId;

    @NotBlank(message = "User ID cannot be blank")
    private String userId;

    @NotBlank(message = "Order ID cannot be blank")
    private String orderId;

    @NotBlank(message = "Razorpay Order ID cannot be blank")
    private String razorpayOrderId;

    private String razorpayPaymentId;

    private Long transactionTime = Instant.now().toEpochMilli();

    @NotNull(message = "Amount cannot be null")
    private Double amountPaid;

    @NotBlank(message = "Currency cannot be blank")
    @Size(min = 3, max = 3)
    private String currency = "INR";

    @NotNull(message = "Payment status cannot be null")
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @NotNull(message = "Gateway fee cannot be null")
    private Double gatewayFee = 0.0;

    @NotNull(message = "Signature verification cannot be null")
    private Boolean signatureVerified = false;

    @PrePersist
    public void prePersist() {
        super.prePersist();
    }

    @PreUpdate
    public void preUpdate() {
        super.preUpdate();
    }

}
