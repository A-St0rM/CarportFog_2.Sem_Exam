package app.entities;

public class BOMItem {

    private int OrderItemId;
    private int quantity;
    private String description;
    private Order order;
    private MaterialVariant materialVariant;

    public BOMItem(int orderItemId, int quantity, String description, Order order, MaterialVariant materialVariant) {
        this.OrderItemId = orderItemId;
        this.quantity = quantity;
        this.description = description;
        this.order = order;
        this.materialVariant = materialVariant;
    }

    public BOMItem(int quantity, String description, Order order, MaterialVariant materialVariant) {
        this.quantity = quantity;
        this.description = description;
        this.order = order;
        this.materialVariant = materialVariant;
    }

    public int getOrderItemId() {
        return OrderItemId;
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
