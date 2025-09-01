package com.gogo.mcp_java_server.repository;

import com.gogo.mcp_java_server.model.Customer;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<Customer,Long> {
    @Modifying
    @Transactional
    @Query("UPDATE Customer c SET c.status= :status,  c.name= :name,  c.phone= :phone,  c.email= :email,  c.address= :address,  c.city= :city,  c.postalCode= :postalCode WHERE c.customerId= :customerId")
    void updateCustomer(@Param("customerId") String customerId, @Param("status") String status, @Param("name") String name, @Param("phone") String phone, @Param("email") String email, @Param("address") String address, @Param("city") String city, @Param("postalCode") String postalCode);


    @Modifying
    @Transactional
    @Query("DELETE FROM Customer c  where c.customerId =:customerId")
    void deleteCustomer(@Param("customerId") String customerId);



    Customer findCustomerByCustomerId(String customerIdEvent);

    boolean existsByCustomerIdAndStatus(String customerId, String status);
}
