package app.service;

import app.entities.Customer;
import app.entities.Order;
import app.persistence.ConnectionPool;
import app.persistence.ProductMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CalculateBOMTest {

    private static final String USER = "postgres";
    private static final String PASSWORD = "Ahlam1982";
    private static final String URL = "jdbc:postgresql://157.230.98.129:5432/%s?currentSchema=public";
    private static final String DB = "CarportFog";

    public static final ConnectionPool connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, DB);
    public static final ProductMapper productMapper = new ProductMapper(connectionPool);
    public static final CalculateBOM calculateBOM = new CalculateBOM(productMapper);
    Customer customer = new Customer("Casper@example.com", "Voltvej 5", "12345678", "Casper", 1000);
    Order order = new Order(300, 470, "Kommer", 10000, customer, false);
    @BeforeAll
    static void setUp() {

    }

    @Test
    void calculateCarportTest() {

    }


    @Test
    void calculatePolesTest() {

    }
    @Test
    void calculatePolesQuantityTest() {
        assertEquals(6, calculateBOM.calculatePolesQuantity(order));

    }


    @Test
    void calculateBeamsTest(){

    }

    @Test
    void getOptimalBeamCombinationTest(){

    }

    @Test
    void calculateRaftersTest(){

    }

    @Test
    void calculateTrapezRoofQuantityTest(){

    }

    @Test
    void calculateRoofWidthQuantityTest(){

    }

    @Test
    void calculateRoofLengthQuantityTest(){

    }

    @Test
    void getOptimalRoofCombinationTest(){

    }

    @Test
    void calculateRoofsTest(){

    }

    @Test
    void getBomTest(){

    }
}