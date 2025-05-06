package app.service;

import app.persistence.ConnectionPool;
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

    @BeforeAll
    static void setUp() {

    }

    @Test
    void calculatePolesQuantity() {
        CalculateBOM calculateBOM = new CalculateBOM(600, 700, connectionPool);

        assertEquals(6, calculateBOM.calculatePolesQuantity());

    }
}