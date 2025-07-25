package com.example.salesforcepoc.controller;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.salesforcepoc.common.BrandCategoryResults;
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
    public BrandCategoryResults getProductsBySupplierWithFilters(
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
            
            // Extract unique brands (both smkts and liq brands)
            Set<String> uniqueBrands = new LinkedHashSet<>();
            Set<String> uniqueCategories = new LinkedHashSet<>();
            
            for (Product product : products) {
                // Add digital brand name
                if (product.getDigitalBrandName() != null && !product.getDigitalBrandName().trim().isEmpty()) {
                    uniqueBrands.add(product.getDigitalBrandName().trim());
                }
                
                // Add sub brand name
                if (product.getSubBrandName() != null && !product.getSubBrandName().trim().isEmpty()) {
                    uniqueBrands.add(product.getSubBrandName().trim());
                }
                
                // Add smkts merch category
                if (product.getSmktsMerchCategory() != null && !product.getSmktsMerchCategory().trim().isEmpty()) {
                    uniqueCategories.add(product.getSmktsMerchCategory().trim());
                }
                
                // Add liq merch category
                if (product.getLiqMerchCategory() != null && !product.getLiqMerchCategory().trim().isEmpty()) {
                    uniqueCategories.add(product.getLiqMerchCategory().trim());
                }
            }
            
            long endTime = System.currentTimeMillis();
            
            System.out.println("Supplier search with filters completed in " + (endTime - startTime) + 
                "ms. Suppliers: " + supplierIds + 
                (brandSearch != null ? ", Brand: " + brandSearch : "") +
                (itemDescriptionSearch != null ? ", Description: " + itemDescriptionSearch : "") +
                ". Found " + products.size() + " results.");

            BrandCategoryResults results = new BrandCategoryResults(
                products, 
                queryResults.getMatchingResultsCount(),
                new ArrayList<>(uniqueBrands),
                new ArrayList<>(uniqueCategories)
            );
            
            return results;
            
        } catch (Exception e) {
            System.err.println("Error in getProductsBySupplierWithFilters: " + e.getMessage());
            return new BrandCategoryResults(
                new ArrayList<>(),
                0,
                new ArrayList<>(),
                new ArrayList<>()
            );
        }
    }
}
