package app.service;

import app.entities.BOM;
import app.entities.Customer;
import app.entities.Order;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.ProductMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CalculateBOMTest {

    private static final String USER = "postgres";
    private static final String PASSWORD = "Ahlam1982";
    private static final String URL = "jdbc:postgresql://157.230.98.129:5432/%s?currentSchema=public";
    private static final String DB = "CarportFog";

    public static final ConnectionPool connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, DB);

    Customer customer = new Customer("Casper@example.com", "Voltvej 5", "12345678", "Casper", 1000);
    Order order = new Order(300, 470, "Kommer", 10000, customer, false);

    private CalculateBOM calculateBOM;
    private ProductMapper productMapper;

    @BeforeEach
    void setUp() {
        productMapper = new ProductMapper(connectionPool);
        calculateBOM = new CalculateBOM(productMapper);
    }

    @AfterEach
    void tearDown() {
        // Ikke nødvendig hvis ny CalculateBOM instantieres i @BeforeEach
    }

    @Test
    void calculateCarportTest() throws DatabaseException {
        // BOM skal være tom til at starte med
        assertEquals(0, calculateBOM.getBom().size());

        // Kald den samlede beregning
        calculateBOM.calculateCarport(order);

        // Bekræft at BOM ikke er tom bagefter
        assertTrue(calculateBOM.getBom().size() > 0);
    }


    @Test
    void calculatePolesTest() {
        // Forventer at størrelsen på styklisten er 0
        assertEquals(0, calculateBOM.getBom().size());

        // Kører metoden for at (forhåbentligt) smide poles ned i listen
        calculateBOM.calculatePoles(order);

        // Forventer at listen nu er 1 i størrelsen
        assertEquals(1, calculateBOM.getBom().size());


    }

    @Test
    void calculatePolesQuantityTest() {
        int expected = 6;
        int actual = calculateBOM.calculatePolesQuantity(order);
        assertEquals(expected, actual);
    }


    @Test
    void calculateBeamsTest() throws DatabaseException {
        // Forventer at størrelsen på styklisten er 0
        assertEquals(0, calculateBOM.getBom().size());

        // Kalder metode
        calculateBOM.calculateBeams(order);

        // Forventer at der er mindst tilføjet 1 beam til styklisten
        assertTrue(calculateBOM.getBom().size() > 0);

        // Vi tjekker om der er et element i BOM med beskrivelsen for remme
        boolean hasBeam = false;
        for (BOM bom : calculateBOM.getBom()) {
            if (bom.getDescription().equals("Remme i sider, sadles ned i stolper")) {
                hasBeam = true;
                break;
            }

        }
        assertTrue(hasBeam);
    }

    @Test
    void getOptimalBeamCombinationTest() {
        // Har smidt 580 ind som længde for at drille metoden mest muligt. Altså den skal finde spøjse kombinationer
        Map<Integer, Integer> combination = calculateBOM.getOptimalBeamCombination(580);

        // Laver testen false hvis kombinationen er tom. Det skal den ikke være.
        assertFalse(combination.isEmpty());

        // Tjekker at den indeholder de optimale længder (i dette tilfælde er det 240+360 || 300 + 300 der er mest optimal)
        assertTrue(combination.containsKey(240) || combination.containsKey(300));
        assertTrue(combination.containsKey(360) || combination.containsKey(300));
    }

    @Test
    void calculateRaftersTest() {

    }

    @Test
    void calculateTrapezRoofQuantityTest() {

    }

    @Test
    void calculateRoofWidthQuantityTest() {

    }

    @Test
    void calculateRoofLengthQuantityTest() {

    }

    @Test
    void getOptimalRoofCombinationTest() {

    }

    @Test
    void calculateRoofsTest() {

    }

    @Test
    void getBomTest() {

    }
}