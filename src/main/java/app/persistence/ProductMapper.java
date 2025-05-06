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

    public static List<ProductVariant> getVariantsByProductIdAndMinLength(int minLength, int productId, ConnectionPool connectionPool) throws DatabaseException {

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
}
