package com.example.johannes.huawei;

public class Product {
    private String barcode;
    private String name;
    private String price;
    private String desc;

    public Product(String barcode, String name, String price, String desc) {
        this.barcode = barcode;
        this.name = name;
        this.price = price;
        this.desc = desc;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getDesc() {
        return desc;
    }
}
