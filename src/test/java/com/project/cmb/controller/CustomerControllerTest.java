package com.project.cmb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.cmb.entity.Customer;
import com.project.cmb.entity.Employee;
import com.project.cmb.projection.CustomerListView;
import com.project.cmb.projection.OrderListView;
import com.project.cmb.projection.PaymentListView;
import com.project.cmb.repo.CustomerRepo;
import com.project.cmb.repo.EmployeeRepo;
import com.project.cmb.repo.OrderRepo;
import com.project.cmb.repo.PaymentRepo;
import com.project.cmb.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean CustomerRepo     customerRepo;
    @MockitoBean OrderRepo        orderRepo;
    @MockitoBean PaymentRepo      paymentRepo;
    @MockitoBean CustomerService  customerService;
    @MockitoBean EmployeeRepo     employeeRepo;   // ← was missing; causes context failure without it

    // ─── Helpers ──────────────────────────────────────────────────

    private Customer buildCustomerEntity(Integer number, String name) {
        Customer c = new Customer();
        c.setCustomerNumber(number);
        c.setCustomerName(name);
        c.setContactFirstName("John");
        c.setContactLastName("Doe");
        c.setPhone("40.32.2555");
        c.setAddressLine1("54, rue Royale");
        c.setCity("Nantes");
        c.setCountry("France");
        c.setCreditLimit(new BigDecimal("21000.00"));
        return c;
    }

    private CustomerListView buildCustomerView(Integer number, String name,
                                               String city, String country, BigDecimal credit) {
        return new CustomerListView() {
            public Integer    getCustomerNumber()   { return number; }
            public String     getCustomerName()     { return name;   }
            public String     getContactFirstName() { return "John"; }
            public String     getContactLastName()  { return "Doe";  }
            public String     getCity()             { return city;   }
            public String     getCountry()          { return country;}
            public BigDecimal getCreditLimit()      { return credit; }
        };
    }

    private OrderListView buildOrder(Integer orderNumber, String status) {
        return new OrderListView() {
            public Integer   getOrderNumber()  { return orderNumber; }
            public LocalDate getOrderDate()    { return LocalDate.of(2024, 1, 10); }
            public LocalDate getRequiredDate() { return LocalDate.of(2024, 1, 20); }
            public LocalDate getShippedDate()  { return null; }
            public String    getStatus()       { return status; }
            public CustomerInfo getCustomer()  { return null; }
        };
    }

    private PaymentListView buildPayment(String checkNumber, Integer customerNumber,
                                         BigDecimal amount) {
        return new PaymentListView() {
            public PaymentIdInfo getId() {
                return new PaymentIdInfo() {
                    public String  getCheckNumber()    { return checkNumber;     }
                    public Integer getCustomerNumber() { return customerNumber; }
                };
            }
            public Integer   getOrderNumber()  { return 10100; }
            public LocalDate getPaymentDate()  { return LocalDate.of(2024, 1, 15); }
            public BigDecimal getAmount()      { return amount; }
        };
    }

    // ─── GET /{customerNumber} ────────────────────────────────────

    @Test
    void getCustomer_found_shouldReturn200AndFields() throws Exception {
        Customer c = buildCustomerEntity(103, "Atelier graphique");
        when(customerRepo.findById(103)).thenReturn(Optional.of(c));

        mockMvc.perform(get("/api/v1/customers/103"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.customerNumber").value(103))
                .andExpect(jsonPath("$.customerName").value("Atelier graphique"))
                .andExpect(jsonPath("$.city").value("Nantes"))
                .andExpect(jsonPath("$.country").value("France"));
    }

    @Test
    void getCustomer_notFound_shouldReturn404() throws Exception {
        when(customerRepo.findById(99999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/customers/99999"))
                .andExpect(status().isNotFound());
    }

    // ─── POST /add ────────────────────────────────────────────────

    @Test
    void addCustomer_shouldReturn201AndCustomerNumber() throws Exception {
        Customer last = buildCustomerEntity(200, "Last Customer");
        when(customerRepo.findTopByOrderByCustomerNumberDesc()).thenReturn(last);

        Customer saved = buildCustomerEntity(201, "New Customer");
        when(customerRepo.save(any(Customer.class))).thenReturn(saved);

        Map<String, Object> body = Map.of(
                "customerName",    "New Customer",
                "contactFirstName","Jane",
                "contactLastName", "Smith",
                "phone",           "123-456-7890",
                "addressLine1",    "123 Main St",
                "city",            "Paris",
                "country",         "France",
                "creditLimit",     "15000.00"
        );

        mockMvc.perform(post("/api/v1/customers/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerNumber").value(201))
                .andExpect(jsonPath("$.message").value("Customer created"));
    }

    @Test
    void addCustomer_withSalesRep_shouldReturn201() throws Exception {
        Customer last = buildCustomerEntity(200, "Last Customer");
        when(customerRepo.findTopByOrderByCustomerNumberDesc()).thenReturn(last);

        Employee emp = new Employee();
        emp.setEmployeeNumber(1370);
        when(employeeRepo.findById(1370)).thenReturn(Optional.of(emp));

        Customer saved = buildCustomerEntity(201, "New Customer");
        when(customerRepo.save(any(Customer.class))).thenReturn(saved);

        Map<String, Object> body = Map.of(
                "customerName",            "New Customer",
                "addressLine1",            "123 Main St",
                "city",                    "Paris",
                "country",                 "France",
                "salesRepEmployeeNumber",  "1370"
        );

        mockMvc.perform(post("/api/v1/customers/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Customer created"));
    }

    // ─── PUT /update/{customerNumber} ────────────────────────────

    @Test
    void updateCustomer_found_shouldReturn200() throws Exception {
        Customer c = buildCustomerEntity(103, "Atelier graphique");
        when(customerRepo.findById(103)).thenReturn(Optional.of(c));
        when(customerRepo.save(any(Customer.class))).thenReturn(c);

        Map<String, Object> body = Map.of(
                "customerName", "Atelier graphique Updated",
                "city",         "Lyon",
                "creditLimit",  "25000.00"
        );

        mockMvc.perform(put("/api/v1/customers/update/103")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Customer updated"));
    }

    @Test
    void updateCustomer_notFound_shouldReturn404() throws Exception {
        when(customerRepo.findById(99999)).thenReturn(Optional.empty());

        Map<String, Object> body = Map.of("customerName", "Ghost");

        mockMvc.perform(put("/api/v1/customers/update/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    // ─── GET /stats ───────────────────────────────────────────────

    @Test
    void getStats_shouldReturn200AndFields() throws Exception {
        when(customerService.getTotalCustomers()).thenReturn(122L);
        when(customerService.getTotalCountries()).thenReturn(28L);
        when(customerService.getAvgCreditLimit()).thenReturn(new BigDecimal("67659.05"));

        mockMvc.perform(get("/api/v1/customers/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalCustomers").value(122))
                .andExpect(jsonPath("$.totalCountries").value(28))
                .andExpect(jsonPath("$.avgCreditLimit").exists());
    }

    // ─── GET /search ──────────────────────────────────────────────

    @Test
    void searchByName_shouldReturn200AndPage() throws Exception {
        CustomerListView c = buildCustomerView(103, "Atelier graphique",
                "Nantes", "France", new BigDecimal("21000.00"));

        when(customerRepo.findByCustomerNameContainingIgnoreCase(eq("atelier"), any()))
                .thenReturn(new PageImpl<>(List.of(c), PageRequest.of(0, 5), 1));

        mockMvc.perform(get("/api/v1/customers/search").param("name", "atelier"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].customerNumber").value(103))
                .andExpect(jsonPath("$.content[0].city").value("Nantes"))
                .andExpect(jsonPath("$.content[0].country").value("France"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void searchByName_noMatch_shouldReturnEmptyPage() throws Exception {
        when(customerRepo.findByCustomerNameContainingIgnoreCase(eq("xyzxyz"), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/customers/search").param("name", "xyzxyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ─── GET /search/phone ────────────────────────────────────────

    @Test
    void searchByPhone_shouldReturn200AndPage() throws Exception {
        CustomerListView c = buildCustomerView(103, "Atelier graphique",
                "Nantes", "France", new BigDecimal("21000.00"));

        when(customerRepo.findByPhoneContaining(eq("40.32"), any()))
                .thenReturn(new PageImpl<>(List.of(c), PageRequest.of(0, 5), 1));

        mockMvc.perform(get("/api/v1/customers/search/phone").param("phone", "40.32"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].customerNumber").value(103))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void searchByPhone_noMatch_shouldReturnEmptyPage() throws Exception {
        when(customerRepo.findByPhoneContaining(eq("0000000000"), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/customers/search/phone").param("phone", "0000000000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ─── GET /filter/country ──────────────────────────────────────

    @Test
    void filterByCountry_shouldReturn200AndPage() throws Exception {
        CustomerListView c = buildCustomerView(103, "Atelier graphique",
                "Nantes", "France", new BigDecimal("21000.00"));

        when(customerRepo.findByCountry(eq("France"), any()))
                .thenReturn(new PageImpl<>(List.of(c), PageRequest.of(0, 5), 1));

        mockMvc.perform(get("/api/v1/customers/filter/country").param("country", "France"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].country").value("France"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void filterByCountry_noMatch_shouldReturnEmptyPage() throws Exception {
        when(customerRepo.findByCountry(eq("Antarctica"), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/customers/filter/country").param("country", "Antarctica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ─── GET /filter/credit ───────────────────────────────────────

    @Test
    void filterByCreditLimit_shouldReturn200AndPage() throws Exception {
        CustomerListView c = buildCustomerView(103, "Atelier graphique",
                "Nantes", "France", new BigDecimal("21000.00"));

        when(customerRepo.findByCreditLimitBetween(
                eq(new BigDecimal("5000")), eq(new BigDecimal("25000")), any()))
                .thenReturn(new PageImpl<>(List.of(c), PageRequest.of(0, 5), 1));

        mockMvc.perform(get("/api/v1/customers/filter/credit")
                        .param("min", "5000")
                        .param("max", "25000"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].customerNumber").value(103))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void filterByCreditLimit_noMatch_shouldReturnEmptyPage() throws Exception {
        when(customerRepo.findByCreditLimitBetween(
                eq(new BigDecimal("99000")), eq(new BigDecimal("99999")), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/customers/filter/credit")
                        .param("min", "99000")
                        .param("max", "99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ─── GET /{customerNumber}/orders ─────────────────────────────

    @Test
    void getOrders_shouldReturn200AndList() throws Exception {
        OrderListView o = buildOrder(10100, "Shipped");
        when(orderRepo.findByCustomer_CustomerNumber(103)).thenReturn(List.of(o));

        mockMvc.perform(get("/api/v1/customers/103/orders"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].orderNumber").value(10100))
                .andExpect(jsonPath("$[0].status").value("Shipped"));
    }

    @Test
    void getOrders_noOrders_shouldReturnEmptyList() throws Exception {
        when(orderRepo.findByCustomer_CustomerNumber(99999)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/customers/99999/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ─── GET /{customerNumber}/payments ───────────────────────────

    @Test
    void getPayments_shouldReturn200AndList() throws Exception {
        PaymentListView p = buildPayment("HQ336336", 103, new BigDecimal("6066.78"));
        when(paymentRepo.findById_CustomerNumber(103)).thenReturn(List.of(p));

        mockMvc.perform(get("/api/v1/customers/103/payments"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id.checkNumber").value("HQ336336"))
                .andExpect(jsonPath("$[0].amount").value(6066.78));
    }

    @Test
    void getPayments_noPayments_shouldReturnEmptyList() throws Exception {
        when(paymentRepo.findById_CustomerNumber(99999)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/customers/99999/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}