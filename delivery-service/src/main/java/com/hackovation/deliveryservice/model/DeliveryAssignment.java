package com.hackovation.deliveryservice.model;

import lombok.Builder;

@Builder
public class DeliveryAssignment {
    private String deliveryAgentName;
    private Address deliveryAgentCurrentAddress;
    private Long deliveryAgentPhoneNumber;
    private OrderDetails orderDetails;
    private Long deliveryTime;
}
