package com.example.salesforcepoc.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.salesforcepoc.common.ProductResults;
import com.example.salesforcepoc.common.QueryResults;
import com.example.salesforcepoc.entity.Product;
import com.example.salesforcepoc.service.LuceneSearchService;
import com.example.salesforcepoc.service.ProductService;

@RestController
@RequestMapping("/api")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private LuceneSearchService luceneSearchService;
    
    @GetMapping("/productBySupplier/{supplierIds}")
    public ProductResults getProductsBySupplierWithFilters(
            @PathVariable String supplierIds,
            @RequestParam(required = false) String brandSearch,
            @RequestParam(required = false) String itemDescriptionSearch,
            @RequestParam(defaultValue = "500") int limit) {
        
        try {
            long startTime = System.currentTimeMillis();
            
            QueryResults queryResults = luceneSearchService.searchProductsBySupplierWithFilters(
                supplierIds, brandSearch, itemDescriptionSearch, limit);
            
            List<Product> products = new ArrayList<>();
            for (String productId : queryResults.getProductIds()) {
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

            ProductResults productResults = new ProductResults(products, queryResults.getMatchingResultsCount());
            
            return productResults;
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ProductResults(
                new ArrayList<>(),
                0
            );
        }
    }
}
