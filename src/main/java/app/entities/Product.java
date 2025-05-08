package app.entities;

public class Product {

    private int productId;
    private String productName;
    private String unit;
    private double price;

    public Product(int productId, String productName, String unit, double price) {
        this.productId = productId;
        this.productName = productName;
        this.unit = unit;
        this.price = price;
    }

    public Product(String productName, String unit, double price) {
        this.productName = productName;
        this.unit = unit;
        this.price = price;
    }

    public int getproductId() {
        return productId;
    }

    public String getproductName() {
        return productName;
    }

    public String getUnit() {
        return unit;
    }

    public double getPrice() {
        return price;
    }
}
