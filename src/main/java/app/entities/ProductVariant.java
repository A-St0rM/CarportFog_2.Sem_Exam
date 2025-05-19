package app.entities;

public class ProductVariant {

    private int productVariantId;
    private int length;
    private int width;
    private Product product;

    public ProductVariant(int productVariantId, int length, int width, Product product) {
        this.productVariantId = productVariantId;
        this.length = length;
        this.product = product;
        this.width = width;
    }

    public ProductVariant(int length, int width, Product product) {
        this.length = length;
        this.width = width;
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

    public int getWidth() {
        return width;
    }

    public Product getProduct() {
        return product;
    }

}
