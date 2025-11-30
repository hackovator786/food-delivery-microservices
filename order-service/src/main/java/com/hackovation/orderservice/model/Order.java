package com.hackovation.orderservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "orders",
        uniqueConstraints = {
                @UniqueConstraint(name = "order_id_unique", columnNames = "order_id"),
                @UniqueConstraint(name = "transaction_id_unique", columnNames = "transaction_id"),
        },
        indexes = {
                @Index(name = "order_id_index", columnList = "order_id"),
                @Index(name = "transaction_id_index", columnList = "transaction_id"),
        }
)
public class Order extends BaseModel{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Order Id cannot be blank")
    private String orderId;

    @NotBlank(message = "User Id cannot be blank")
    private String userId;

    @NotBlank(message = "Restaurant Id cannot be blank")
    private String restaurantId;

    @NotNull(message = "Order Items cannot be null")
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;

    @NotNull(message = "Total Amount cannot be null")
    private BigDecimal totalAmount;

    @NotNull(message = "Sub Total cannot be null")
    private BigDecimal subTotal;

    @NotNull(message = "Delivery Charge cannot be null")
    private BigDecimal deliveryCharge;

    @NotNull(message = "Tax cannot be null")
    private BigDecimal tax;

    @NotNull(message = "Discount cannot be null")
    private BigDecimal discount;

    @NotNull(message = "Order Time cannot be null")
    private Long orderTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false, foreignKey = @ForeignKey(name = "fk_address_user"))
    private Address address;

    @NotNull(message = "Expected Delivery Time cannot be null")
    private Long expectedDeliveryTime;

    @NotNull(message = "Order Status cannot be null")
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @NotNull(message = "Payment Status cannot be null")
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private String transactionId;
}
