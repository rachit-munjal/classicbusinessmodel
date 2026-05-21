package com.project.cmb.controller;

import com.project.cmb.dto.RecentOrderDto;
import com.project.cmb.service.DashBoardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashBoardController.class)
class DashBoardControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean DashBoardService dashBoardService;

    // ─── GET /api/v1/dashboard/stats ──────────────────────────────

    @Test
    void getStats_shouldReturn200() throws Exception {
        when(dashBoardService.getEmployeesCount()).thenReturn(23L);
        when(dashBoardService.getCustomersCount()).thenReturn(122L);
        when(dashBoardService.getOrdersCount()).thenReturn(326L);
        when(dashBoardService.getProductsCount()).thenReturn(110L);
        when(dashBoardService.getPaymentsCount()).thenReturn(273L);
        when(dashBoardService.getTotalSalesAmount()).thenReturn(new BigDecimal("8853839.23"));
        when(dashBoardService.getPendingOrdersCount()).thenReturn(6L);

        mockMvc.perform(get("/api/v1/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEmployees").value(23))
                .andExpect(jsonPath("$.totalCustomers").value(122))
                .andExpect(jsonPath("$.totalOrders").value(326))
                .andExpect(jsonPath("$.totalProducts").value(110))
                .andExpect(jsonPath("$.totalPayments").value(273))
                .andExpect(jsonPath("$.totalSalesAmount").exists())
                .andExpect(jsonPath("$.pendingOrdersCount").value(6));
    }

    @Test
    void getStats_shouldCallAllServiceMethods() throws Exception {
        when(dashBoardService.getEmployeesCount()).thenReturn(0L);
        when(dashBoardService.getCustomersCount()).thenReturn(0L);
        when(dashBoardService.getOrdersCount()).thenReturn(0L);
        when(dashBoardService.getProductsCount()).thenReturn(0L);
        when(dashBoardService.getPaymentsCount()).thenReturn(0L);
        when(dashBoardService.getTotalSalesAmount()).thenReturn(BigDecimal.ZERO);
        when(dashBoardService.getPendingOrdersCount()).thenReturn(0L);

        mockMvc.perform(get("/api/v1/dashboard/stats"))
                .andExpect(status().isOk());

        verify(dashBoardService).getEmployeesCount();
        verify(dashBoardService).getCustomersCount();
        verify(dashBoardService).getOrdersCount();
        verify(dashBoardService).getProductsCount();
        verify(dashBoardService).getPaymentsCount();
        verify(dashBoardService).getTotalSalesAmount();
        verify(dashBoardService).getPendingOrdersCount();
    }

    // ─── GET /api/v1/dashboard/recent-orders ─────────────────────

    @Test
    void getRecentOrders_shouldReturn200AndList() throws Exception {
        RecentOrderDto dto1 = new RecentOrderDto(10100, LocalDate.of(2003, 1, 6),  "Shipped",    null, new BigDecimal("5432.10"));
        RecentOrderDto dto2 = new RecentOrderDto(10101, LocalDate.of(2003, 1, 9),  "In Process", null, new BigDecimal("2100.00"));

        when(dashBoardService.getRecentOrders()).thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/api/v1/dashboard/recent-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].orderNumber").value(10100))
                .andExpect(jsonPath("$[0].status").value("Shipped"));
    }

    @Test
    void getRecentOrders_emptyList_shouldReturn200() throws Exception {
        when(dashBoardService.getRecentOrders()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/dashboard/recent-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ─── GET /api/v1/dashboard/orders-per-month ───────────────────

    @Test
    void getOrdersPerMonth_shouldReturn200AndMap() throws Exception {
        when(dashBoardService.getOrdersPerMonth()).thenReturn(
                Map.of("JANUARY", 12L, "FEBRUARY", 8L, "MARCH", 15L));

        mockMvc.perform(get("/api/v1/dashboard/orders-per-month"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.JANUARY").value(12))
                .andExpect(jsonPath("$.FEBRUARY").value(8))
                .andExpect(jsonPath("$.MARCH").value(15));
    }

    @Test
    void getOrdersPerMonth_emptyMap_shouldReturn200() throws Exception {
        when(dashBoardService.getOrdersPerMonth()).thenReturn(Map.of());

        mockMvc.perform(get("/api/v1/dashboard/orders-per-month"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap());
    }

    // ─── DELETE /api/v1/dashboard/cache ───────────────────────────

    @Test
    void evictDashboardCache_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/v1/dashboard/cache"))
                .andExpect(status().isNoContent());
    }
}