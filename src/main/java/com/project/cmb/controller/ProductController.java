package com.project.cmb.controller;

import com.project.cmb.entity.Product;
import com.project.cmb.entity.ProductLine;
import com.project.cmb.exception.ResourceNotFoundException;
import com.project.cmb.projection.ProductListView;
import com.project.cmb.repo.ProductLineRepo;
import com.project.cmb.repo.ProductRepo;
import com.project.cmb.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/v1/products")
@AllArgsConstructor
public class ProductController {

    private final ProductRepo productRepo;
    private final ProductService productService;
    private final ProductLineRepo productLineRepo;

    // ─── GET /{productCode} — single product detail ───────────────
    @GetMapping("/{productCode}")
    public ResponseEntity<?> getProduct(@PathVariable String productCode) {
        Optional<Product> opt = productRepo.findById(productCode);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Product p = opt.get();
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("productCode",        p.getProductCode());
        result.put("productName",        p.getProductName());
        result.put("productScale",       p.getProductScale());
        result.put("productVendor",      p.getProductVendor());
        result.put("productDescription", p.getProductDescription());
        result.put("quantityInStock",    p.getQuantityInStock());
        result.put("buyPrice",           p.getBuyPrice());
        result.put("msrp",               p.getMsrp());
        if (p.getProductLine() != null) {
            result.put("productLineName", p.getProductLine().getProductLine());
        }
        return ResponseEntity.ok(result);
    }

    // ─── POST /add — create product ───────────────────────────────
    @PostMapping("/add")
    public ResponseEntity<?> addProduct(@RequestBody Map<String, Object> dto) {
        Product p = new Product();
        p.setProductCode((String)   dto.get("productCode"));
        p.setProductName((String)   dto.get("productName"));
        p.setProductScale((String)  dto.getOrDefault("productScale", "1:10"));
        p.setProductVendor((String) dto.getOrDefault("productVendor", ""));
        p.setProductDescription((String) dto.getOrDefault("productDescription", ""));
        if (dto.get("quantityInStock") != null)
            p.setQuantityInStock(Short.valueOf(dto.get("quantityInStock").toString()));
        if (dto.get("buyPrice") != null)
            p.setBuyPrice(new BigDecimal(dto.get("buyPrice").toString()));
        if (dto.get("msrp") != null)
            p.setMsrp(new BigDecimal(dto.get("msrp").toString()));
        // Product line — required, return 400 if not found
        String productLineName = (String) dto.get("productLineName");
        if (productLineName == null || productLineName.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "productLineName is required"));
        }
        ProductLine productLine = productLineRepo.findById(productLineName)
                .orElse(null);
        if (productLine == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Product line not found: " + productLineName));
        }
        p.setProductLine(productLine);

        // Validate required fields before save
        if (p.getProductCode() == null || p.getProductCode().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "productCode is required"));
        }
        if (productRepo.existsById(p.getProductCode())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Product code already exists: " + p.getProductCode()));
        }

        Product saved = productRepo.save(p);
        return ResponseEntity.status(201).body(
                Map.of("productCode", saved.getProductCode(), "message", "Product created"));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalProducts",      productService.getTotalProducts());
        stats.put("totalProductLines",  productService.getTotalProductLines());
        stats.put("productCountPerLine", productService.getProductCountPerLine());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductListView>> getLowStock() {
        return ResponseEntity.ok(productService.getLowStockProducts());
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductListView>> searchByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                productRepo.findByProductNameContainingIgnoreCase(name, pageable));
    }

    @GetMapping("/search/vendor")
    public ResponseEntity<Page<ProductListView>> searchByVendor(
            @RequestParam String vendor,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                productRepo.findByProductVendorContainingIgnoreCase(vendor, pageable));
    }

    @GetMapping("/search/code")
    public ResponseEntity<Page<ProductListView>> searchByCode(
            @RequestParam String code,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                productRepo.findByProductCodeContainingIgnoreCase(code, pageable));
    }

    @GetMapping("/filter/line")
    public ResponseEntity<Page<ProductListView>> filterByProductLine(
            @RequestParam String productLine,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                productRepo.findByProductLine_ProductLine(productLine, pageable));
    }

    @PutMapping("/{productCode}/price/{buyPrice}")
    public ResponseEntity<Product> updateBuyPrice(
            @PathVariable String productCode,
            @PathVariable BigDecimal buyPrice) {
        Product product = productRepo.findById(productCode)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productCode", productCode));
        product.setBuyPrice(buyPrice);
        return ResponseEntity.ok(productRepo.save(product));
    }

    @PutMapping("/{productCode}/msrp/{msrp}")
    public ResponseEntity<Product> updateMsrp(
            @PathVariable String productCode,
            @PathVariable BigDecimal msrp) {
        Product product = productRepo.findById(productCode)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productCode", productCode));
        product.setMsrp(msrp);
        return ResponseEntity.ok(productRepo.save(product));
    }

    @PutMapping("/{productCode}/quantity/{quantity}")
    public ResponseEntity<Product> updateQuantity(
            @PathVariable String productCode,
            @PathVariable Short quantity) {
        Product product = productRepo.findById(productCode)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productCode", productCode));
        product.setQuantityInStock(quantity);
        return ResponseEntity.ok(productRepo.save(product));
    }
}