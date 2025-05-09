package app.entities;

public class Customer {

    private int customerId;
    private String customerName;
    private String email;
    private String address;
    private String phone;
    private int postalCode; //TODO: object or variable?


    public Customer(int customerId, String customerName, String email, String address, String phone, int postalCode) {
        this.email = email;
        this.address = address;
        this.phone = phone;
        this.postalCode = postalCode;
        this.customerName = customerName;
    }

    public Customer(String customerName, String email, String address, String phone, int postalCode) {
        this.email = email;
        this.address = address;
        this.phone = phone;
        this.postalCode = postalCode;
        this.customerName = customerName;
    }

    public int getCustomerId() {
        return customerId;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public int getPostalCode() {
        return postalCode;
    }

    public String getCustomerName() {
        return customerName;
    }
}
