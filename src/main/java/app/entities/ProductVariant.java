package app.entities;

public class ProductVariant {

    private int productVariantId;
    private int length;
    private Product product;

    public ProductVariant(int productVariantId, int length, Product product) {
        this.productVariantId = productVariantId;
        this.length = length;
        this.product = product;
    }

    public ProductVariant(int length, Product product) {
        this.length = length;
        this.product = product;
    }

    public int getProductVariantId() {
        return productVariantId;
    }

    public int getLength() {
        return length;
    }

    public Product getProduct() {
        return product;
    }
}
