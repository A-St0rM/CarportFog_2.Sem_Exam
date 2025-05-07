package app.entities;

public class Order {

    private int orderId;
    private int carportWidth;
    private int carportLength;
    private boolean isPaid;
    private int totalPrice;
    private Customer customer;

    public Order(int orderId, int carportWidth, int carportLength, int totalPrice, Customer customer) {
        this.orderId = orderId;
        this.carportWidth = carportWidth;
        this.carportLength = carportLength;
        this.isPaid = false;
        this.customer = customer;
    }

    public Order(int carportWidth, int carportLength, int totalPrice, boolean isPaid, Customer customer) {
        this.totalPrice = totalPrice;
        this.carportWidth = carportWidth;
        this.carportLength = carportLength;
        this.isPaid = isPaid;
        this.customer = customer;
    }

    public Order(int carportWidth, int carportLength, int totalPrice, Customer customer) {
        this.totalPrice = totalPrice;
        this.carportWidth = carportWidth;
        this.carportLength = carportLength;
        this.isPaid = false;
        this.customer = customer;
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

    public boolean isPaid() {
        return isPaid;
    }

    public Customer getCustomer() {
        return customer;
    }

}
