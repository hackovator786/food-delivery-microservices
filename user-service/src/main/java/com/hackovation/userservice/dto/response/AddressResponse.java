package com.hackovation.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponse {
    private String doorNumber;
    private String street;
    private String area;
    private String city;
    private String state;
    private String country = "India";
    private Integer zipcode;
}
