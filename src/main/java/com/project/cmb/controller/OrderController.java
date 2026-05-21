package com.project.cmb.controller;

import com.project.cmb.entity.Order;
import com.project.cmb.entity.OrderDetail;
import com.project.cmb.exception.ResourceNotFoundException;
import com.project.cmb.projection.OrderDetailView;
import com.project.cmb.projection.OrderListView;
import com.project.cmb.repo.OrderRepo;
import com.project.cmb.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@AllArgsConstructor
public class OrderController {

    private final OrderRepo    orderRepo;
    private final OrderService orderService;


    @GetMapping("/{orderNumber}")
    public ResponseEntity<OrderDetailView> getOrder(@PathVariable Integer orderNumber) {
        return orderRepo.findByOrderNumber(orderNumber)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders",    orderService.getTotalOrders());
        stats.put("totalShipped",   orderService.getTotalShipped());
        stats.put("totalInProcess", orderService.getTotalInProcess());
        stats.put("totalCancelled", orderService.getTotalCancelled());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/filter/status")
    public ResponseEntity<List<OrderListView>> filterByStatus(@RequestParam String status) {
        return ResponseEntity.ok(orderRepo.findByStatus(status));
    }

    @GetMapping("/filter/date")
    public ResponseEntity<List<OrderListView>> filterByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(orderRepo.findByOrderDateBetween(start, end));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<OrderListView>> searchByCustomerName(
            @RequestParam String customerName,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                orderRepo.findByCustomer_CustomerNameContainingIgnoreCase(customerName, pageable));
    }

    @GetMapping("/customer/{customerNumber}")
    public ResponseEntity<List<OrderListView>> getByCustomer(@PathVariable Integer customerNumber) {
        return ResponseEntity.ok(orderRepo.findByCustomer_CustomerNumber(customerNumber));
    }

    @GetMapping("/{orderNumber}/financials")
    public ResponseEntity<Map<String, Object>> getFinancials(@PathVariable Integer orderNumber) {
        BigDecimal orderTotal = orderService.getOrderTotal(orderNumber);
        BigDecimal totalPaid  = orderService.getTotalPaymentReceived(orderNumber);
        BigDecimal pending    = orderService.getPendingPayment(orderNumber);

        Map<String, Object> financials = new HashMap<>();
        financials.put("orderTotal",           orderTotal);
        financials.put("totalPaymentReceived", totalPaid);
        financials.put("pendingPayment",       pending);
        return ResponseEntity.ok(financials);
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody CreateOrderRequest request) {
        // MySQL orderNumber has no AUTO_INCREMENT — generate next ID in Java
        int nextOrderNumber = orderRepo.findAll().stream()
                .mapToInt(Order::getOrderNumber)
                .max()
                .orElse(10000) + 1;
        request.getOrder().setOrderNumber(nextOrderNumber);

        Order saved = orderService.createOrder(request.getOrder(), request.getOrderDetails());

        // Return only primitives — avoid lazy-loading Customer proxy in JSON serialization
        Map<String, Object> response = new HashMap<>();
        response.put("orderNumber", saved.getOrderNumber());
        response.put("status",      saved.getStatus());
        response.put("orderDate",   saved.getOrderDate().toString());
        response.put("message",     "Order created successfully");
        return ResponseEntity.status(201).body(response);
    }



    @PutMapping("/{orderNumber}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Integer orderNumber,
            @RequestParam String status) {
        Order order = orderRepo.findById(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));
        order.setStatus(status);
        orderRepo.save(order);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{orderNumber}/shipped-date/{date}")
    public ResponseEntity<Void> updateShippedDate(
            @PathVariable Integer orderNumber,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Order order = orderRepo.findById(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));
        order.setShippedDate(date);
        orderRepo.save(order);
        return ResponseEntity.noContent().build();
    }

    @lombok.Data
    public static class CreateOrderRequest {
        private Order             order;
        private List<OrderDetail> orderDetails;
    }
}