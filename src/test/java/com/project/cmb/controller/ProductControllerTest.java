//package com.project.cmb.controller;
//
//import com.project.cmb.entity.Product;
//import com.project.cmb.entity.ProductLine;
//import com.project.cmb.projection.ProductListView;
//import com.project.cmb.repo.ProductLineRepo;
//import com.project.cmb.repo.ProductRepo;
//import com.project.cmb.service.ProductService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.math.BigDecimal;
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(ProductController.class)
//class ProductControllerTest {
//
//    @Autowired MockMvc mockMvc;
//    @MockitoBean
//    ProductRepo productRepo;
//    @MockitoBean
//    ProductService productService;
//
//    @MockitoBean
//    ProductLineRepo productLineRepo;
//
//
//    private ProductListView buildProductListView(String code, String name,
//                                                 Short qty, BigDecimal buyPrice, BigDecimal msrp, String line) {
//        return new ProductListView() {
//            public String getProductCode() { return code; }
//            public String getProductName() { return name; }
//            public Short getQuantityInStock() { return qty; }
//            public BigDecimal getBuyPrice() { return buyPrice; }
//            public BigDecimal getMsrp() { return msrp; }
//            public ProductLineInfo getProductLine() {
//                return new ProductLineInfo() {
//                    public String getProductLine() { return line; }
//                };
//            }
//        };
//    }
//
//    private Product buildProduct(String code, String name, BigDecimal buyPrice,
//                                 BigDecimal msrp, Short qty) {
//        ProductLine pl = new ProductLine();
//        pl.setProductLine("Classic Cars");
//        Product p = new Product();
//        p.setProductCode(code); p.setProductName(name);
//        p.setBuyPrice(buyPrice); p.setMsrp(msrp);
//        p.setQuantityInStock(qty); p.setProductLine(pl);
//        p.setProductScale("1:18"); p.setProductVendor("Test Vendor");
//        p.setProductDescription("Test description");
//        return p;
//    }
//
//    @Test
//    void getStats_shouldReturn200AndFields() throws Exception {
//        when(productService.getTotalProducts()).thenReturn(110L);
//        when(productService.getTotalProductLines()).thenReturn(7L);
//        when(productService.getProductCountPerLine())
//                .thenReturn(Map.of("Classic Cars", 38L, "Motorcycles", 13L));
//
//        mockMvc.perform(get("/api/v1/products/stats"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.totalProducts").value(110))
//                .andExpect(jsonPath("$.totalProductLines").value(7))
//                .andExpect(jsonPath("$.productCountPerLine").exists());
//    }
//
//    @Test
//    void getLowStock_shouldReturn200AndList() throws Exception {
//        List<ProductListView> lowStock = List.of(
//                buildProductListView("S10_1949", "1952 Alpine Renault",
//                        (short) 23, new BigDecimal("98.58"), new BigDecimal("214.30"), "Classic Cars")
//        );
//
//        when(productService.getLowStockProducts()).thenReturn(lowStock);
//
//        mockMvc.perform(get("/api/v1/products/low-stock"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$").isArray())
//                .andExpect(jsonPath("$.length()").value(1))
//                .andExpect(jsonPath("$[0].productCode").value("S10_1949"))
//                .andExpect(jsonPath("$[0].quantityInStock").value(23));
//    }
//
//    @Test
//    void getLowStock_noneBelow_shouldReturnEmptyList() throws Exception {
//        List<ProductListView> empty = Collections.emptyList();
//        when(productService.getLowStockProducts()).thenReturn(empty);
//
//        mockMvc.perform(get("/api/v1/products/low-stock"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$").isArray())
//                .andExpect(jsonPath("$.length()").value(0));
//    }
//
//    @Test
//    void searchByName_shouldReturn200AndPage() throws Exception {
//        List<ProductListView> content = List.of(
//                buildProductListView("S18_1749", "1917 Grand Touring Sedan",
//                        (short) 2724, new BigDecimal("86.70"), new BigDecimal("170.00"), "Classic Cars")
//        );
//
//        when(productRepo.findByProductNameContainingIgnoreCase(eq("grand"), any()))
//                .thenReturn(new PageImpl<>(content, PageRequest.of(0, 10), 1));
//
//        mockMvc.perform(get("/api/v1/products/search").param("name", "grand"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.content[0].productCode").value("S18_1749"))
//                .andExpect(jsonPath("$.content[0].productLine.productLine").value("Classic Cars"))
//                .andExpect(jsonPath("$.totalElements").value(1));
//    }
//
//    @Test
//    void searchByName_noMatch_shouldReturnEmptyPage() throws Exception {
//        List<ProductListView> empty = Collections.emptyList();
//        when(productRepo.findByProductNameContainingIgnoreCase(eq("xyzxyz"), any()))
//                .thenReturn(new PageImpl<>(empty));
//
//        mockMvc.perform(get("/api/v1/products/search").param("name", "xyzxyz"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.totalElements").value(0));
//    }
//
//    @Test
//    void filterByProductLine_shouldReturn200AndPage() throws Exception {
//        List<ProductListView> content = List.of(
//                buildProductListView("S18_1749", "1917 Grand Touring Sedan",
//                        (short) 2724, new BigDecimal("86.70"), new BigDecimal("170.00"), "Classic Cars")
//        );
//
//        when(productRepo.findByProductLine_ProductLine(eq("Classic Cars"), any()))
//                .thenReturn(new PageImpl<>(content, PageRequest.of(0, 10), 1));
//
//        mockMvc.perform(get("/api/v1/products/filter/line")
//                        .param("productLine", "Classic Cars"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.content[0].productLine.productLine").value("Classic Cars"))
//                .andExpect(jsonPath("$.totalElements").value(1));
//    }
//
//    @Test
//    void filterByProductLine_noMatch_shouldReturnEmptyPage() throws Exception {
//        List<ProductListView> empty = Collections.emptyList();
//        when(productRepo.findByProductLine_ProductLine(eq("Nonexistent"), any()))
//                .thenReturn(new PageImpl<>(empty));
//
//        mockMvc.perform(get("/api/v1/products/filter/line")
//                        .param("productLine", "Nonexistent"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.totalElements").value(0));
//    }
//
//    @Test
//    void updateBuyPrice_shouldReturn200AndUpdatedProduct() throws Exception {
//        Product product = buildProduct("S18_1749", "1917 Grand Touring Sedan",
//                new BigDecimal("86.70"), new BigDecimal("170.00"), (short) 2724);
//        Product updated = buildProduct("S18_1749", "1917 Grand Touring Sedan",
//                new BigDecimal("95.00"), new BigDecimal("170.00"), (short) 2724);
//
//        when(productRepo.findById("S18_1749")).thenReturn(Optional.of(product));
//        when(productRepo.save(any())).thenReturn(updated);
//
//        mockMvc.perform(put("/api/v1/products/S18_1749/price/95.00"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.productCode").value("S18_1749"))
//                .andExpect(jsonPath("$.buyPrice").value(95.00));
//    }
//
//    @Test
//    void updateMsrp_shouldReturn200AndUpdatedProduct() throws Exception {
//        Product product = buildProduct("S18_1749", "1917 Grand Touring Sedan",
//                new BigDecimal("86.70"), new BigDecimal("170.00"), (short) 2724);
//        Product updated = buildProduct("S18_1749", "1917 Grand Touring Sedan",
//                new BigDecimal("86.70"), new BigDecimal("185.00"), (short) 2724);
//
//        when(productRepo.findById("S18_1749")).thenReturn(Optional.of(product));
//        when(productRepo.save(any())).thenReturn(updated);
//
//        mockMvc.perform(put("/api/v1/products/S18_1749/msrp/185.00"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.msrp").value(185.00));
//    }
//
//    @Test
//    void updateQuantity_shouldReturn200AndUpdatedProduct() throws Exception {
//        Product product = buildProduct("S18_1749", "1917 Grand Touring Sedan",
//                new BigDecimal("86.70"), new BigDecimal("170.00"), (short) 2724);
//        Product updated = buildProduct("S18_1749", "1917 Grand Touring Sedan",
//                new BigDecimal("86.70"), new BigDecimal("170.00"), (short) 150);
//
//        when(productRepo.findById("S18_1749")).thenReturn(Optional.of(product));
//        when(productRepo.save(any())).thenReturn(updated);
//
//        mockMvc.perform(put("/api/v1/products/S18_1749/quantity/150"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.quantityInStock").value(150));
//    }
//
//    @Test
//    void updateBuyPrice_productNotFound_shouldReturn404() throws Exception {
//        when(productRepo.findById("ZZZZZZ")).thenReturn(Optional.empty());
//
//        mockMvc.perform(put("/api/v1/products/ZZZZZZ/price/95.00"))
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.status").value(404))
//                .andExpect(jsonPath("$.error").value("Not Found"))
//                .andExpect(jsonPath("$.message").exists())
//                .andExpect(jsonPath("$.timestamp").exists());
//    }
//
//    @Test
//    void updateMsrp_productNotFound_shouldReturn404() throws Exception {
//        when(productRepo.findById("ZZZZZZ")).thenReturn(Optional.empty());
//
//        mockMvc.perform(put("/api/v1/products/ZZZZZZ/msrp/185.00"))
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.status").value(404))
//                .andExpect(jsonPath("$.message").exists());
//    }
//
//    @Test
//    void updateQuantity_productNotFound_shouldReturn404() throws Exception {
//        when(productRepo.findById("ZZZZZZ")).thenReturn(Optional.empty());
//
//        mockMvc.perform(put("/api/v1/products/ZZZZZZ/quantity/150"))
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.status").value(404))
//                .andExpect(jsonPath("$.message").exists());
//    }
//
//}

package com.project.cmb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.cmb.entity.Product;
import com.project.cmb.entity.ProductLine;
import com.project.cmb.projection.ProductListView;
import com.project.cmb.repo.ProductLineRepo;
import com.project.cmb.repo.ProductRepo;
import com.project.cmb.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired MockMvc       mockMvc;
    @Autowired ObjectMapper  objectMapper;

    @MockitoBean ProductRepo     productRepo;
    @MockitoBean ProductService  productService;
    @MockitoBean ProductLineRepo productLineRepo;

    // ─── Helpers ──────────────────────────────────────────────────

    private ProductListView buildProductListView(String code, String name,
                                                 Short qty, BigDecimal buyPrice,
                                                 BigDecimal msrp, String line) {
        return new ProductListView() {
            public String     getProductCode()      { return code;     }
            public String     getProductName()      { return name;     }
            public Short      getQuantityInStock()  { return qty;      }
            public BigDecimal getBuyPrice()         { return buyPrice; }
            public BigDecimal getMsrp()             { return msrp;     }
            public ProductLineInfo getProductLine() {
                return new ProductLineInfo() {
                    public String getProductLine() { return line; }
                };
            }
        };
    }

    private Product buildProductEntity(String code, String name, BigDecimal buyPrice,
                                       BigDecimal msrp, Short qty) {
        ProductLine pl = new ProductLine();
        pl.setProductLine("Classic Cars");
        Product p = new Product();
        p.setProductCode(code);
        p.setProductName(name);
        p.setBuyPrice(buyPrice);
        p.setMsrp(msrp);
        p.setQuantityInStock(qty);
        p.setProductLine(pl);
        p.setProductScale("1:18");
        p.setProductVendor("Test Vendor");
        p.setProductDescription("Test description");
        return p;
    }

    // ─── GET /{productCode} ───────────────────────────────────────

    @Test
    void getProduct_found_shouldReturn200AndFields() throws Exception {
        Product p = buildProductEntity("S18_1749", "1917 Grand Touring Sedan",
                new BigDecimal("86.70"), new BigDecimal("170.00"), (short) 2724);
        when(productRepo.findById("S18_1749")).thenReturn(Optional.of(p));

        mockMvc.perform(get("/api/v1/products/S18_1749"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.productCode").value("S18_1749"))
                .andExpect(jsonPath("$.productName").value("1917 Grand Touring Sedan"))
                .andExpect(jsonPath("$.buyPrice").value(86.70))
                .andExpect(jsonPath("$.msrp").value(170.00))
                .andExpect(jsonPath("$.quantityInStock").value(2724))
                .andExpect(jsonPath("$.productLineName").value("Classic Cars"));
    }

    @Test
    void getProduct_notFound_shouldReturn404() throws Exception {
        when(productRepo.findById("ZZZZZZ")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/products/ZZZZZZ"))
                .andExpect(status().isNotFound());
    }

    // ─── POST /add ────────────────────────────────────────────────

    @Test
    void addProduct_shouldReturn201AndProductCode() throws Exception {
        ProductLine pl = new ProductLine();
        pl.setProductLine("Classic Cars");
        when(productLineRepo.findById("Classic Cars")).thenReturn(Optional.of(pl));
        when(productRepo.existsById("S18_9999")).thenReturn(false);

        Product saved = buildProductEntity("S18_9999", "New Model",
                new BigDecimal("55.00"), new BigDecimal("110.00"), (short) 500);
        when(productRepo.save(any(Product.class))).thenReturn(saved);

        Map<String, Object> body = Map.of(
                "productCode",        "S18_9999",
                "productName",        "New Model",
                "productLineName",    "Classic Cars",
                "productScale",       "1:18",
                "productVendor",      "Test Vendor",
                "productDescription", "A new model",
                "quantityInStock",    500,
                "buyPrice",           "55.00",
                "msrp",               "110.00"
        );

        mockMvc.perform(post("/api/v1/products/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productCode").value("S18_9999"))
                .andExpect(jsonPath("$.message").value("Product created"));
    }

    @Test
    void addProduct_missingProductLineName_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of(
                "productCode", "S18_9999",
                "productName", "New Model"
        );

        mockMvc.perform(post("/api/v1/products/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("productLineName is required"));
    }

    @Test
    void addProduct_productLineNotFound_shouldReturn400() throws Exception {
        when(productLineRepo.findById("Unknown Line")).thenReturn(Optional.empty());

        Map<String, Object> body = Map.of(
                "productCode",     "S18_9999",
                "productName",     "New Model",
                "productLineName", "Unknown Line"
        );

        mockMvc.perform(post("/api/v1/products/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Product line not found: Unknown Line"));
    }

    @Test
    void addProduct_duplicateCode_shouldReturn400() throws Exception {
        ProductLine pl = new ProductLine();
        pl.setProductLine("Classic Cars");
        when(productLineRepo.findById("Classic Cars")).thenReturn(Optional.of(pl));
        when(productRepo.existsById("S18_1749")).thenReturn(true);

        Map<String, Object> body = Map.of(
                "productCode",     "S18_1749",
                "productName",     "Duplicate",
                "productLineName", "Classic Cars"
        );

        mockMvc.perform(post("/api/v1/products/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Product code already exists: S18_1749"));
    }

    // ─── GET /stats ───────────────────────────────────────────────

    @Test
    void getStats_shouldReturn200AndFields() throws Exception {
        when(productService.getTotalProducts()).thenReturn(110L);
        when(productService.getTotalProductLines()).thenReturn(7L);
        when(productService.getProductCountPerLine())
                .thenReturn(Map.of("Classic Cars", 38L, "Motorcycles", 13L));

        mockMvc.perform(get("/api/v1/products/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalProducts").value(110))
                .andExpect(jsonPath("$.totalProductLines").value(7))
                .andExpect(jsonPath("$.productCountPerLine").exists());
    }

    // ─── GET /low-stock ───────────────────────────────────────────

    @Test
    void getLowStock_shouldReturn200AndList() throws Exception {
        List<ProductListView> lowStock = List.of(
                buildProductListView("S10_1949", "1952 Alpine Renault",
                        (short) 23, new BigDecimal("98.58"), new BigDecimal("214.30"), "Classic Cars")
        );
        when(productService.getLowStockProducts()).thenReturn(lowStock);

        mockMvc.perform(get("/api/v1/products/low-stock"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].productCode").value("S10_1949"))
                .andExpect(jsonPath("$[0].quantityInStock").value(23));
    }

    @Test
    void getLowStock_noneBelow_shouldReturnEmptyList() throws Exception {
        when(productService.getLowStockProducts()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/products/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ─── GET /search ──────────────────────────────────────────────

    @Test
    void searchByName_shouldReturn200AndPage() throws Exception {
        List<ProductListView> content = List.of(
                buildProductListView("S18_1749", "1917 Grand Touring Sedan",
                        (short) 2724, new BigDecimal("86.70"), new BigDecimal("170.00"), "Classic Cars")
        );
        when(productRepo.findByProductNameContainingIgnoreCase(eq("grand"), any()))
                .thenReturn(new PageImpl<>(content, PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/products/search").param("name", "grand"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].productCode").value("S18_1749"))
                .andExpect(jsonPath("$.content[0].productLine.productLine").value("Classic Cars"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void searchByName_noMatch_shouldReturnEmptyPage() throws Exception {
        when(productRepo.findByProductNameContainingIgnoreCase(eq("xyzxyz"), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/v1/products/search").param("name", "xyzxyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ─── GET /search/vendor ───────────────────────────────────────

    @Test
    void searchByVendor_shouldReturn200AndPage() throws Exception {
        List<ProductListView> content = List.of(
                buildProductListView("S18_1749", "1917 Grand Touring Sedan",
                        (short) 2724, new BigDecimal("86.70"), new BigDecimal("170.00"), "Classic Cars")
        );
        when(productRepo.findByProductVendorContainingIgnoreCase(eq("test"), any()))
                .thenReturn(new PageImpl<>(content, PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/products/search/vendor").param("vendor", "test"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].productCode").value("S18_1749"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void searchByVendor_noMatch_shouldReturnEmptyPage() throws Exception {
        when(productRepo.findByProductVendorContainingIgnoreCase(eq("xyzxyz"), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/v1/products/search/vendor").param("vendor", "xyzxyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ─── GET /search/code ─────────────────────────────────────────

    @Test
    void searchByCode_shouldReturn200AndPage() throws Exception {
        List<ProductListView> content = List.of(
                buildProductListView("S18_1749", "1917 Grand Touring Sedan",
                        (short) 2724, new BigDecimal("86.70"), new BigDecimal("170.00"), "Classic Cars")
        );
        when(productRepo.findByProductCodeContainingIgnoreCase(eq("S18"), any()))
                .thenReturn(new PageImpl<>(content, PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/products/search/code").param("code", "S18"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].productCode").value("S18_1749"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void searchByCode_noMatch_shouldReturnEmptyPage() throws Exception {
        when(productRepo.findByProductCodeContainingIgnoreCase(eq("ZZZZZZ"), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/v1/products/search/code").param("code", "ZZZZZZ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ─── GET /filter/line ─────────────────────────────────────────

    @Test
    void filterByProductLine_shouldReturn200AndPage() throws Exception {
        List<ProductListView> content = List.of(
                buildProductListView("S18_1749", "1917 Grand Touring Sedan",
                        (short) 2724, new BigDecimal("86.70"), new BigDecimal("170.00"), "Classic Cars")
        );
        when(productRepo.findByProductLine_ProductLine(eq("Classic Cars"), any()))
                .thenReturn(new PageImpl<>(content, PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/products/filter/line").param("productLine", "Classic Cars"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].productLine.productLine").value("Classic Cars"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void filterByProductLine_noMatch_shouldReturnEmptyPage() throws Exception {
        when(productRepo.findByProductLine_ProductLine(eq("Nonexistent"), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/v1/products/filter/line").param("productLine", "Nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ─── PUT /{productCode}/price/{buyPrice} ──────────────────────

    @Test
    void updateBuyPrice_shouldReturn200AndUpdatedProduct() throws Exception {
        Product product = buildProductEntity("S18_1749", "1917 Grand Touring Sedan",
                new BigDecimal("86.70"), new BigDecimal("170.00"), (short) 2724);
        Product updated = buildProductEntity("S18_1749", "1917 Grand Touring Sedan",
                new BigDecimal("95.00"), new BigDecimal("170.00"), (short) 2724);

        when(productRepo.findById("S18_1749")).thenReturn(Optional.of(product));
        when(productRepo.save(any())).thenReturn(updated);

        mockMvc.perform(put("/api/v1/products/S18_1749/price/95.00"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.productCode").value("S18_1749"))
                .andExpect(jsonPath("$.buyPrice").value(95.00));
    }

    @Test
    void updateBuyPrice_productNotFound_shouldReturn404() throws Exception {
        when(productRepo.findById("ZZZZZZ")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/products/ZZZZZZ/price/95.00"))
                .andExpect(status().isNotFound());
    }

    // ─── PUT /{productCode}/msrp/{msrp} ──────────────────────────

    @Test
    void updateMsrp_shouldReturn200AndUpdatedProduct() throws Exception {
        Product product = buildProductEntity("S18_1749", "1917 Grand Touring Sedan",
                new BigDecimal("86.70"), new BigDecimal("170.00"), (short) 2724);
        Product updated = buildProductEntity("S18_1749", "1917 Grand Touring Sedan",
                new BigDecimal("86.70"), new BigDecimal("185.00"), (short) 2724);

        when(productRepo.findById("S18_1749")).thenReturn(Optional.of(product));
        when(productRepo.save(any())).thenReturn(updated);

        mockMvc.perform(put("/api/v1/products/S18_1749/msrp/185.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msrp").value(185.00));
    }

    @Test
    void updateMsrp_productNotFound_shouldReturn404() throws Exception {
        when(productRepo.findById("ZZZZZZ")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/products/ZZZZZZ/msrp/185.00"))
                .andExpect(status().isNotFound());
    }

    // ─── PUT /{productCode}/quantity/{quantity} ───────────────────

    @Test
    void updateQuantity_shouldReturn200AndUpdatedProduct() throws Exception {
        Product product = buildProductEntity("S18_1749", "1917 Grand Touring Sedan",
                new BigDecimal("86.70"), new BigDecimal("170.00"), (short) 2724);
        Product updated = buildProductEntity("S18_1749", "1917 Grand Touring Sedan",
                new BigDecimal("86.70"), new BigDecimal("170.00"), (short) 150);

        when(productRepo.findById("S18_1749")).thenReturn(Optional.of(product));
        when(productRepo.save(any())).thenReturn(updated);

        mockMvc.perform(put("/api/v1/products/S18_1749/quantity/150"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantityInStock").value(150));
    }

    @Test
    void updateQuantity_productNotFound_shouldReturn404() throws Exception {
        when(productRepo.findById("ZZZZZZ")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/products/ZZZZZZ/quantity/150"))
                .andExpect(status().isNotFound());
    }
}