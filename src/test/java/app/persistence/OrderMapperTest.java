package app.persistence;

import app.entities.*;
import app.entities.Order;
import app.exceptions.DatabaseException;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderMapperTest {

    private static final String USER = "postgres";
    private static final String PASSWORD = System.getenv("password");
    private static final String DB_NAME = "CarportFog";
    private static final String DB_IP = System.getenv("ip");
    private static final String URL = "jdbc:postgresql://" + DB_IP + ":5432/" + DB_NAME + "?currentSchema=test";

    private static ConnectionPool connectionPool;
    private static OrderMapper orderMapper;
    private Customer customer;
    private Product product;
    private ProductVariant productVariant;


    @BeforeAll
    static void setUpClass() {
        try {
            connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, DB_NAME);
            orderMapper = new OrderMapper(connectionPool);

            try (Connection testConnection = connectionPool.getConnection();
                 Statement stmt = testConnection.createStatement()) {

                stmt.execute("DROP VIEW IF EXISTS bill_of_products_view CASCADE;");
                stmt.execute("DROP TABLE IF EXISTS bom_items CASCADE;");
                stmt.execute("DROP TABLE IF EXISTS product_variants CASCADE;");
                stmt.execute("DROP TABLE IF EXISTS products CASCADE;");
                stmt.execute("DROP TABLE IF EXISTS orders CASCADE;");
                stmt.execute("DROP TABLE IF EXISTS customers CASCADE;");
                stmt.execute("DROP TABLE IF EXISTS postal_codes CASCADE;");

                stmt.execute("DROP SEQUENCE IF EXISTS products_product_id_seq CASCADE;");
                stmt.execute("DROP SEQUENCE IF EXISTS product_variants_product_variant_id_seq CASCADE;");
                stmt.execute("DROP SEQUENCE IF EXISTS bom_items_order_item_id_seq CASCADE;");
                stmt.execute("DROP SEQUENCE IF EXISTS orders_order_id_seq CASCADE;");
                stmt.execute("DROP SEQUENCE IF EXISTS customers_customer_id_seq CASCADE;");

                stmt.execute("CREATE TABLE postal_codes (" +
                        "postal_code INT PRIMARY KEY, " +
                        "city_name VARCHAR(255)" +
                        ");");

                stmt.execute("CREATE TABLE customers (" +
                        "customer_id SERIAL PRIMARY KEY, " +
                        "name VARCHAR(255), " +
                        "email VARCHAR(255) UNIQUE, " +
                        "address VARCHAR(255), " +
                        "phone VARCHAR(255), " +
                        "postal_code INT, " +
                        "FOREIGN KEY (postal_code) REFERENCES postal_codes(postal_code)" +
                        ");");

                stmt.execute("CREATE TABLE orders (" +
                        "order_id SERIAL PRIMARY KEY, " +
                        "carport_width INT, " +
                        "carport_length INT, " +
                        "status VARCHAR(255) DEFAULT 'pending', " +
                        "customer_id INT NOT NULL, " +
                        "total_price INT, " +
                        "trapeze_roof BOOLEAN, " +
                        "FOREIGN KEY (customer_id) REFERENCES customers(customer_id)" +
                        ");");

                stmt.execute("CREATE TABLE products (" +
                        "product_id SERIAL PRIMARY KEY, " +
                        "name VARCHAR(255) NOT NULL, " +
                        "unit VARCHAR(50), " +
                        "price INT" +
                        ");");

                stmt.execute("CREATE TABLE product_variants (" +
                        "product_variant_id SERIAL PRIMARY KEY, " +
                        "product_id INT NOT NULL, " +
                        "length INT, " +
                        "FOREIGN KEY (product_id) REFERENCES products(product_id)" +
                        ");");

                stmt.execute("CREATE TABLE bom_items (" +
                        "order_item_id SERIAL PRIMARY KEY, " +
                        "order_id INT NOT NULL, " +
                        "product_variant_id INT NOT NULL, " +
                        "quantity INT NOT NULL, " +
                        "description VARCHAR(255), " +
                        "FOREIGN KEY (order_id) REFERENCES orders(order_id), " +
                        "FOREIGN KEY (product_variant_id) REFERENCES product_variants(product_variant_id)" +
                        ");");

                stmt.execute(
                        "CREATE VIEW bill_of_products_view AS " +
                                "SELECT " +
                                "   o.order_id, o.carport_width, o.carport_length, o.total_price, o.trapeze_roof, o.status, " +
                                "   p.product_id, p.name, p.unit, p.price, " +
                                "   pv.product_variant_id, pv.length, " +
                                "   bi.order_item_id, bi.quantity, bi.description " +
                                "FROM " +
                                "   orders o " +
                                "JOIN " +
                                "   bom_items bi ON o.order_id = bi.order_id " +
                                "JOIN " +
                                "   product_variants pv ON bi.product_variant_id = pv.product_variant_id " +
                                "JOIN " +
                                "   products p ON pv.product_id = p.product_id" +
                                ";"
                );

                stmt.execute("INSERT INTO postal_codes (postal_code, city_name) VALUES (1000, 'Testby');");
                stmt.execute("INSERT INTO postal_codes (postal_code, city_name) VALUES (2000, 'Andenby');");

            } catch (SQLException e) {
                fail("Fejl ved opbygning af DB: " + e.getMessage());
            }
        } catch (DatabaseException e) {
            fail("Fejl ved start af DB-forbindelse: " + e.getMessage());
        }
    }

    @BeforeEach
    void setUp() {
        try (Connection testConnection = connectionPool.getConnection();
             Statement stmt = testConnection.createStatement()) {

            stmt.execute("DELETE FROM bom_items;");
            stmt.execute("DELETE FROM product_variants;");
            stmt.execute("DELETE FROM products;");
            stmt.execute("DELETE FROM orders;");
            stmt.execute("DELETE FROM customers;");

            stmt.execute("ALTER SEQUENCE customers_customer_id_seq RESTART WITH 1;");
            stmt.execute("ALTER SEQUENCE orders_order_id_seq RESTART WITH 1;");
            stmt.execute("ALTER SEQUENCE products_product_id_seq RESTART WITH 1;");
            stmt.execute("ALTER SEQUENCE product_variants_product_variant_id_seq RESTART WITH 1;");
            stmt.execute("ALTER SEQUENCE bom_items_order_item_id_seq RESTART WITH 1;");

            stmt.execute("INSERT INTO customers (name, email, postal_code) VALUES ('Testperson', 'test@mail.dk', 1000);");
            customer = new Customer(1, "test@mail.dk", "Testvej 1", "123", "Testperson", 1000);

            stmt.execute("INSERT INTO products (name, unit, price) VALUES ('Spær', 'stk', 200);");
            product = new Product(1, "Spær", "stk", 200);

            stmt.execute("INSERT INTO product_variants (product_id, length) VALUES (1, 300);");
            productVariant = new ProductVariant(1, 300, product);

        } catch (SQLException | DatabaseException e) {
            fail("Fejl i oprydning før test: " + e.getMessage());
        }
    }

    @Test
    void insertOrderTest() throws DatabaseException {
        Order nyOrdre = new Order(200, 400, "Afventede", 5000, customer, false);
        Order indsatOrdre = orderMapper.insertOrder(nyOrdre);
        assertTrue(indsatOrdre.getId() > 0);
    }

    @Test
    void getOrderByIdTest() throws DatabaseException {
        Order nyOrdre = new Order(250, 500, "Afventede", 6000, customer, true);
        Order indsatOrdre = orderMapper.insertOrder(nyOrdre);
        int forventetId = indsatOrdre.getId();

        Order hentetOrdre = orderMapper.getOrderById(forventetId);
        assertNotNull(hentetOrdre);
        assertEquals(forventetId, hentetOrdre.getId());
    }

    @Test
    void getAllOrdersWithCustomerInfoTest() throws DatabaseException {
        orderMapper.insertOrder(new Order(300, 600, "Afventede", 7000, customer, true));
        List<Order> alleOrdrer = orderMapper.getAllOrdersWithCustomerInfo();
        assertEquals(1, alleOrdrer.size());
    }

    @Test
    void updateOrderTotalPriceTest() throws DatabaseException {
        Order oprindeligOrdre = new Order(100, 200, "Afventede", 2000, customer, false);
        Order indsatOrdre = orderMapper.insertOrder(oprindeligOrdre);
        int newPrice = 2500;

        orderMapper.updateOrderTotalPrice(indsatOrdre.getId(), newPrice);

        Order opdateretOrdre = orderMapper.getOrderById(indsatOrdre.getId());
        assertEquals(newPrice, opdateretOrdre.getTotalPrice());
    }

    @Test
    void insertBOMItemsTest() throws DatabaseException, SQLException {
        Order ordreForBOM = orderMapper.insertOrder(new Order(150, 300, "Afventemde", 3000, customer, true));
        List<BOM> bomListe = new ArrayList<>();
        bomListe.add(new BOM(0, 5, "Skrue", ordreForBOM, productVariant));

        orderMapper.insertBOMItems(bomListe);

        int antalBomLinjer = 0;
        Connection conn = connectionPool.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM bom_items WHERE order_id = ?");
        ps.setInt(1, ordreForBOM.getId());
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            antalBomLinjer = rs.getInt(1);
        }
        rs.close();
        ps.close();
        conn.close();
        assertEquals(1, antalBomLinjer);
    }


    @Test
    void getBOMForOrderTest() throws DatabaseException, SQLException {
        Order ordreForBOM = orderMapper.insertOrder(new Order(180, 360, "Afventede", 4000, customer, false));


        Connection conn = connectionPool.getConnection();
        Statement stmt = conn.createStatement();
        stmt.execute("INSERT INTO bom_items (order_id, product_variant_id, quantity, description) VALUES " +
                "(" + ordreForBOM.getId() + ", " + productVariant.getProductVariantId() + ", 10, 'Planke')");
        stmt.close();
        conn.close();

        List<BOM> hentetBOM = orderMapper.getBOMForOrder(ordreForBOM.getId());
        assertEquals(1, hentetBOM.size());
    }
}