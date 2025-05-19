package app.persistence;

import app.entities.Product;
import app.entities.ProductVariant;
import app.exceptions.DatabaseException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductMapper {

    private final ConnectionPool connectionPool;

    public ProductMapper(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public List<ProductVariant> getVariantsByProductIdAndMinLength(int minLength, int productId) throws DatabaseException {
        List<ProductVariant> variants = new ArrayList<>();
        String sql = """
                SELECT pv.product_variant_id,
                       pv.length,
                       pv.width,
                       p.product_id,
                       p.name,
                       p.unit,
                       p.price
                FROM product_variants pv
                JOIN products p ON pv.product_id = p.product_id
                WHERE pv.product_id = ? AND pv.length >= ?
                """;

        try (Connection con = connectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setInt(2, minLength);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Product product = new Product(
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getString("unit"),
                        rs.getInt("price")
                );
                ProductVariant variant = new ProductVariant(
                        rs.getInt("product_variant_id"),
                        rs.getInt("length"),
                        rs.getInt("width"),
                        product
                );
                variants.add(variant);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Couldn't get variants: " + e.getMessage());
        }

        return variants;
    }

    public ProductVariant getVariantByProductIdAndWidth(int productId, int width) throws DatabaseException {
        String sql = "SELECT pv.*, p.name, p.unit, p.price " +
                "FROM product_variants pv " +
                "JOIN products p ON pv.product_id = p.product_id " +
                "WHERE pv.product_id = ? AND pv.width = ?";

        try (Connection con = connectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setInt(2, width);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new ProductVariant(
                        rs.getInt("product_variant_id"),
                        rs.getInt("length"),
                        rs.getInt("width"),
                        new Product(
                                productId,
                                rs.getString("name"),
                                rs.getString("unit"),
                                rs.getInt("price")
                        )
                );
            } else {
                throw new DatabaseException("Ingen variant med bredde " + width + " cm for produkt " + productId);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Databasefejl: " + e.getMessage());
        }
    }

    public Product getProductByName(String name) throws DatabaseException {
        try (Connection connection = connectionPool.getConnection()) {
            String sql = "SELECT * FROM products WHERE name = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, name);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new Product(
                                rs.getInt("product_id"),
                                rs.getString("name"),
                                rs.getString("unit"),
                                rs.getInt("price")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Fejl under hentning af produkt med navn: " + name);
        }
        return null;
    }

    public int getProductIdByName(String name) throws DatabaseException {
        String sql = "SELECT product_id FROM products WHERE LOWER(TRIM(name)) = LOWER(TRIM(?))"; // Trim and lowercase so we ensure no mistakes
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("product_id");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Fejl ved opslag af produktnavn", e.getMessage());
        }
        throw new DatabaseException("Product not found: " + name);
    }


    public int[] getAvailableBeamLengths() throws DatabaseException {
        List<Integer> beamLengths = new ArrayList<>();
        String query = "SELECT length FROM product_variants WHERE product_id = ?";

        try (Connection con = connectionPool.getConnection()) {
            PreparedStatement ps = con.prepareStatement(query);
            int productIdForBeams = 2; // Product ID for beams
            ps.setInt(1, productIdForBeams);

            ResultSet rs = ps.executeQuery();

            // Adds all beam-lengths to our list (beamLengths)
            while (rs.next()) {
                int length = rs.getInt("length");
                beamLengths.add(length);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Couldn't get beam lengths: " + e.getMessage(), e.getMessage());
        }

        // Converts List to array and returns
        int[] beamLengthsArray = new int[beamLengths.size()];
        for (int i = 0; i < beamLengths.size(); i++) {
            beamLengthsArray[i] = beamLengths.get(i);
        }
        return beamLengthsArray;
    }

    public int[] getAvailableRoofWidths() throws DatabaseException {
        List<Integer> roofWidths = new ArrayList<>();
        String query = "SELECT width FROM product_variants WHERE product_id = ? AND width >= 240";

        try (Connection con = connectionPool.getConnection()) {
            PreparedStatement ps = con.prepareStatement(query);
            int productIdForRoofs = 3; // Product ID for roof
            ps.setInt(1, productIdForRoofs);

            ResultSet rs = ps.executeQuery();

            // Adds all roof-widths to our list (roofWidths)
            while (rs.next()) {
                int width = rs.getInt("width");
                roofWidths.add(width);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Couldn't get roof widths: " + e.getMessage(), e.getMessage());
        }

        // Converts List to array and returns
        int[] roofWidthsArray = new int[roofWidths.size()];
        for (int i = 0; i < roofWidths.size(); i++) {
            roofWidthsArray[i] = roofWidths.get(i);
        }
        return roofWidthsArray;
    }

    public int insertProduct(Product product) throws DatabaseException {
        String sql = "INSERT INTO products (name, unit, price) VALUES (?, ?, ?)";

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, product.getProductName());
            ps.setString(2, product.getUnit());
            ps.setInt(3, product.getPrice());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1); // Returns product_id
            } else {
                throw new DatabaseException("Produkt blev ikke oprettet.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Fejl ved oprettelse af produkt: " + e.getMessage());
        }
    }

    public void insertProductVariant(ProductVariant variant, int productId) throws DatabaseException {
        String sql = "INSERT INTO product_variants (length, product_id, width) VALUES (?, ?, ?)";

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, variant.getLength());
            ps.setInt(2, productId);
            ps.setInt(3, variant.getWidth());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException("Fejl ved oprettelse af produktvariant: " + e.getMessage());
        }
    }

    public List<Product> getAllProducts() throws DatabaseException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products";

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Product product = new Product(
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getString("unit"),
                        rs.getInt("price")
                );
                products.add(product);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Fejl ved hentning af produkter: " + e.getMessage());
        }

        return products;
    }

    public void updateProductPrice(int productId, int newPrice) throws DatabaseException {
        String sql = "UPDATE products SET price = ? WHERE product_id = ?";

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, newPrice);
            ps.setInt(2, productId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException("Fejl ved opdatering af pris: " + e.getMessage());
        }
    }

    public void deleteProductAndVariants(int productId) throws DatabaseException {
        String deleteVariants = "DELETE FROM product_variants WHERE product_id = ?";
        String deleteProduct = "DELETE FROM products WHERE product_id = ?";

        try (Connection conn = connectionPool.getConnection()) {

            try (PreparedStatement ps1 = conn.prepareStatement(deleteVariants)) {
                ps1.setInt(1, productId);
                ps1.executeUpdate();
            }

            try (PreparedStatement ps2 = conn.prepareStatement(deleteProduct)) {
                ps2.setInt(1, productId);
                ps2.executeUpdate();
            }

        } catch (SQLException e) {
            throw new DatabaseException("Fejl ved sletning af produkt: " + e.getMessage());
        }
    }


}
