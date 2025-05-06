package app.entities;

public class Admin {

    private String email;
    private String password;

    public Admin(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

}
