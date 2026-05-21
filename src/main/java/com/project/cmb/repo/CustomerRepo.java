package com.project.cmb.repo;

import com.project.cmb.entity.Customer;
import com.project.cmb.projection.CustomerListView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.math.BigDecimal;

@RepositoryRestResource(path = "customers")
public interface CustomerRepo extends JpaRepository<Customer, Integer> {

    Customer findTopByOrderByCustomerNumberDesc();

    Page<CustomerListView> findByCustomerNameContainingIgnoreCase(String customerName, Pageable pageable);
    Page<CustomerListView> findByPhoneContaining(String phone, Pageable pageable);

    Page<CustomerListView> findByCountry(String country, Pageable pageable);

    Page<CustomerListView> findByCreditLimitBetween(BigDecimal min, BigDecimal max, Pageable pageable);

    Page<CustomerListView> findBySalesRepEmployee_EmployeeNumber(Integer employeeNumber, Pageable pageable);

    boolean existsByCustomerName(String customerName);
    boolean existsByPhone(String phone);
}