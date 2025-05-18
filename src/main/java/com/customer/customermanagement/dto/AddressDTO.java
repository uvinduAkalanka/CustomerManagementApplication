package com.customer.customermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {

    private Long id;
    private String addressLine1;
    private String addressLine2;
    private Long cityId;
    private String cityName;
    private Long countryId;
    private String countryName;
}