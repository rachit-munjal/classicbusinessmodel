//
//package com.project.cmb.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.project.cmb.entity.Order;
//import com.project.cmb.projection.OrderListView;
//import com.project.cmb.repo.OrderRepo;
//import com.project.cmb.service.OrderService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(OrderController.class)
//class OrderControllerTest {
//
//    @Autowired MockMvc mockMvc;
//    @Autowired ObjectMapper objectMapper;
//    @MockBean OrderRepo orderRepo;
//    @MockBean OrderService orderService;
//
//    // --- helpers ---
//
//    private OrderListView buildOrderView(Integer orderNumber, String status,
//                                         Integer customerNumber, String customerName) {
//        return new OrderListView() {
//            public Integer getOrderNumber() { return orderNumber; }
//            public LocalDate getOrderDate() { return LocalDate.of(2024, 1, 10); }
//            public LocalDate getRequiredDate() { return LocalDate.of(2024, 1, 20); }
//            public LocalDate getShippedDate() { return null; }
//            public String getStatus() { return status; }
//            public CustomerInfo getCustomer() {
//                return new CustomerInfo() {
//                    public Integer getCustomerNumber() { return customerNumber; }
//                    public String getCustomerName() { return customerName; }
//                };
//            }
//        };
//    }
//
//    private Order buildOrder(Integer orderNumber, String status) {
//        Order o = new Order();
//        o.setOrderNumber(orderNumber);
//        o.setOrderDate(LocalDate.of(2024, 1, 10));
//        o.setRequiredDate(LocalDate.of(2024, 1, 20));
//        o.setStatus(status);
//        return o;
//    }
//
//    // --- GET /api/v1/orders/stats ---
//
//    @Test
//    void getStats_shouldReturn200AndFields() throws Exception {
//        when(orderService.getTotalOrders()).thenReturn(326L);
//        when(orderService.getTotalShipped()).thenReturn(303L);
//        when(orderService.getTotalInProcess()).thenReturn(6L);
//        when(orderService.getTotalCancelled()).thenReturn(6L);
//
//        mockMvc.perform(get("/api/v1/orders/stats"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.totalOrders").value(326))
//                .andExpect(jsonPath("$.totalShipped").value(303))
//                .andExpect(jsonPath("$.totalInProcess").value(6))
//                .andExpect(jsonPath("$.totalCancelled").value(6));
//    }
//
//    // --- GET /api/v1/orders/filter/status?status=Shipped ---
//
//    @Test
//    void filterByStatus_shouldReturn200AndList() throws Exception {
//        OrderListView o = buildOrderView(10100, "Shipped", 363, "Online Diecast Creations Co.");
//
//        when(orderRepo.findByStatus("Shipped")).thenReturn(List.of(o));
//
//        mockMvc.perform(get("/api/v1/orders/filter/status").param("status", "Shipped"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$").isArray())
//                .andExpect(jsonPath("$[0].orderNumber").value(10100))
//                .andExpect(jsonPath("$[0].status").value("Shipped"))
//                .andExpect(jsonPath("$[0].customer.customerNumber").value(363));
//    }
//
//    @Test
//    void filterByStatus_noMatch_shouldReturnEmptyList() throws Exception {
//        when(orderRepo.findByStatus("NonExistent")).thenReturn(List.of());
//
//        mockMvc.perform(get("/api/v1/orders/filter/status").param("status", "NonExistent"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$").isArray())
//                .andExpect(jsonPath("$.length()").value(0));
//    }
//
//    // --- GET /api/v1/orders/filter/date?start=2024-01-01&end=2024-12-31 ---
//
//    @Test
//    void filterByDate_shouldReturn200AndList() throws Exception {
//        OrderListView o = buildOrderView(10100, "Shipped", 363, "Online Diecast Creations Co.");
//
//        when(orderRepo.findByOrderDateBetween(
//                eq(LocalDate.of(2024, 1, 1)),
//                eq(LocalDate.of(2024, 12, 31))))
//                .thenReturn(List.of(o));
//
//        mockMvc.perform(get("/api/v1/orders/filter/date")
//                        .param("start", "2024-01-01")
//                        .param("end", "2024-12-31"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$").isArray())
//                .andExpect(jsonPath("$[0].orderNumber").value(10100));
//    }
//
//    @Test
//    void filterByDate_noMatch_shouldReturnEmptyList() throws Exception {
//        when(orderRepo.findByOrderDateBetween(any(), any())).thenReturn(List.of());
//
//        mockMvc.perform(get("/api/v1/orders/filter/date")
//                        .param("start", "2000-01-01")
//                        .param("end", "2000-12-31"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.length()").value(0));
//    }
//
//    // --- GET /api/v1/orders/search?customerName=alpha ---
//
//    @Test
//    void searchByCustomerName_shouldReturn200AndPage() throws Exception {
//        OrderListView o = buildOrderView(10100, "Shipped", 363, "Online Diecast Creations Co.");
//
//        when(orderRepo.findByCustomer_CustomerNameContainingIgnoreCase(eq("online"), any()))
//                .thenReturn(new PageImpl<>(List.of(o), PageRequest.of(0, 10), 1));
//
//        mockMvc.perform(get("/api/v1/orders/search").param("customerName", "online"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.content[0].customer.customerName")
//                        .value("Online Diecast Creations Co."))
//                .andExpect(jsonPath("$.totalElements").value(1));
//    }
//
//    // --- GET /api/v1/orders/customer/{customerNumber} ---
//
//    @Test
//    void getByCustomer_shouldReturn200AndList() throws Exception {
//        OrderListView o = buildOrderView(10100, "Shipped", 103, "Atelier graphique");
//
//        when(orderRepo.findByCustomer_CustomerNumber(103)).thenReturn(List.of(o));
//
//        mockMvc.perform(get("/api/v1/orders/customer/103"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$[0].orderNumber").value(10100))
//                .andExpect(jsonPath("$[0].customer.customerNumber").value(103));
//    }
//
//    @Test
//    void getByCustomer_noOrders_shouldReturnEmptyList() throws Exception {
//        when(orderRepo.findByCustomer_CustomerNumber(99999)).thenReturn(List.of());
//
//        mockMvc.perform(get("/api/v1/orders/customer/99999"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.length()").value(0));
//    }
//
//    // --- GET /api/v1/orders/{orderNumber}/financials ---
//
//    @Test
//    void getFinancials_shouldReturn200AndAllFields() throws Exception {
//        when(orderService.getOrderTotal(10100))
//                .thenReturn(new BigDecimal("1000.00"));
//        when(orderService.getTotalPaymentReceived(10100))
//                .thenReturn(new BigDecimal("800.00"));
//        when(orderService.getPendingPayment(10100))
//                .thenReturn(new BigDecimal("200.00"));
//
//        mockMvc.perform(get("/api/v1/orders/10100/financials"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.orderTotal").value(1000.00))
//                .andExpect(jsonPath("$.totalPaymentReceived").value(800.00))
//                .andExpect(jsonPath("$.pendingPayment").value(200.00));
//    }
//
//    // --- POST /api/v1/orders/create ---
//
//    @Test
//    void createOrder_shouldReturn200AndOrder() throws Exception {
//        Order savedOrder = buildOrder(99999, "In Process");
//
//        when(orderService.createOrder(any(), any())).thenReturn(savedOrder);
//
//        Map<String, Object> requestBody = Map.of(
//                "order", Map.of(
//                        "orderNumber", 99999,
//                        "orderDate", "2024-01-15",
//                        "requiredDate", "2024-01-25",
//                        "status", "In Process"
//                ),
//                "orderDetails", List.of(Map.of(
//                        "id", Map.of("productCode", "S18_1749"),
//                        "quantityOrdered", 5,
//                        "priceEach", 136.00,
//                        "orderLineNumber", 1
//                ))
//        );
//
//        mockMvc.perform(post("/api/v1/orders/create")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(requestBody)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.orderNumber").value(99999))
//                .andExpect(jsonPath("$.status").value("In Process"));
//    }
//
//    // --- PUT /api/v1/orders/{orderNumber}/status/{status} ---
//
//    @Test
//    void updateStatus_shouldReturn200AndUpdatedOrder() throws Exception {
//        Order order = buildOrder(10100, "In Process");
//        Order updated = buildOrder(10100, "Shipped");
//
//        when(orderRepo.findById(10100)).thenReturn(Optional.of(order));
//        when(orderRepo.save(any())).thenReturn(updated);
//
//        mockMvc.perform(put("/api/v1/orders/10100/status/Shipped"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.orderNumber").value(10100))
//                .andExpect(jsonPath("$.status").value("Shipped"));
//    }
//
//    @Test
//    void updateShippedDate_shouldReturn200AndUpdatedOrder() throws Exception {
//        Order order = buildOrder(10100, "Shipped");
//        Order updated = buildOrder(10100, "Shipped");
//        updated.setShippedDate(LocalDate.of(2024, 1, 15));
//
//        when(orderRepo.findById(10100)).thenReturn(Optional.of(order));
//        when(orderRepo.save(any())).thenReturn(updated);
//
//        mockMvc.perform(put("/api/v1/orders/10100/shipped-date/2024-01-15"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.orderNumber").value(10100))
//                .andExpect(jsonPath("$.shippedDate").value("2024-01-15"));
//    }
//
//    @Test
//    void updateStatus_orderNotFound_shouldReturn404() throws Exception {
//        when(orderRepo.findById(99999)).thenReturn(Optional.empty());
//
//        mockMvc.perform(put("/api/v1/orders/99999/status/Shipped"))
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.status").value(404))
//                .andExpect(jsonPath("$.message").exists())
//                .andExpect(jsonPath("$.timestamp").exists());
//    }
//
//    @Test
//    void updateShippedDate_orderNotFound_shouldReturn404() throws Exception {
//        when(orderRepo.findById(99999)).thenReturn(Optional.empty());
//
//        mockMvc.perform(put("/api/v1/orders/99999/shipped-date/2024-01-15"))
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.status").value(404))
//                .andExpect(jsonPath("$.message").exists());
//    }
//
//}


package com.project.cmb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.cmb.entity.Order;
import com.project.cmb.projection.OrderDetailView;
import com.project.cmb.projection.OrderListView;
import com.project.cmb.repo.OrderRepo;
import com.project.cmb.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired MockMvc       mockMvc;
    @Autowired ObjectMapper  objectMapper;

    @MockitoBean OrderRepo    orderRepo;
    @MockitoBean OrderService orderService;

    // ─── Helpers ──────────────────────────────────────────────────

    private OrderListView buildOrderView(Integer orderNumber, String status,
                                         Integer customerNumber, String customerName) {
        return new OrderListView() {
            public Integer   getOrderNumber()  { return orderNumber; }
            public LocalDate getOrderDate()    { return LocalDate.of(2024, 1, 10); }
            public LocalDate getRequiredDate() { return LocalDate.of(2024, 1, 20); }
            public LocalDate getShippedDate()  { return null; }
            public String    getStatus()       { return status; }
            public CustomerInfo getCustomer() {
                return new CustomerInfo() {
                    public Integer getCustomerNumber() { return customerNumber; }
                    public String  getCustomerName()   { return customerName;   }
                };
            }
        };
    }

    private OrderDetailView buildOrderDetailView(Integer orderNumber, String status) {
        return new OrderDetailView() {
            public Integer   getOrderNumber()  { return orderNumber; }
            public LocalDate getOrderDate()    { return LocalDate.of(2024, 1, 10); }
            public LocalDate getRequiredDate() { return LocalDate.of(2024, 1, 20); }
            public LocalDate getShippedDate()  { return null; }
            public String    getStatus()       { return status; }
            public String    getComments()     { return null; }
            public CustomerInfo getCustomer()  { return null; }
        };
    }

    private Order buildOrderEntity(Integer orderNumber, String status) {
        Order o = new Order();
        o.setOrderNumber(orderNumber);
        o.setOrderDate(LocalDate.of(2024, 1, 10));
        o.setRequiredDate(LocalDate.of(2024, 1, 20));
        o.setStatus(status);
        return o;
    }

    // ─── GET /{orderNumber} ───────────────────────────────────────

    @Test
    void getOrder_found_shouldReturn200AndFields() throws Exception {
        OrderDetailView view = buildOrderDetailView(10100, "Shipped");
        when(orderRepo.findByOrderNumber(10100)).thenReturn(Optional.of(view));

        mockMvc.perform(get("/api/v1/orders/10100"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.orderNumber").value(10100))
                .andExpect(jsonPath("$.status").value("Shipped"));
    }

    @Test
    void getOrder_notFound_shouldReturn404() throws Exception {
        when(orderRepo.findByOrderNumber(99999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/orders/99999"))
                .andExpect(status().isNotFound());
    }

    // ─── GET /stats ───────────────────────────────────────────────

    @Test
    void getStats_shouldReturn200AndFields() throws Exception {
        when(orderService.getTotalOrders()).thenReturn(326L);
        when(orderService.getTotalShipped()).thenReturn(303L);
        when(orderService.getTotalInProcess()).thenReturn(6L);
        when(orderService.getTotalCancelled()).thenReturn(6L);

        mockMvc.perform(get("/api/v1/orders/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalOrders").value(326))
                .andExpect(jsonPath("$.totalShipped").value(303))
                .andExpect(jsonPath("$.totalInProcess").value(6))
                .andExpect(jsonPath("$.totalCancelled").value(6));
    }

    // ─── GET /filter/status ───────────────────────────────────────

    @Test
    void filterByStatus_shouldReturn200AndList() throws Exception {
        OrderListView o = buildOrderView(10100, "Shipped", 363, "Online Diecast Creations Co.");
        when(orderRepo.findByStatus("Shipped")).thenReturn(List.of(o));

        mockMvc.perform(get("/api/v1/orders/filter/status").param("status", "Shipped"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].orderNumber").value(10100))
                .andExpect(jsonPath("$[0].status").value("Shipped"))
                .andExpect(jsonPath("$[0].customer.customerNumber").value(363));
    }

    @Test
    void filterByStatus_noMatch_shouldReturnEmptyList() throws Exception {
        when(orderRepo.findByStatus("NonExistent")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/orders/filter/status").param("status", "NonExistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ─── GET /filter/date ─────────────────────────────────────────

    @Test
    void filterByDate_shouldReturn200AndList() throws Exception {
        OrderListView o = buildOrderView(10100, "Shipped", 363, "Online Diecast Creations Co.");

        when(orderRepo.findByOrderDateBetween(
                eq(LocalDate.of(2024, 1, 1)),
                eq(LocalDate.of(2024, 12, 31))))
                .thenReturn(List.of(o));

        mockMvc.perform(get("/api/v1/orders/filter/date")
                        .param("start", "2024-01-01")
                        .param("end",   "2024-12-31"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].orderNumber").value(10100));
    }

    @Test
    void filterByDate_noMatch_shouldReturnEmptyList() throws Exception {
        when(orderRepo.findByOrderDateBetween(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/orders/filter/date")
                        .param("start", "2000-01-01")
                        .param("end",   "2000-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ─── GET /search ──────────────────────────────────────────────

    @Test
    void searchByCustomerName_shouldReturn200AndPage() throws Exception {
        OrderListView o = buildOrderView(10100, "Shipped", 363, "Online Diecast Creations Co.");

        when(orderRepo.findByCustomer_CustomerNameContainingIgnoreCase(eq("online"), any()))
                .thenReturn(new PageImpl<>(List.of(o), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/orders/search").param("customerName", "online"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].customer.customerName")
                        .value("Online Diecast Creations Co."))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void searchByCustomerName_noMatch_shouldReturnEmptyPage() throws Exception {
        when(orderRepo.findByCustomer_CustomerNameContainingIgnoreCase(eq("xyzxyz"), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/orders/search").param("customerName", "xyzxyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ─── GET /customer/{customerNumber} ───────────────────────────

    @Test
    void getByCustomer_shouldReturn200AndList() throws Exception {
        OrderListView o = buildOrderView(10100, "Shipped", 103, "Atelier graphique");
        when(orderRepo.findByCustomer_CustomerNumber(103)).thenReturn(List.of(o));

        mockMvc.perform(get("/api/v1/orders/customer/103"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].orderNumber").value(10100))
                .andExpect(jsonPath("$[0].customer.customerNumber").value(103));
    }

    @Test
    void getByCustomer_noOrders_shouldReturnEmptyList() throws Exception {
        when(orderRepo.findByCustomer_CustomerNumber(99999)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/orders/customer/99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ─── GET /{orderNumber}/financials ────────────────────────────

    @Test
    void getFinancials_shouldReturn200AndAllFields() throws Exception {
        when(orderService.getOrderTotal(10100)).thenReturn(new BigDecimal("1000.00"));
        when(orderService.getTotalPaymentReceived(10100)).thenReturn(new BigDecimal("800.00"));
        when(orderService.getPendingPayment(10100)).thenReturn(new BigDecimal("200.00"));

        mockMvc.perform(get("/api/v1/orders/10100/financials"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.orderTotal").value(1000.00))
                .andExpect(jsonPath("$.totalPaymentReceived").value(800.00))
                .andExpect(jsonPath("$.pendingPayment").value(200.00));
    }

    // ─── POST /create ─────────────────────────────────────────────

    @Test
    void createOrder_shouldReturn201AndResponseFields() throws Exception {
        // Controller computes nextOrderNumber from orderRepo.findAll(), then calls createOrder
        Order saved = buildOrderEntity(10327, "In Process");
        when(orderRepo.findAll()).thenReturn(List.of(buildOrderEntity(10326, "Shipped")));
        when(orderService.createOrder(any(), any())).thenReturn(saved);

        Map<String, Object> requestBody = Map.of(
                "order", Map.of(
                        "orderDate",    "2024-01-15",
                        "requiredDate", "2024-01-25",
                        "status",       "In Process"
                ),
                "orderDetails", List.of(Map.of(
                        "id",              Map.of("productCode", "S18_1749"),
                        "quantityOrdered", 5,
                        "priceEach",       136.00,
                        "orderLineNumber", 1
                ))
        );

        mockMvc.perform(post("/api/v1/orders/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber").value(10327))
                .andExpect(jsonPath("$.status").value("In Process"))
                .andExpect(jsonPath("$.message").value("Order created successfully"));
    }

    // ─── PUT /{orderNumber}/status ────────────────────────────────

    @Test
    void updateStatus_shouldReturn204() throws Exception {
        Order order = buildOrderEntity(10100, "In Process");
        when(orderRepo.findById(10100)).thenReturn(Optional.of(order));
        when(orderRepo.save(any())).thenReturn(order);

        // status is a @RequestParam, not a path segment
        mockMvc.perform(put("/api/v1/orders/10100/status")
                        .param("status", "Shipped"))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateStatus_notFound_shouldReturn404() throws Exception {
        when(orderRepo.findById(99999)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/orders/99999/status")
                        .param("status", "Shipped"))
                .andExpect(status().isNotFound());
    }

    // ─── PUT /{orderNumber}/shipped-date/{date} ───────────────────

    @Test
    void updateShippedDate_shouldReturn204() throws Exception {
        Order order = buildOrderEntity(10100, "Shipped");
        when(orderRepo.findById(10100)).thenReturn(Optional.of(order));
        when(orderRepo.save(any())).thenReturn(order);

        // date is a @PathVariable
        mockMvc.perform(put("/api/v1/orders/10100/shipped-date/2024-01-15"))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateShippedDate_notFound_shouldReturn404() throws Exception {
        when(orderRepo.findById(99999)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/orders/99999/shipped-date/2024-01-15"))
                .andExpect(status().isNotFound());
    }
}