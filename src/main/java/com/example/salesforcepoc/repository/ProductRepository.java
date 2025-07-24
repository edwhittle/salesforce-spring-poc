package com.example.salesforcepoc.repository;

import com.example.salesforcepoc.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    
    // Main business method - find products by supplier
    List<Product> findBySupplier(String supplier);
    
    // Find products by multiple supplier IDs
    @Query("SELECT p FROM Product p WHERE p.supplier IN :suppliers")
    List<Product> findBySupplierIn(@Param("suppliers") List<String> suppliers);
    
    // Required for SearchController - find product by productId
    Product findByProductId(String productId);
    
    // Required for SearchController database search comparison
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.productId) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.supplier) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.itemDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.digitalBrandName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.subBrandName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.smktsMerchCategory) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.liqMerchCategory) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Product> searchProducts(@Param("searchTerm") String searchTerm);
}
