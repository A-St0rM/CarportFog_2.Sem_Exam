package app.entities;

public class Material {

    private int materialId;
    private String materialName;
    private String unit;
    private double price;

    public Material(int materialId, String materialName, String unit, double price) {
        this.materialId = materialId;
        this.materialName = materialName;
        this.unit = unit;
        this.price = price;
    }

    public Material(String materialName, String unit, double price) {
        this.materialName = materialName;
        this.unit = unit;
        this.price = price;
    }

    public int getMaterialId() {
        return materialId;
    }

    public String getMaterialName() {
        return materialName;
    }

    public String getUnit() {
        return unit;
    }

    public double getPrice() {
        return price;
    }
}
