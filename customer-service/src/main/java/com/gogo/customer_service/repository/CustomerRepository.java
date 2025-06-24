package com.gogo.customer_service.repository;

import com.gogo.customer_service.model.CustomerModel;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<CustomerModel,Long> {

    CustomerModel findCustomerByCustomerIdEvent(String id);

    @Modifying
    @Transactional
    @Query("UPDATE CustomerModel c SET c.status= :status,  c.name= :name,  c.phone= :phone,  c.email= :email,  c.address= :address,  c.city= :city,  c.postalCode= :postalCode WHERE c.customerIdEvent= :customerIdEvent")
    void updateCustomer(@Param("customerIdEvent") String customerIdEvent, @Param("status") String status, @Param("name") String name, @Param("phone") String phone, @Param("email") String email, @Param("address") String address,@Param("city") String city,@Param("postalCode") String postalCode);

    @Modifying
    @Query("UPDATE CustomerModel c SET c.status= :status WHERE c.customerIdEvent= :customerIdEvent")
    void updateCustomerStatus(@Param("customerIdEvent") String customerIdEvent, @Param("status") String status);

    @Modifying
    @Transactional
    @Query("DELETE FROM CustomerModel c  where c.customerIdEvent =:customerIdEvent")
    void deleteCustomer(@Param("customerIdEvent") String customerIdEvent, @Param("customerIdEvent") String status);

    boolean existsByEmail(String email);
}
