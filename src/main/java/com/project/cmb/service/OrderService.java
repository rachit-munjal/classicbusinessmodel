package com.project.cmb.service;

import com.project.cmb.constant.OrderStatus;
import com.project.cmb.entity.Order;
import com.project.cmb.entity.OrderDetail;
import com.project.cmb.entity.Product;
import com.project.cmb.exception.InsufficientStockException;
import com.project.cmb.exception.ResourceNotFoundException;
import com.project.cmb.projection.PaymentListView;
import com.project.cmb.repo.OrderDetailRepo;
import com.project.cmb.repo.OrderRepo;
import com.project.cmb.repo.PaymentRepo;
import com.project.cmb.repo.ProductRepo;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class OrderService {

    private final OrderRepo       orderRepo;
    private final OrderDetailRepo orderDetailRepo;
    private final ProductRepo     productRepo;
    private final PaymentRepo     paymentRepo;

    public long getTotalOrders()    { return orderRepo.count(); }
    public long getTotalShipped()   { return orderRepo.countByStatus(OrderStatus.SHIPPED); }
    public long getTotalInProcess() { return orderRepo.countByStatus(OrderStatus.IN_PROCESS); }
    public long getTotalCancelled() { return orderRepo.countByStatus(OrderStatus.CANCELLED); }

    public BigDecimal getOrderTotal(Integer orderNumber) {
        return orderDetailRepo.findById_OrderNumber(orderNumber)
                .stream()
                .map(od -> od.getPriceEach()
                        .multiply(new BigDecimal(od.getQuantityOrdered())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalPaymentReceived(Integer orderNumber) {
        return paymentRepo.findByOrderNumber(orderNumber)
                .stream()
                .map(PaymentListView::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getPendingPayment(Integer orderNumber) {
        return getOrderTotal(orderNumber)
                .subtract(getTotalPaymentReceived(orderNumber));
    }

    /**
     * Creates an order with its line items.
     * Each product is fetched once: stock validated and stock level updated in a single pass.
     */
    @Transactional
    public Order createOrder(Order order, List<OrderDetail> orderDetails) {

        // Single pass: fetch each product once, validate stock, keep reference for update
        Map<String, Product> productMap = new HashMap<>();
        for (OrderDetail detail : orderDetails) {
            String code = detail.getId().getProductCode();
            Product product = productRepo.findById(code)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "productCode", code));

            if (product.getQuantityInStock() < detail.getQuantityOrdered()) {
                throw new InsufficientStockException(
                        product.getProductName(),
                        product.getQuantityInStock(),
                        detail.getQuantityOrdered());
            }
            productMap.put(code, product);
        }

        Order savedOrder = orderRepo.save(order);

        // Use already-fetched products — no second DB lookup per line item
        for (OrderDetail detail : orderDetails) {
            detail.getId().setOrderNumber(savedOrder.getOrderNumber());
            orderDetailRepo.save(detail);

            Product product = productMap.get(detail.getId().getProductCode());
            product.setQuantityInStock(
                    (short) (product.getQuantityInStock() - detail.getQuantityOrdered()));
            productRepo.save(product);
        }

        return savedOrder;
    }
}