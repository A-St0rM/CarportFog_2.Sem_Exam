package app.entities;

public class MaterialVariant {

    private int materialVariantId;
    private int length;
    private Material material;

    public MaterialVariant(int materialVariantId, int length, Material material) {
        this.materialVariantId = materialVariantId;
        this.length = length;
        this.material = material;
    }

    public MaterialVariant(int length, Material material) {
        this.length = length;
        this.material = material;
    }

    public int getMaterialVariantId() {
        return materialVariantId;
    }

    public int getLength() {
        return length;
    }

    public Material getMaterial() {
        return material;
    }
}
