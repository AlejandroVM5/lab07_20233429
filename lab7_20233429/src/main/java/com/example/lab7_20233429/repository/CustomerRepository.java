package com.example.lab7_20233429.repository;


import com.example.lab7_20233429.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Optional<Customer> findByDocumentNumber(String documentNumber);
}