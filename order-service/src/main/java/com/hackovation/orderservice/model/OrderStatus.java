package com.hackovation.orderservice.model;

public enum OrderStatus {
    PAYMENT_PENDING,
    PAYMENT_FAILED,
    PENDING,
    ACCEPTED,
    PREPARING,
    OUT_FOR_DELIVERY,
    DELIVERED,
    REJECTED,
    CANCELLED
}
