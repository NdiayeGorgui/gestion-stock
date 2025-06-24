package com.gogo.base_domaine_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerEventDto {
    private String customerIdEvent;
    private String customerStatus;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String city;
    private String postalCode;
}
