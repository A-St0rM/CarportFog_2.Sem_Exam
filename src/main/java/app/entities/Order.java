package app.entities;

public class Order {

    private int orderId;
    private int carportWidth;
    private int carportLength;
    private String status;
    private int totalPrice;
    private Customer customer;
    private boolean trapezeRoof;

    public Order(int orderId, int carportWidth, int carportLength, String status, int totalPrice, Customer customer, boolean trapezeRoof) {
        this.orderId = orderId;
        this.totalPrice = totalPrice;
        this.carportWidth = carportWidth;
        this.carportLength = carportLength;
        this.status = status;
        this.customer = customer;
        this.trapezeRoof = trapezeRoof;
    }

    public Order(int carportWidth, int carportLength,String status, int totalPrice, Customer customer, boolean trapezeRoof) {
        this.totalPrice = totalPrice;
        this.carportWidth = carportWidth;
        this.carportLength = carportLength;
        this.status = status;
        this.customer = customer;
        this.trapezeRoof = trapezeRoof;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public int setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
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

    public Customer getCustomer() {
        return customer;
    }

    public boolean getTrapezeRoof() {
        return trapezeRoof;
    }

    public String getStatus() {
        return status;
    }


}
