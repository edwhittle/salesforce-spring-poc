package com.example.salesforcepoc.service;

import com.example.salesforcepoc.entity.Product;
import com.example.salesforcepoc.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvImportService implements CommandLineRunner {

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private LuceneSearchService luceneSearchService;

    @Override
    public void run(String... args) throws Exception {
        importProductsFromCsv();
    }

    public void importProductsFromCsv() {
        try {
            ClassPathResource resource = new ClassPathResource("data-all.csv");
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                
                List<Product> products = new ArrayList<>();
                String line;
                int lineCount = 0;
                int successfullyImported = 0;
                int duplicatesSkipped = 0;
                int batchSize = 1000; // Process in batches to avoid memory issues
                
                // Skip the header line
                reader.readLine();
                
                // Check existing products to avoid duplicates
                System.out.println("Checking existing products in database...");
                long existingCount = productRepository.count();
                System.out.println("Found " + existingCount + " existing products in database");
                
                if (existingCount > 0) {
                    System.out.println("Database already contains products. Skipping import to avoid duplicates.");
                    System.out.println("Delete existing data if you want to re-import all records.");
                    return;
                }
                
                while ((line = reader.readLine()) != null) { // Import all records
                    String[] fields = line.split("\\|");
                    
                    // Check for minimum required fields (first 5 are essential)
                    if (fields.length >= 5 && !fields[1].trim().isEmpty()) { // Need at least productId
                        Product product = new Product();
                        
                        // Required fields
                        product.setSupplierGroupId(getFieldSafely(fields, 0));
                        product.setProductId(getFieldSafely(fields, 1));
                        product.setSupplier(getFieldSafely(fields, 2));
                        product.setIsPrimarySupplier(getFieldSafely(fields, 3));
                        product.setItemDescription(getFieldSafely(fields, 4));
                        
                        // Optional fields - can be null or empty
                        product.setSmktsMerchCategory(getFieldSafely(fields, 5));
                        product.setLiqMerchCategory(getFieldSafely(fields, 6));
                        product.setDigitalBrandName(getFieldSafely(fields, 7));
                        product.setSubBrandName(getFieldSafely(fields, 8));
                        
                        products.add(product);
                        lineCount++;
                        
                        // Save in batches
                        if (products.size() >= batchSize) {
                            try {
                                List<Product> savedProducts = productRepository.saveAll(products);
                                successfullyImported += savedProducts.size();
                                products.clear();
                                System.out.println("Imported " + lineCount + " products so far... (Successfully saved: " + successfullyImported + ")");
                            } catch (Exception e) {
                                System.err.println("Batch save error at line " + lineCount + ": " + e.getMessage());
                                // Try to save individual products to identify problematic ones
                                for (Product p : products) {
                                    try {
                                        productRepository.save(p);
                                        successfullyImported++;
                                    } catch (Exception individualError) {
                                        duplicatesSkipped++;
                                        if (duplicatesSkipped <= 10) { // Only log first 10 errors
                                            System.err.println("Failed to save product ID " + p.getProductId() + ": " + individualError.getMessage());
                                        }
                                    }
                                }
                                products.clear();
                            }
                        }
                    } else {
                        // Log skipped lines for debugging
                        if (lineCount < 10) { // Only log first 10 skipped lines
                            System.out.println("Skipping line " + (lineCount + 1) + " - insufficient fields or empty productId: " + line.substring(0, Math.min(100, line.length())));
                        }
                    }
                }
                
                // Save remaining products
                if (!products.isEmpty()) {
                    try {
                        List<Product> savedProducts = productRepository.saveAll(products);
                        successfullyImported += savedProducts.size();
                    } catch (Exception e) {
                        System.err.println("Final batch save error: " + e.getMessage());
                        // Try individual saves for remaining products
                        for (Product p : products) {
                            try {
                                productRepository.save(p);
                                successfullyImported++;
                            } catch (Exception individualError) {
                                duplicatesSkipped++;
                            }
                        }
                    }
                }
                
                System.out.println("CSV import completed.");
                System.out.println("Total lines processed: " + lineCount);
                System.out.println("Successfully imported: " + successfullyImported);
                System.out.println("Duplicates/errors skipped: " + duplicatesSkipped);
                
                // Verify actual count in database
                long finalCount = productRepository.count();
                System.out.println("Final database count: " + finalCount);
                
                // Index all products in Lucene for fast searching
                System.out.println("Starting Lucene indexing...");
                long indexStartTime = System.currentTimeMillis();
                luceneSearchService.indexAllProducts();
                long indexEndTime = System.currentTimeMillis();
                System.out.println("Lucene indexing completed in " + (indexEndTime - indexStartTime) + "ms");
                
            }
        } catch (Exception e) {
            System.err.println("Error importing CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Safely get field value from array, returning null if index doesn't exist or value is empty
     */
    private String getFieldSafely(String[] fields, int index) {
        if (index >= fields.length) {
            return null; // Field doesn't exist
        }
        String value = fields[index].trim();
        return value.isEmpty() ? null : value;
    }
}
