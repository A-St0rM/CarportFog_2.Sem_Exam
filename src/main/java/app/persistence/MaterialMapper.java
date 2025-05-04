package app.persistence;

import app.entities.Material;
import app.entities.MaterialVariant;
import app.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MaterialMapper {

    public static List<MaterialVariant> getVariantsByProductIdAndMinLength(int minLength, int materialId, ConnectionPool connectionPool) throws DatabaseException {

        List<MaterialVariant> variants = new ArrayList<MaterialVariant>();

        String query = "SELECT * FROM material_variants" + "INNER JOIN materials m USING(material_id)" + "WHERE material_id = ? AND length = >= ?";

        try(Connection con = connectionPool.getConnection())
        {
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, materialId);
            ps.setInt(2, minLength);
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                int variantId = rs.getInt("material_variant_id");
                int material_id = rs.getInt("material_id");
                int length = rs.getInt("length");
                String name = rs.getString("name");
                String unit = rs.getString("unit");
                int price = rs.getInt("price");
                Material material = new Material(material_id, name, unit, price);
                MaterialVariant materialVariant = new MaterialVariant(variantId, length, material);
                variants.add(materialVariant);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Couldn't get variants " + e.getMessage());
        }
        return variants;
    }
}
