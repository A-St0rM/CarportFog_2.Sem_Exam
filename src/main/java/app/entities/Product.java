package app.entities;

public class Product {

    private int productId;
    private String productName;
    private String unit;
    private int price;

    public Product(int productId, String productName, String unit, int price) {
        this.productId = productId;
        this.productName = productName;
        this.unit = unit;
        this.price = price;
    }

    public Product(String productName, String unit, int price) {
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

    public int getPrice() {
        return price;
    }

    public int getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }
}
