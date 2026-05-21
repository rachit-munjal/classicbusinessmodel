package com.project.cmb.service;

import com.project.cmb.projection.ProductListView;
import com.project.cmb.repo.ProductLineRepo;
import com.project.cmb.repo.ProductRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProductService {

    private final ProductRepo     productRepo;
    private final ProductLineRepo productLineRepo;

    @Transactional(readOnly = true)
    public long getTotalProducts() {
        return productRepo.count();
    }

    @Transactional(readOnly = true)
    public long getTotalProductLines() {
        return productLineRepo.count();
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getProductCountPerLine() {
        return productRepo.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        p -> p.getProductLine().getProductLine(),
                        Collectors.counting()
                ));
    }

    @Transactional(readOnly = true)
    public List<ProductListView> getLowStockProducts() {
        return productRepo.findByQuantityInStockLessThan((short) 50);
    }
}