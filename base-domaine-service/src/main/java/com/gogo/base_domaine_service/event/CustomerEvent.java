package com.gogo.base_domaine_service.event;
import com.gogo.base_domaine_service.dto.Customer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerEvent {
    private String message;
    private String status;
    private Customer customer;
}
