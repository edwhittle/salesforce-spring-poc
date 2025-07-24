package com.example.salesforcepoc.controller;

import com.example.salesforcepoc.entity.Product;
import com.example.salesforcepoc.service.LuceneSearchService;
import com.example.salesforcepoc.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    private LuceneSearchService luceneSearchService;
    
    @Autowired
    private ProductService productService;

    /**
     * Initialize/rebuild the Lucene index
     */
    @PostMapping("/index/rebuild")
    public ResponseEntity<Map<String, String>> rebuildIndex() {
        try {
            long startTime = System.currentTimeMillis();
            luceneSearchService.indexAllProducts();
            long endTime = System.currentTimeMillis();
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Index rebuilt successfully",
                "timeTaken", (endTime - startTime) + "ms"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Failed to rebuild index: " + e.getMessage()
            ));
        }
    }

    /**
     * Get index statistics
     */
    @GetMapping("/index/stats")
    public ResponseEntity<Map<String, String>> getIndexStats() {
        try {
            String stats = luceneSearchService.getIndexStats();
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "stats", stats
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Failed to get index stats: " + e.getMessage()
            ));
        }
    }

    /**
     * Search products using Lucene (optimized for supplier searches)
     */
    @GetMapping("/lucene")
    public ResponseEntity<List<Product>> searchWithLucene(
            @RequestParam String query,
            @RequestParam(defaultValue = "50") int limit) {
        try {
            long startTime = System.currentTimeMillis();
            
            // This now searches primarily in the supplier field
            List<String> productIds = luceneSearchService.searchProducts(query, limit);
            List<Product> products = new ArrayList<>();
            
            for (String productId : productIds) {
                Product product = productService.getProductByProductId(productId);
                if (product != null) {
                    products.add(product);
                }
            }
            
            long endTime = System.currentTimeMillis();
            
            System.out.println("Lucene supplier search completed in " + (endTime - startTime) + "ms. Found " + products.size() + " results.");
            
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new ArrayList<>());
        }
    }

    /**
     * Fast supplier search using optimized Lucene index
     */
    @GetMapping("/supplier")
    public ResponseEntity<List<Product>> searchBySupplier(
            @RequestParam String supplierIds,
            @RequestParam(defaultValue = "1000") int limit) {
        try {
            long startTime = System.currentTimeMillis();
            
            List<String> productIds = luceneSearchService.searchProductsBySupplier(supplierIds, limit);
            List<Product> products = new ArrayList<>();
            
            for (String productId : productIds) {
                Product product = productService.getProductByProductId(productId);
                if (product != null) {
                    products.add(product);
                }
            }
            
            long endTime = System.currentTimeMillis();
            
            System.out.println("Lucene supplier search for '" + supplierIds + "' completed in " + (endTime - startTime) + "ms. Found " + products.size() + " results.");
            
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new ArrayList<>());
        }
    }

    /**
     * Search products by specific field using Lucene
     */
    @GetMapping("/lucene/field")
    public ResponseEntity<List<Product>> searchByFieldWithLucene(
            @RequestParam String field,
            @RequestParam String query,
            @RequestParam(defaultValue = "50") int limit) {
        try {
            long startTime = System.currentTimeMillis();
            
            List<String> productIds = luceneSearchService.searchProductsByField(field, query, limit);
            List<Product> products = new ArrayList<>();
            
            for (String productId : productIds) {
                Product product = productService.getProductByProductId(productId);
                if (product != null) {
                    products.add(product);
                }
            }
            
            long endTime = System.currentTimeMillis();
            
            System.out.println("Lucene field search (" + field + ") completed in " + (endTime - startTime) + "ms. Found " + products.size() + " results.");
            
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new ArrayList<>());
        }
    }

    /**
     * Traditional database search (for comparison)
     */
    @GetMapping("/database")
    public ResponseEntity<List<Product>> searchWithDatabase(
            @RequestParam String query,
            @RequestParam(defaultValue = "50") int limit) {
        try {
            long startTime = System.currentTimeMillis();
            
            List<Product> products = productService.searchProducts(query);
            if (products.size() > limit) {
                products = products.subList(0, limit);
            }
            
            long endTime = System.currentTimeMillis();
            
            System.out.println("Database search completed in " + (endTime - startTime) + "ms. Found " + products.size() + " results.");
            
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new ArrayList<>());
        }
    }

    /**
     * Performance comparison endpoint
     */
    @GetMapping("/compare")
    public ResponseEntity<Map<String, Object>> compareSearchMethods(@RequestParam String query) {
        try {
            // Test Lucene search
            long luceneStart = System.currentTimeMillis();
            List<String> luceneProductIds = luceneSearchService.searchProducts(query, 50);
            long luceneEnd = System.currentTimeMillis();
            long luceneTime = luceneEnd - luceneStart;

            // Test database search
            long dbStart = System.currentTimeMillis();
            List<Product> dbProducts = productService.searchProducts(query);
            long dbEnd = System.currentTimeMillis();
            long dbTime = dbEnd - dbStart;

            return ResponseEntity.ok(Map.of(
                "query", query,
                "lucene", Map.of(
                    "timeMs", luceneTime,
                    "resultsCount", luceneProductIds.size()
                ),
                "database", Map.of(
                    "timeMs", dbTime,
                    "resultsCount", dbProducts.size()
                ),
                "speedupFactor", dbTime > 0 ? (double) dbTime / luceneTime : 0
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to compare search methods: " + e.getMessage()
            ));
        }
    }
}
