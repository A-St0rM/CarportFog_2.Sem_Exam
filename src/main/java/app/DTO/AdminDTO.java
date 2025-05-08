package app.DTO;

public class AdminDTO {

    private int admin_id;
    private String email;

    public AdminDTO(int admin_id, String email) {
        this.admin_id = admin_id;
        this.email = email;
    }

    public int getAdmin_id() {
        return admin_id;
    }

    public void setAdmin_id(int admin_id) {
        this.admin_id = admin_id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
