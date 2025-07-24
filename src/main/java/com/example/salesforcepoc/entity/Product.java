package com.example.salesforcepoc.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {
    
    @Id
    @Column(name = "product_id")
    private String productId;
    
    @Column(name = "supplier_group_id")
    private String supplierGroupId;
    
    @Column(name = "supplier")
    private String supplier;
    
    @Column(name = "is_primary_supplier")
    private String isPrimarySupplier;
    
    @Column(name = "item_description", length = 1000)
    private String itemDescription;
    
    @Column(name = "smkts_merch_category")
    private String smktsMerchCategory;
    
    @Column(name = "liq_merch_category")
    private String liqMerchCategory;
    
    @Column(name = "digital_brand_name")
    private String digitalBrandName;
    
    @Column(name = "sub_brand_name")
    private String subBrandName;
    
    // Default constructor
    public Product() {}
    
    // Constructor
    public Product(String productId, String supplierGroupId, String supplier, String isPrimarySupplier, 
                   String itemDescription, String smktsMerchCategory, String liqMerchCategory, 
                   String digitalBrandName, String subBrandName) {
        this.productId = productId;
        this.supplierGroupId = supplierGroupId;
        this.supplier = supplier;
        this.isPrimarySupplier = isPrimarySupplier;
        this.itemDescription = itemDescription;
        this.smktsMerchCategory = smktsMerchCategory;
        this.liqMerchCategory = liqMerchCategory;
        this.digitalBrandName = digitalBrandName;
        this.subBrandName = subBrandName;
    }
    
    // Getters and Setters
    public String getProductId() {
        return productId;
    }
    
    public void setProductId(String productId) {
        this.productId = productId;
    }
    
    public String getSupplierGroupId() {
        return supplierGroupId;
    }
    
    public void setSupplierGroupId(String supplierGroupId) {
        this.supplierGroupId = supplierGroupId;
    }
    
    public String getSupplier() {
        return supplier;
    }
    
    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }
    
    public String getIsPrimarySupplier() {
        return isPrimarySupplier;
    }
    
    public void setIsPrimarySupplier(String isPrimarySupplier) {
        this.isPrimarySupplier = isPrimarySupplier;
    }
    
    public String getItemDescription() {
        return itemDescription;
    }
    
    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }
    
    public String getSmktsMerchCategory() {
        return smktsMerchCategory;
    }
    
    public void setSmktsMerchCategory(String smktsMerchCategory) {
        this.smktsMerchCategory = smktsMerchCategory;
    }
    
    public String getLiqMerchCategory() {
        return liqMerchCategory;
    }
    
    public void setLiqMerchCategory(String liqMerchCategory) {
        this.liqMerchCategory = liqMerchCategory;
    }
    
    public String getDigitalBrandName() {
        return digitalBrandName;
    }
    
    public void setDigitalBrandName(String digitalBrandName) {
        this.digitalBrandName = digitalBrandName;
    }
    
    public String getSubBrandName() {
        return subBrandName;
    }
    
    public void setSubBrandName(String subBrandName) {
        this.subBrandName = subBrandName;
    }
    
    @Override
    public String toString() {
        return "Product{" +
                "productId='" + productId + '\'' +
                ", supplierGroupId='" + supplierGroupId + '\'' +
                ", supplier='" + supplier + '\'' +
                ", isPrimarySupplier='" + isPrimarySupplier + '\'' +
                ", itemDescription='" + itemDescription + '\'' +
                ", smktsMerchCategory='" + smktsMerchCategory + '\'' +
                ", liqMerchCategory='" + liqMerchCategory + '\'' +
                ", digitalBrandName='" + digitalBrandName + '\'' +
                ", subBrandName='" + subBrandName + '\'' +
                '}';
    }
}
