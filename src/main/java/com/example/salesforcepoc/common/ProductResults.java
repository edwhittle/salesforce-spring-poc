package com.example.salesforcepoc.common;

import java.util.List;

import com.example.salesforcepoc.entity.Product;


public final class ProductResults {
    private final List<Product> products;
    private final Integer totalProducts;

    public ProductResults(List<Product> products, Integer totalProducts) {
        this.products = products;
        this.totalProducts = totalProducts;
    }

    public List<Product> getProducts() {
        return products;
    }

    public Integer getTotalProducts() {
        return totalProducts;
    }
}