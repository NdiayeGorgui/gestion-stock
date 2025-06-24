package com.gogo.order_service.repository;

import com.gogo.order_service.model.Customer;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer,Long> {
    Customer findCustomerById(Long id);

    Customer findCustomerByCustomerIdEvent(String id);
    boolean existsByCustomerIdEventAndStatus(String customerIdEvent,String customerStatus);

    @Modifying
    @Transactional
    @Query("DELETE FROM Customer c  where c.customerIdEvent =:customerIdEvent")
    void deleteCustomer(@Param("customerIdEvent") String customerIdEvent);

    @Modifying
    @Transactional
    @Query("UPDATE Customer c SET c.status= :status,  c.name= :name,  c.phone= :phone,  c.email= :email,  c.address= :address,c.city= :city,c.postalCode= :postalCode WHERE c.customerIdEvent= :customerIdEvent")
    void updateCustomer(@Param("customerIdEvent") String customerIdEvent, @Param("status") String status, @Param("name") String name, @Param("phone") String phone, @Param("email") String email, @Param("address") String address, @Param("city") String city, @Param("postalCode") String postalCode);

    Optional<Customer> findByCustomerIdEvent(String customerIdEvent);
}
