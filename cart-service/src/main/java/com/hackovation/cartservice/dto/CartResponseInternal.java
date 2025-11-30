package com.hackovation.cartservice.dto;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponseInternal extends CartResponse{
    private Double totalAmount;
    private Double tax;
    private Double discount;
}
