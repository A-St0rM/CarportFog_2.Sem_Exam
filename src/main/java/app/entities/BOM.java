package app.entities;

public class BOM {

    private int bomId;
    private int quantity;
    private String description;
    private Order order;
    private ProductVariant productVariant;

    // BOM med produktvariant (fx stolper, spær)
    public BOM(int bomId, int quantity, String description, Order order, ProductVariant productVariant) {
        this.bomId = bomId;
        this.quantity = quantity;
        this.description = description;
        this.order = order;
        this.productVariant = productVariant;
    }

    public BOM(int quantity, String description, Order order, ProductVariant productVariant) {
        this.quantity = quantity;
        this.description = description;
        this.order = order;
        this.productVariant = productVariant;
    }


    public int getBomId() {
        return bomId;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getDescription() {
        return description;
    }

    public Order getOrder() {
        return order;
    }

    public ProductVariant getProductVariant() {
        return productVariant;
    }

}
