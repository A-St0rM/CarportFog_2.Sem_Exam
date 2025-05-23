package app.service;

import app.entities.*;
import app.exceptions.DatabaseException;
import app.persistence.ProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

class CalculateBOMTest {
    private CalculateBOM calculateBOM;
    private ProductMapperStub productMapperStub;
    private Customer customer;
    private Order order;

    static class ProductMapperStub extends ProductMapper {
        private final List<ProductVariant> variants = new ArrayList<>();

        public ProductMapperStub() {
            super(null);
        }

        @Override
        public int getProductIdByName(String name) {
            return switch (name.toLowerCase()) {
                case "97x97 mm. trykimp. stolpe" -> 1;
                case "45x195 mm. spærtræ ubh." -> 2;
                case "plastmo ecolite blåtonet 109 mm." -> 3;
                case "plastmo bundskruer 200 stk." -> 4;
                case "4x50 mm. skruer 250 stk." -> 5;
                case "bræddebolt 10x120 mm." -> 6;
                case "firkantskiver 40x40x11 mm." -> 7;
                case "universalbeslag 190 mm. højre" -> 8;
                case "universalbeslag 190 mm. venstre" -> 9;
                case "hulbånd 1x20 mm. 10 meter" -> 10;
                default -> throw new IllegalArgumentException("Ukendt produkt: " + name);
            };
        }

        @Override
        public List<ProductVariant> getVariantsByProductIdAndMinLength(int minLength, int productId) {
            List<ProductVariant> result = new ArrayList<>();
            for (ProductVariant v : variants) {
                if (v.getProduct().getProductId() == productId && v.getLength() >= minLength) {
                    result.add(v);
                }
            }
            return result;
        }

        @Override
        public ProductVariant getVariantByProductIdAndWidth(int productId, int width) {
            for (ProductVariant v : variants) {
                if (v.getProduct().getProductId() == productId && v.getWidth() == width) {
                    return v;
                }
            }
            throw new DatabaseException("Ingen variant fundet");
        }

        @Override
        public int[] getAvailableBeamLengths() {
            return new int[]{300, 360, 420, 480, 540, 600};
        }

        @Override
        public int[] getAvailableRoofWidths() {
            return new int[]{240, 300, 360, 420, 480, 600};
        }
    }

    @BeforeEach
    void setup() {
        productMapperStub = new ProductMapperStub();
        customer = new Customer("test@mail.dk", "Testvej 1", "123", "Testperson", 1000, "Testby");
        order = new Order(300, 600, "pending", 0, customer, false);

        // Add test product variants
        productMapperStub.variants.addAll(List.of(
                // Poles (product_id=1)
                new ProductVariant(1, 300, 97, new Product(1, "97x97 mm. trykimp. Stolpe", "stk", 45)),

                // Beams/Rafters (product_id=2)
                new ProductVariant(2, 300, 45, new Product(2, "45x195 mm. spærtræ ubh.", "stk", 46)),
                new ProductVariant(3, 360, 45, new Product(2, "45x195 mm. spærtræ ubh.", "stk", 46)),
                new ProductVariant(4, 420, 45, new Product(2, "45x195 mm. spærtræ ubh.", "stk", 46)),
                new ProductVariant(5, 480, 45, new Product(2, "45x195 mm. spærtræ ubh.", "stk", 46)),
                new ProductVariant(6, 540, 45, new Product(2, "45x195 mm. spærtræ ubh.", "stk", 46)),
                new ProductVariant(7, 600, 45, new Product(2, "45x195 mm. spærtræ ubh.", "stk", 46)),

                // Roof plates (product_id=3)
                new ProductVariant(8, 109, 240, new Product(3, "Plastmo Ecolite Blåtonet 109 mm.", "stk", 250)),
                new ProductVariant(9, 109, 300, new Product(3, "Plastmo Ecolite Blåtonet 109 mm.", "stk", 250)),
                new ProductVariant(10, 109, 360, new Product(3, "Plastmo Ecolite Blåtonet 109 mm.", "stk", 250)),
                new ProductVariant(11, 109, 420, new Product(3, "Plastmo Ecolite Blåtonet 109 mm.", "stk", 250)),
                new ProductVariant(12, 109, 480, new Product(3, "Plastmo Ecolite Blåtonet 109 mm.", "stk", 250)),
                new ProductVariant(13, 109, 600, new Product(3, "Plastmo Ecolite Blåtonet 109 mm.", "stk", 250)),

                // Other products
                new ProductVariant(26, 0, 0, new Product(4, "plastmo bundskruer 200 stk.", "pakke", 429)),
                new ProductVariant(27, 0, 0, new Product(5, "4x50 mm. skruer 250 stk.", "pakke", 60)),
                new ProductVariant(28, 0, 0, new Product(6, "bræddebolt 10x120 mm.", "stk", 17)),
                new ProductVariant(29, 0, 0, new Product(7, "firkantskiver 40x40x11 mm.", "stk", 9)),
                new ProductVariant(30, 0, 0, new Product(8, "universalbeslag 190 mm. højre", "stk", 50)),
                new ProductVariant(31, 0, 0, new Product(9, "universalbeslag 190 mm. venstre", "stk", 50)),
                new ProductVariant(32, 0, 0, new Product(10, "hulbånd 1x20 mm. 10 meter", "rulle", 349))
        ));

        calculateBOM = new CalculateBOM(productMapperStub);
    }

    @Test
    void testFullCarportBOM() throws DatabaseException {
        calculateBOM.calculateCarport(order);
        List<BOM> bom = calculateBOM.getBom();

        // The expected values are based from a calculation of what exactly a carport with 300width, 600length should return.
        assertTrue(bom.size() >= 9, "Manglende BOM-elementer");
        assertEquals(8, getQuantityForProduct("stolpe"));

        // Had to combine rafters and beams since they're the same product and variant in this instance
        assertEquals(13, getQuantityForBeamsAndRaftersFixedLength300());
        assertEquals(2, getQuantityForProduct("hulbånd"));
        assertEquals(24, getQuantityForProduct("bræddebolt"));
        assertEquals(24, getQuantityForProduct("firkantskiver"));
        assertEquals(11, getQuantityForProduct("venstre"));
        assertEquals(11, getQuantityForProduct("højre"));
        assertEquals(1, getQuantityForProduct("4x50 mm"));
    }

    @Test
    void testFullCarportBOMWithRoof() throws DatabaseException {
        order = new Order(300, 600, "pending", 0, customer, true);
        calculateBOM.calculateCarport(order);
        assertTrue(getQuantityForProduct("Plastmo") > 0);
        assertTrue(getQuantityForProduct("plastmo bundskruer") > 0);
    }

    @Test
    void testMinimalDimensionsPolesAndRafters() throws DatabaseException {
        Order smallOrder = new Order(10, 10, "pending", 0, customer, false);
        calculateBOM.calculateCarport(smallOrder);
        assertEquals(4, getQuantityForProduct("Stolpe"));
        assertEquals(2, getQuantityForProduct("Spær"));
    }

    @Test
    void testNoVariantsErrorHandling() throws DatabaseException {
        productMapperStub.variants.clear();
        assertThrows(DatabaseException.class, () -> calculateBOM.calculateCarport(order));
    }

    @Test
    void testCalculateTotalPriceFromBOM() throws DatabaseException {
        calculateBOM.calculateCarport(order);
        int totalPrice = calculateBOM.calculateTotalPriceFromBOM();

        // In this test-case, the total sum expected would be 4636
        assertEquals(4636, totalPrice);
    }

    // Helper methods
    private int getQuantityForProduct(String partialProductName) {
        for (BOM b : calculateBOM.getBom()) {
            String productName = b.getProductVariant().getProduct().getProductName().toLowerCase();
            if (productName.contains(partialProductName.toLowerCase())) {
                return b.getQuantity();
            }
        }
        return 0;
    }

    private int getQuantityForBeamsAndRaftersFixedLength300() {
        int total = 0;
        for (BOM b : calculateBOM.getBom()) {
            ProductVariant variant = b.getProductVariant();
            if (variant.getProduct().getProductId() == 2 && variant.getLength() == 300) {
                total += b.getQuantity();
            }
        }
        return total;
    }
}