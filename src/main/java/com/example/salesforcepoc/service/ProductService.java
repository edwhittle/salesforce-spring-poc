package com.example.salesforcepoc.service;

import com.example.salesforcepoc.entity.Product;
import com.example.salesforcepoc.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;

    // Required for Lucene indexing
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    // Required for SearchController 
    public Product getProductByProductId(String productId) {
        return productRepository.findByProductId(productId);
    }
    
    // Required for SearchController database search comparison
    public List<Product> searchProducts(String searchTerm) {
        return productRepository.searchProducts(searchTerm);
    }
    
    // Main business logic method - search by single supplier
    public List<Product> getProductsBySupplier(String supplier) {
        return productRepository.findBySupplier(supplier);
    }
    
    // Main business logic method - search by multiple suppliers (comma-separated)
    public List<Product> getProductsBySuppliers(String suppliers) {
        if (suppliers == null || suppliers.trim().isEmpty()) {
            return List.of();
        }
        
        // Split by comma and trim whitespace
        List<String> supplierList = Arrays.stream(suppliers.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        
        if (supplierList.isEmpty()) {
            return List.of();
        }
        
        // If only one supplier, use the single supplier method for consistency
        if (supplierList.size() == 1) {
            return getProductsBySupplier(supplierList.get(0));
        }
        
        return productRepository.findBySupplierIn(supplierList);
    }
}
