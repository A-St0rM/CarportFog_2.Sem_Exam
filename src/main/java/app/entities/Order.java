package app.entities;

public class Order {

    private int orderId;
    private int carportWidth;
    private int carportHeight;
    private boolean isPaid;
    private Customer customer;

    public Order(int orderId, int carportWidth, int carportHeight, boolean isPaid, Customer customer) {
        this.orderId = orderId;
        this.carportWidth = carportWidth;
        this.carportHeight = carportHeight;
        this.isPaid = isPaid;
        this.customer = customer;
    }

    public Order(int carportWidth, int carportHeight, boolean isPaid, Customer customer) {
        this.carportWidth = carportWidth;
        this.carportHeight = carportHeight;
        this.isPaid = isPaid;
        this.customer = customer;
    }

    public int getOrderId() {
        return orderId;
    }

    public int getCarportWidth() {
        return carportWidth;
    }

    public int getCarportHeight() {
        return carportHeight;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public Customer getCustomer() {
        return customer;
    }
}
