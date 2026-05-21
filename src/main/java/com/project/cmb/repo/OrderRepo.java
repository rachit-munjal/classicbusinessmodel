//package com.project.cmb.repo;
//
//import com.project.cmb.entity.Order;
//import com.project.cmb.projection.OrderListView;
//import com.project.cmb.projection.RecentOrderView;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.rest.core.annotation.RepositoryRestResource;
//
//import java.time.LocalDate;
//import java.util.List;
//
//@RepositoryRestResource(path = "orders")
//public interface OrderRepo extends JpaRepository<Order, Integer> {
//
//    Order findTopByOrderByOrderNumberDesc();
//
//    List<OrderListView> findByStatus(String status);
//
//    List<OrderListView> findByCustomer_CustomerNumber(
//            Integer customerNumber
//    );
//
//    Page<OrderListView> findByCustomer_CustomerNameContainingIgnoreCase(
//            String customerName,
//            Pageable pageable
//    );
//
//    List<OrderListView> findByOrderDateBetween(
//            LocalDate startDate,
//            LocalDate endDate
//    );
//
//    long countByStatus(String status);
//
//    List<RecentOrderView> findTop5ByOrderByOrderDateDesc();
//}

package com.project.cmb.repo;

import com.project.cmb.entity.Order;
import com.project.cmb.projection.OrderDetailView;
import com.project.cmb.projection.OrderListView;
import com.project.cmb.projection.RecentOrderView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RepositoryRestResource(path = "orders")
public interface OrderRepo extends JpaRepository<Order, Integer> {

    Order findTopByOrderByOrderNumberDesc();

    Optional<OrderDetailView> findByOrderNumber(Integer orderNumber);

    List<OrderListView> findByStatus(String status);

    List<OrderListView> findByCustomer_CustomerNumber(Integer customerNumber);

    Page<OrderListView> findByCustomer_CustomerNameContainingIgnoreCase(
            String customerName, Pageable pageable);

    List<OrderListView> findByOrderDateBetween(LocalDate startDate, LocalDate endDate);

    long countByStatus(String status);

    List<RecentOrderView> findTop5ByOrderByOrderDateDesc();
}