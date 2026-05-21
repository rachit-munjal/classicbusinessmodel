package com.project.cmb.service;

import com.project.cmb.entity.Customer;
import com.project.cmb.repo.CustomerRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@AllArgsConstructor
public class CustomerService {

    private final CustomerRepo customerRepo;

    @Transactional(readOnly = true)
    public long getTotalCustomers() {
        return customerRepo.count();
    }

    @Transactional(readOnly = true)
    public long getTotalCountries() {
        return customerRepo.findAll()
                .stream()
                .map(Customer::getCountry)
                .distinct()
                .count();
    }

    @Transactional(readOnly = true)
    public BigDecimal getAvgCreditLimit() {
        List<Customer> all = customerRepo.findAll();
        if (all.isEmpty()) return BigDecimal.ZERO;
        BigDecimal total = all.stream()
                .map(Customer::getCreditLimit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(new BigDecimal(all.size()), 2, RoundingMode.HALF_UP);
    }
}