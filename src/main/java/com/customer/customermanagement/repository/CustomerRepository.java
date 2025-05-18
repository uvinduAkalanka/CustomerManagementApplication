package com.customer.customermanagement.repository;

import com.customer.customermanagement.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByNicNumber(String nicNumber);

    boolean existsByNicNumber(String nicNumber);

    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.addresses a LEFT JOIN FETCH a.city LEFT JOIN FETCH a.city.country WHERE c.id = :id")
    Optional<Customer> findByIdWithDetails(Long id);
}
