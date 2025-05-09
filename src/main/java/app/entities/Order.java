package app.entities;

public class Order {

    private int orderId;
    private int carportWidth;
    private int carportLength;
    private String status;
    private int totalPrice;
    private Customer customer;

    public Order(int orderId, int carportWidth, int carportLength, String status, int totalPrice, Customer customer) {
        this.orderId = orderId;
        this.carportWidth = carportWidth;
        this.carportLength = carportLength;
        this.totalPrice = totalPrice;
        this.status = status;
        this.customer = customer;
    }

    public Order(int carportWidth, int carportLength, int totalPrice, String status, Customer customer) {
        this.totalPrice = totalPrice;
        this.carportWidth = carportWidth;
        this.carportLength = carportLength;
        this.status = status;
        this.customer = customer;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public int getOrderId() {
        return orderId;
    }

    public int getCarportWidth() {
        return carportWidth;
    }

    public int getCarportLength() {
        return carportLength;
    }

    public String getStatus() {
        return status;
    }

    public Customer getCustomer() {
        return customer;
    }

}
