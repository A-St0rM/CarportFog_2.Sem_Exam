package app.entities;

public class BOM {

    private int bomId;
    private int quantity;
    private String description;
    private Order order;
    private MaterialVariant materialVariant;

    public BOM(int bomId, int quantity, String description, Order order, MaterialVariant materialVariant) {
        this.bomId = bomId;
        this.quantity = quantity;
        this.description = description;
        this.order = order;
        this.materialVariant = materialVariant;
    }

    public BOM(int quantity, String description, Order order, MaterialVariant materialVariant) {
        this.quantity = quantity;
        this.description = description;
        this.order = order;
        this.materialVariant = materialVariant;
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

    public MaterialVariant getMaterialVariant() {
        return materialVariant;
    }
}
