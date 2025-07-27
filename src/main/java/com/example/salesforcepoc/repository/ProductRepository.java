package com.example.salesforcepoc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.salesforcepoc.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    
    // Main business method - find products by supplier
    List<Product> findBySupplier(String supplier);
    
    // Find products by multiple supplier IDs
    @Query("SELECT p FROM Product p WHERE p.supplier IN :suppliers")
    List<Product> findBySupplierIn(@Param("suppliers") List<String> suppliers);
    
    // Required for SearchController - find product by productId
    Product findByProductId(String productId);
}
