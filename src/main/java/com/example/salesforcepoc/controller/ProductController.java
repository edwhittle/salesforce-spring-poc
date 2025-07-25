package com.example.salesforcepoc.controller;

import com.example.salesforcepoc.entity.Product;
import com.example.salesforcepoc.service.ProductService;
import com.example.salesforcepoc.service.LuceneSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private LuceneSearchService luceneSearchService;
    
    @GetMapping("/products/supplier/{supplier}")
    public List<Product> getProductsBySupplier(@PathVariable String supplier) {
        return productService.getProductsBySupplier(supplier);
    }
    
    @GetMapping("/products/suppliers/{suppliers}")
    public List<Product> getProductsBySuppliers(@PathVariable String suppliers) {
        return productService.getProductsBySuppliers(suppliers);
    }
    
    @GetMapping("/products/suppliers")
    public List<Product> getProductsBySupplierParams(@RequestParam String suppliers) {
        return productService.getProductsBySuppliers(suppliers);
    }
    
    @GetMapping("/productBySupplier/{supplierIds}")
    public List<Product> getProductsBySupplierWithFilters(
            @PathVariable String supplierIds,
            @RequestParam(required = false) String brandSearch,
            @RequestParam(required = false) String itemDescriptionSearch,
            @RequestParam(defaultValue = "100") int limit) {
        
        try {
            long startTime = System.currentTimeMillis();
            
            List<String> productIds = luceneSearchService.searchProductsBySupplierWithFilters(
                supplierIds, brandSearch, itemDescriptionSearch, limit);
            
            List<Product> products = new ArrayList<>();
            for (String productId : productIds) {
                Product product = productService.getProductByProductId(productId);
                if (product != null) {
                    products.add(product);
                }
            }
            
            long endTime = System.currentTimeMillis();
            
            System.out.println("Supplier search with filters completed in " + (endTime - startTime) + 
                "ms. Suppliers: " + supplierIds + 
                (brandSearch != null ? ", Brand: " + brandSearch : "") +
                (itemDescriptionSearch != null ? ", Description: " + itemDescriptionSearch : "") +
                ". Found " + products.size() + " results.");
            
            return products;
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
