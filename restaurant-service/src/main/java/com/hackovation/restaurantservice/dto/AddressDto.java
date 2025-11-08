package com.hackovation.restaurantservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDto {

    @NotBlank
    @Size(min = 2, max = 10, message = "Minimum length should be 2 and maximum length should be 10")
    private String doorNumber;

    @NotBlank
    @Size(min = 2, max = 50, message = "Minimum length should be 2 and maximum length should be 50")
    private String street;

    @NotBlank
    @Size(min = 2, max = 20, message = "Minimum length should be 2 and maximum length should be 20")
    private String area;

    @NotBlank
    @Size(min = 2, max = 100, message = "Minimum length should be 2 and maximum length should be 100")
    private String city;

    @NotBlank
    @Size(min = 2, max = 20, message = "Minimum length should be 2 and maximum length should be 20")
    private String state;

    private String country = "India";

    @Min(100000)
    @Max(999999)
    private Integer zipcode;

}
