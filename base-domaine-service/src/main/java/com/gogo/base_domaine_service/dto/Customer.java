package com.gogo.base_domaine_service.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Customer {
//
    private String customerIdEvent;
    @NotBlank
    private String name;
    private String address;
    private String city;
    private String postalCode;
    private String phone;
    @Email(message = "L'adresse Email saisie est invalide")
    private String email;


}
