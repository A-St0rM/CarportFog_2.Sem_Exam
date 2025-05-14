package app.entities;

public class Customer {

    private int customerId;
    private String email;
    private String address;
    private String phone;
    private String name;
    private int postalCode; //TODO: object or variable?


    public Customer(int customerId, String email, String address, String phone, String name, int postalCode) {
        this.customerId = customerId;
        this.email = email;
        this.address = address;
        this.phone = phone;
        this.name = name;
        this.postalCode = postalCode;
    }

    public Customer(String email, String address, String phone, String name, int postalCode) {
        this.email = email;
        this.address = address;
        this.phone = phone;
        this.name = name;
        this.postalCode = postalCode;
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
    public String getName() {
        return name;
    }
}
