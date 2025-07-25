package com.example.salesforcepoc.service;

import com.example.salesforcepoc.entity.Product;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class LuceneSearchService {

    private static final String INDEX_DIRECTORY = "./lucene-index";
    private StandardAnalyzer analyzer;
    private FSDirectory indexDirectory;
    private IndexWriter indexWriter;
    
    @Autowired
    private ProductService productService;

    @PostConstruct
    public void init() throws IOException {
        analyzer = new StandardAnalyzer();
        Path indexPath = Paths.get(INDEX_DIRECTORY);
        indexDirectory = FSDirectory.open(indexPath);
        
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        indexWriter = new IndexWriter(indexDirectory, config);
    }

    @PreDestroy
    public void cleanup() throws IOException {
        if (indexWriter != null) {
            indexWriter.close();
        }
        if (indexDirectory != null) {
            indexDirectory.close();
        }
        if (analyzer != null) {
            analyzer.close();
        }
    }

    /**
     * Index all products in the database
     */
    public void indexAllProducts() throws IOException {
        System.out.println("Starting to index all products...");
        
        // Clear existing index
        indexWriter.deleteAll();
        
        List<Product> allProducts = productService.getAllProducts();
        System.out.println("Found " + allProducts.size() + " products to index");
        
        int batchSize = 1000;
        int count = 0;
        
        for (Product product : allProducts) {
            indexProduct(product);
            count++;
            
            if (count % batchSize == 0) {
                indexWriter.commit();
                System.out.println("Indexed " + count + " products...");
            }
        }
        
        indexWriter.commit();
        System.out.println("Indexing completed. Total products indexed: " + count);
    }

    /**
     * Index a single product - optimized for supplier-based searches with brand and description support
     */
    public void indexProduct(Product product) throws IOException {
        Document doc = new Document();
        
        // Store the product ID for retrieval
        doc.add(new StoredField("productId", product.getProductId()));
        
        // Primary index field: supplier (this is our main search target)
        String supplierId = product.getSupplier() != null ? product.getSupplier() : "";
        doc.add(new TextField("supplier", supplierId, Field.Store.YES));
        
        // Secondary searchable fields for fuzzy search
        String itemDescription = product.getItemDescription() != null ? product.getItemDescription() : "";
        String digitalBrandName = product.getDigitalBrandName() != null ? product.getDigitalBrandName() : "";
        String subBrandName = product.getSubBrandName() != null ? product.getSubBrandName() : "";
        
        doc.add(new TextField("itemDescription", itemDescription, Field.Store.YES));
        doc.add(new TextField("digitalBrandName", digitalBrandName, Field.Store.YES));
        doc.add(new TextField("subBrandName", subBrandName, Field.Store.YES));
        
        // Combined brand field for easier searching
        String combinedBrand = String.join(" ", digitalBrandName, subBrandName).trim();
        doc.add(new TextField("brand", combinedBrand, Field.Store.YES));
        
        // Store other essential fields for retrieval but don't heavily index them
        doc.add(new StoredField("supplierGroupId", product.getSupplierGroupId() != null ? product.getSupplierGroupId() : ""));
        doc.add(new StoredField("smktsMerchCategory", product.getSmktsMerchCategory() != null ? product.getSmktsMerchCategory() : ""));
        doc.add(new StoredField("liqMerchCategory", product.getLiqMerchCategory() != null ? product.getLiqMerchCategory() : ""));
        
        // Secondary searchable fields (lighter indexing)
        doc.add(new TextField("productId", product.getProductId(), Field.Store.YES));
        
        // Create a supplier-focused combined field for multi-supplier searches
        String supplierText = String.join(" ", 
            supplierId,
            product.getSupplierGroupId() != null ? product.getSupplierGroupId() : ""
        );
        doc.add(new TextField("supplierSearch", supplierText, Field.Store.NO));
        
        indexWriter.addDocument(doc);
    }

    /**
     * Search products using Lucene - optimized for supplier searches
     */
    public List<String> searchProducts(String searchText, int maxResults) throws Exception {
        if (searchText == null || searchText.trim().isEmpty()) {
            return new ArrayList<>();
        }

        IndexReader reader = DirectoryReader.open(indexDirectory);
        IndexSearcher searcher = new IndexSearcher(reader);
        
        // Default search focuses on supplier field
        QueryParser parser = new QueryParser("supplier", analyzer);
        Query query = parser.parse(searchText.trim());
        
        TopDocs results = searcher.search(query, maxResults);
        List<String> productIds = new ArrayList<>();
        
        for (ScoreDoc scoreDoc : results.scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);
            productIds.add(doc.get("productId"));
        }
        
        reader.close();
        return productIds;
    }

    /**
     * Search products by supplier ID(s) - highly optimized
     */
    public List<String> searchProductsBySupplier(String supplierIds, int maxResults) throws Exception {
        if (supplierIds == null || supplierIds.trim().isEmpty()) {
            return new ArrayList<>();
        }

        IndexReader reader = DirectoryReader.open(indexDirectory);
        IndexSearcher searcher = new IndexSearcher(reader);
        
        // For multiple supplier IDs, create an OR query
        String queryString;
        if (supplierIds.contains(",")) {
            // Multiple suppliers: "supplier1 OR supplier2 OR supplier3"
            String[] suppliers = supplierIds.split(",");
            StringBuilder queryBuilder = new StringBuilder();
            for (int i = 0; i < suppliers.length; i++) {
                if (i > 0) queryBuilder.append(" OR ");
                queryBuilder.append(suppliers[i].trim());
            }
            queryString = queryBuilder.toString();
        } else {
            // Single supplier
            queryString = supplierIds.trim();
        }
        
        QueryParser parser = new QueryParser("supplier", analyzer);
        Query query = parser.parse(queryString);
        
        TopDocs results = searcher.search(query, maxResults);
        List<String> productIds = new ArrayList<>();
        
        for (ScoreDoc scoreDoc : results.scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);
            productIds.add(doc.get("productId"));
        }
        
        reader.close();
        return productIds;
    }

    /**
     * Search products by supplier ID(s) with optional brand and item description filters
     */
    public List<String> searchProductsBySupplierWithFilters(String supplierIds, String brandSearch, 
                                                           String itemDescriptionSearch, int maxResults) throws Exception {
        if (supplierIds == null || supplierIds.trim().isEmpty()) {
            return new ArrayList<>();
        }

        IndexReader reader = DirectoryReader.open(indexDirectory);
        IndexSearcher searcher = new IndexSearcher(reader);
        
        BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
        
        // Add supplier query (required)
        QueryParser supplierParser = new QueryParser("supplier", analyzer);
        String supplierQueryString;
        if (supplierIds.contains(",")) {
            // Multiple suppliers: "supplier1 OR supplier2 OR supplier3"
            String[] suppliers = supplierIds.split(",");
            StringBuilder supplierQueryBuilder = new StringBuilder();
            for (int i = 0; i < suppliers.length; i++) {
                if (i > 0) supplierQueryBuilder.append(" OR ");
                supplierQueryBuilder.append("\"").append(suppliers[i].trim()).append("\"");
            }
            supplierQueryString = supplierQueryBuilder.toString();
        } else {
            // Single supplier
            supplierQueryString = "\"" + supplierIds.trim() + "\"";
        }
        Query supplierQuery = supplierParser.parse(supplierQueryString);
        queryBuilder.add(supplierQuery, BooleanClause.Occur.MUST);
        
        // Add brand search if provided
        if (brandSearch != null && !brandSearch.trim().isEmpty()) {
            QueryParser brandParser = new QueryParser("brand", analyzer);
            // Create fuzzy query for brand search
            String fuzzyBrandQuery = createFuzzyQuery(brandSearch.trim());
            Query brandQuery = brandParser.parse(fuzzyBrandQuery);
            queryBuilder.add(brandQuery, BooleanClause.Occur.MUST);
        }
        
        // Add item description search if provided (fuzzy search)
        if (itemDescriptionSearch != null && !itemDescriptionSearch.trim().isEmpty()) {
            QueryParser descParser = new QueryParser("itemDescription", analyzer);
            // Create fuzzy query for item description
            String fuzzyDescQuery = createFuzzyQuery(itemDescriptionSearch.trim());
            Query descQuery = descParser.parse(fuzzyDescQuery);
            queryBuilder.add(descQuery, BooleanClause.Occur.MUST);
        }
        
        BooleanQuery finalQuery = queryBuilder.build();
        TopDocs results = searcher.search(finalQuery, maxResults);
        List<String> productIds = new ArrayList<>();
        
        for (ScoreDoc scoreDoc : results.scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);
            productIds.add(doc.get("productId"));
        }
        
        reader.close();
        return productIds;
    }

    /**
     * Create a fuzzy query string for better matching
     */
    private String createFuzzyQuery(String searchText) {
        String[] terms = searchText.split("\\s+");
        StringBuilder fuzzyQuery = new StringBuilder();
        
        for (int i = 0; i < terms.length; i++) {
            if (i > 0) fuzzyQuery.append(" AND ");
            String term = terms[i].trim();
            if (term.length() > 4) {
                // Use fuzzy search for longer terms with edit distance of 2
                fuzzyQuery.append("(").append(term).append("~2 OR ").append(term).append("*)");
            } else {
                // Use wildcard for shorter terms
                fuzzyQuery.append(term).append("*");
            }
        }
        
        return fuzzyQuery.toString();
    }
    public List<String> searchProductsByField(String fieldName, String searchText, int maxResults) throws Exception {
        if (searchText == null || searchText.trim().isEmpty()) {
            return new ArrayList<>();
        }

        IndexReader reader = DirectoryReader.open(indexDirectory);
        IndexSearcher searcher = new IndexSearcher(reader);
        
        QueryParser parser = new QueryParser(fieldName, analyzer);
        Query query = parser.parse(searchText.trim());
        
        TopDocs results = searcher.search(query, maxResults);
        List<String> productIds = new ArrayList<>();
        
        for (ScoreDoc scoreDoc : results.scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);
            productIds.add(doc.get("productId"));
        }
        
        reader.close();
        return productIds;
    }

    /**
     * Get index statistics
     */
    public String getIndexStats() throws IOException {
        IndexReader reader = DirectoryReader.open(indexDirectory);
        int numDocs = reader.numDocs();
        reader.close();
        return "Lucene index contains " + numDocs + " documents";
    }
}
