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
                int batchSize = 1000; // Process in batches to avoid memory issues
                
                // Skip the header line
                reader.readLine();
                
                while ((line = reader.readLine()) != null) { // Import all records
                    String[] fields = line.split("\\|");
                    
                    if (fields.length >= 9) {
                        Product product = new Product();
                        product.setSupplierGroupId(fields[0].trim());
                        product.setProductId(fields[1].trim());
                        product.setSupplier(fields[2].trim());
                        product.setIsPrimarySupplier(fields[3].trim());
                        product.setItemDescription(fields[4].trim());
                        product.setSmktsMerchCategory(fields[5].trim());
                        product.setLiqMerchCategory(fields[6].trim());
                        product.setDigitalBrandName(fields[7].trim());
                        product.setSubBrandName(fields[8].trim());
                        
                        products.add(product);
                        lineCount++;
                        
                        // Save in batches
                        if (products.size() >= batchSize) {
                            productRepository.saveAll(products);
                            products.clear();
                            System.out.println("Imported " + lineCount + " products so far...");
                        }
                    }
                }
                
                // Save remaining products
                if (!products.isEmpty()) {
                    productRepository.saveAll(products);
                }
                
                System.out.println("CSV import completed. Total products imported: " + lineCount);
                
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
}
