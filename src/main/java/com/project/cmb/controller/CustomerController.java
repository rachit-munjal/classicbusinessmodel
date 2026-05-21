package com.project.cmb.controller;

import com.project.cmb.entity.Customer;
import com.project.cmb.entity.Order;
import com.project.cmb.entity.Payment;
import com.project.cmb.projection.CustomerListView;
import com.project.cmb.projection.OrderListView;
import com.project.cmb.projection.PaymentListView;
import com.project.cmb.repo.CustomerRepo;
import com.project.cmb.repo.EmployeeRepo;
import com.project.cmb.repo.OrderRepo;
import com.project.cmb.repo.PaymentRepo;
import com.project.cmb.service.CustomerService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/v1/customers")
@AllArgsConstructor
public class CustomerController {

    private final CustomerRepo  customerRepo;
    private final CustomerService customerService;
    private final OrderRepo     orderRepo;
    private final PaymentRepo   paymentRepo;
    private final EmployeeRepo  employeeRepo;

    // ─── GET /{customerNumber} ────────────────────────────────────
    @GetMapping("/{customerNumber}")
    public ResponseEntity<?> getCustomer(@PathVariable Integer customerNumber) {
        Optional<Customer> opt = customerRepo.findById(customerNumber);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Customer c = opt.get();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("customerNumber",   c.getCustomerNumber());
        result.put("customerName",     c.getCustomerName());
        result.put("contactFirstName", c.getContactFirstName());
        result.put("contactLastName",  c.getContactLastName());
        result.put("phone",            c.getPhone());
        result.put("addressLine1",     c.getAddressLine1());
        result.put("addressLine2",     c.getAddressLine2());
        result.put("city",             c.getCity());
        result.put("state",            c.getState());
        result.put("postalCode",       c.getPostalCode());
        result.put("country",          c.getCountry());
        result.put("creditLimit",      c.getCreditLimit());
        if (c.getSalesRepEmployee() != null) {
            result.put("salesRepEmployeeNumber", c.getSalesRepEmployee().getEmployeeNumber());
        }
        return ResponseEntity.ok(result);
    }

    // ─── POST /add ────────────────────────────────────────────────
    @PostMapping("/add")
    public ResponseEntity<?> addCustomer(@RequestBody Map<String, Object> dto) {
        // MySQL customerNumber has no AUTO_INCREMENT — generate next ID in Java
        Customer last = customerRepo.findTopByOrderByCustomerNumberDesc();
        int nextNumber = (last != null ? last.getCustomerNumber() : 100) + 1;

        Customer c = new Customer();
        c.setCustomerNumber(nextNumber);
        c.setCustomerName((String)     dto.get("customerName"));
        c.setContactFirstName((String) dto.getOrDefault("contactFirstName", ""));
        c.setContactLastName((String)  dto.getOrDefault("contactLastName",  ""));
        c.setPhone((String)            dto.getOrDefault("phone", ""));
        c.setAddressLine1((String)     dto.getOrDefault("addressLine1", ""));
        c.setCity((String)             dto.getOrDefault("city", ""));
        c.setCountry((String)          dto.getOrDefault("country", ""));
        if (dto.get("creditLimit") != null && !dto.get("creditLimit").toString().isBlank())
            c.setCreditLimit(new BigDecimal(dto.get("creditLimit").toString()));
        if (dto.get("salesRepEmployeeNumber") != null && !dto.get("salesRepEmployeeNumber").toString().isBlank()) {
            Integer empNo = Integer.valueOf(dto.get("salesRepEmployeeNumber").toString());
            employeeRepo.findById(empNo).ifPresent(c::setSalesRepEmployee);
        }
        Customer saved = customerRepo.save(c);
        return ResponseEntity.status(201).body(
                Map.of("customerNumber", saved.getCustomerNumber(), "message", "Customer created"));
    }

    // ─── PUT /update/{customerNumber} ────────────────────────────
    @PutMapping("/update/{customerNumber}")
    public ResponseEntity<?> updateCustomer(
            @PathVariable Integer customerNumber,
            @RequestBody Map<String, Object> dto) {
        Customer c = customerRepo.findById(customerNumber)
                .orElseThrow(() -> new com.project.cmb.exception.ResourceNotFoundException(
                        "Customer", "customerNumber", customerNumber));
        if (dto.get("customerName")     != null) c.setCustomerName((String)     dto.get("customerName"));
        if (dto.get("contactFirstName") != null) c.setContactFirstName((String) dto.get("contactFirstName"));
        if (dto.get("contactLastName")  != null) c.setContactLastName((String)  dto.get("contactLastName"));
        if (dto.get("phone")            != null) c.setPhone((String)            dto.get("phone"));
        if (dto.get("addressLine1")     != null) c.setAddressLine1((String)     dto.get("addressLine1"));
        if (dto.get("city")             != null) c.setCity((String)             dto.get("city"));
        if (dto.get("country")          != null) c.setCountry((String)          dto.get("country"));
        if (dto.get("creditLimit")      != null && !dto.get("creditLimit").toString().isBlank())
            c.setCreditLimit(new BigDecimal(dto.get("creditLimit").toString()));
        if (dto.get("salesRepEmployeeNumber") != null && !dto.get("salesRepEmployeeNumber").toString().isBlank()) {
            Integer empNo = Integer.valueOf(dto.get("salesRepEmployeeNumber").toString());
            employeeRepo.findById(empNo).ifPresent(c::setSalesRepEmployee);
        }
        customerRepo.save(c);
        return ResponseEntity.ok(Map.of("message", "Customer updated"));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCustomers", customerService.getTotalCustomers());
        stats.put("totalCountries", customerService.getTotalCountries());
        stats.put("avgCreditLimit", customerService.getAvgCreditLimit());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<CustomerListView>> searchByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(customerRepo.findByCustomerNameContainingIgnoreCase(name, pageable));
    }

    @GetMapping("/search/phone")
    public ResponseEntity<Page<CustomerListView>> searchByPhone(
            @RequestParam String phone,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(customerRepo.findByPhoneContaining(phone, pageable));
    }

    @GetMapping("/filter/country")
    public ResponseEntity<Page<CustomerListView>> filterByCountry(
            @RequestParam String country,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(customerRepo.findByCountry(country, pageable));
    }

    @GetMapping("/filter/credit")
    public ResponseEntity<Page<CustomerListView>> filterByCreditLimit(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(customerRepo.findByCreditLimitBetween(min, max, pageable));
    }

    @GetMapping("/{customerNumber}/orders")
    public ResponseEntity<List<OrderListView>> getOrders(@PathVariable Integer customerNumber) {
        return ResponseEntity.ok(orderRepo.findByCustomer_CustomerNumber(customerNumber));
    }

    @GetMapping("/{customerNumber}/payments")
    public ResponseEntity<List<PaymentListView>> getPayments(@PathVariable Integer customerNumber) {
        return ResponseEntity.ok(paymentRepo.findById_CustomerNumber(customerNumber));
    }
}