package com.example.salesforcepoc.common;

import java.util.List;

import com.example.salesforcepoc.entity.Product;

public class BrandCategoryResults {
    
    private List<Product> products;
    private int totalCount;
    private List<String> brands;
    private List<String> categories;
    
    // Default constructor
    public BrandCategoryResults() {}
    
    // Constructor
    public BrandCategoryResults(List<Product> products, int totalCount, List<String> brands, List<String> categories) {
        this.products = products;
        this.totalCount = totalCount;
        this.brands = brands;
        this.categories = categories;
    }
    
    // Getters and Setters
    public List<Product> getProducts() {
        return products;
    }
    
    public void setProducts(List<Product> products) {
        this.products = products;
    }
    
    public int getTotalCount() {
        return totalCount;
    }
    
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
    
    public List<String> getBrands() {
        return brands;
    }
    
    public void setBrands(List<String> brands) {
        this.brands = brands;
    }
    
    public List<String> getCategories() {
        return categories;
    }
    
    public void setCategories(List<String> categories) {
        this.categories = categories;
    }
}
