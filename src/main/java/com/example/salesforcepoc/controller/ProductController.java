package com.example.salesforcepoc.controller;

import com.example.salesforcepoc.entity.Product;
import com.example.salesforcepoc.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    @GetMapping("/supplier/{supplier}")
    public List<Product> getProductsBySupplier(@PathVariable String supplier) {
        return productService.getProductsBySupplier(supplier);
    }
    
    @GetMapping("/suppliers/{suppliers}")
    public List<Product> getProductsBySuppliers(@PathVariable String suppliers) {
        return productService.getProductsBySuppliers(suppliers);
    }
    
    @GetMapping("/suppliers")
    public List<Product> getProductsBySupplierParams(@RequestParam String suppliers) {
        return productService.getProductsBySuppliers(suppliers);
    }
}
