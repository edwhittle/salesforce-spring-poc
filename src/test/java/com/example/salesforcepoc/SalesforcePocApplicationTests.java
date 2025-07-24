package com.example.salesforcepoc;

import com.example.salesforcepoc.entity.Product;
import com.example.salesforcepoc.repository.ProductRepository;
import com.example.salesforcepoc.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SalesforcePocApplicationTests {

    @Autowired
    private ProductService productService;
    
    @Autowired
    private ProductRepository productRepository;

    @Test
    void contextLoads() {
    }
    
    @Test
    void testProductService() {
        // Test finding all products
        List<Product> products = productService.getAllProducts();
        assertNotNull(products);
        
        // Test finding products by supplier
        List<Product> supplierProducts = productService.getProductsBySupplier("Test Supplier");
        assertNotNull(supplierProducts);
        
        // Test finding products by multiple suppliers
        List<Product> multipleSupplierProducts = productService.getProductsBySuppliers("Test Supplier,Another Supplier");
        assertNotNull(multipleSupplierProducts);
        
        // Test general search functionality
        List<Product> searchResults = productService.searchProducts("test");
        assertNotNull(searchResults);
    }
    
    @Test
    void testProductRepository() {
        // Test finding products by supplier
        List<Product> supplierProducts = productRepository.findBySupplier("Test Supplier");
        assertNotNull(supplierProducts);
        
        // Test general search
        List<Product> searchResults = productRepository.searchProducts("test");
        assertNotNull(searchResults);
    }
}
