package app.persistence;

import app.entities.Product;
import app.entities.ProductVariant;
import app.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductMapper {

    private final ConnectionPool connectionPool;

    public ProductMapper(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public List<ProductVariant> getVariantsByProductIdAndMinLength(int minLength, int productId) throws DatabaseException {

        List<ProductVariant> variants = new ArrayList<ProductVariant>();

        String query = "SELECT * FROM product_variants" + "INNER JOIN products m USING(product_id)" + "WHERE product_id = ? AND length = >= ?";

        try(Connection con = connectionPool.getConnection())
        {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, productId);
            ps.setInt(2, minLength);
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                int variantId = rs.getInt("product_variant_id");
                int product_id = rs.getInt("product_id");
                int length = rs.getInt("length");
                String name = rs.getString("name");
                String unit = rs.getString("unit");
                int price = rs.getInt("price");
                Product product = new Product(product_id, name, unit, price);
                ProductVariant productVariant = new ProductVariant(variantId, length, product);
                variants.add(productVariant);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Couldn't get variants " + e.getMessage());
        }
        return variants;
    }

    public int getProductIdByName(String name) throws DatabaseException {
        String sql = "SELECT product_id FROM products WHERE LOWER(name) = LOWER(?)";
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
        return 0;
    }


    public int[] getAvailableBeamLengths() throws DatabaseException {
        List<Integer> beamLengths = new ArrayList<>();
        String query = "SELECT length FROM product_variants WHERE product_id = ?";

        try (Connection con = connectionPool.getConnection()) {
            PreparedStatement ps = con.prepareStatement(query);
            int productIdForBeams = 2; // Det er produkt-ID’et for remme
            ps.setInt(1, productIdForBeams);

            ResultSet rs = ps.executeQuery();

            // Tilføj alle beam-længder til vores liste
            while (rs.next()) {
                int length = rs.getInt("length");
                beamLengths.add(length);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Couldn't get beam lengths: " + e.getMessage(), e.getMessage());
        }

        // Konverter List til array og returner
        int[] beamLengthsArray = new int[beamLengths.size()];
        for (int i = 0; i < beamLengths.size(); i++) {
            beamLengthsArray[i] = beamLengths.get(i);
        }
        return beamLengthsArray;
    }

    public int[] getAvailableRoofWidths() throws DatabaseException {
        List<Integer> roofWidths = new ArrayList<>();
        String query = "SELECT width FROM product_variants WHERE product_id = ?";

        try (Connection con = connectionPool.getConnection()) {
            PreparedStatement ps = con.prepareStatement(query);
            int productIdForBeams = 3; // Det er produkt-ID’et for tag
            ps.setInt(1, productIdForBeams);

            ResultSet rs = ps.executeQuery();

            // Tilføj alle tag-længder til vores liste
            while (rs.next()) {
                int width = rs.getInt("width");
                roofWidths.add(width);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Couldn't get roof widths: " + e.getMessage(), e.getMessage());
        }

        // Konverter List til array og returner
        int[] roofWidthsArray = new int[roofWidths.size()];
        for (int i = 0; i < roofWidths.size(); i++) {
            roofWidthsArray[i] = roofWidths.get(i);
        }
        return roofWidthsArray;
    }
}
