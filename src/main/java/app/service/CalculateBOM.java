package app.service;

import app.entities.BOM;
import app.entities.ProductVariant;
import app.entities.Order;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.ProductMapper;

import java.util.ArrayList;
import java.util.List;

public class CalculateBOM {

    //TODO: kan gøres pænere
    private static final int PRODUCT_ID_POLES = 1;
    private static final int PRODUCT_ID_BEAMS_AND_RAFTERS = 2;


    private List<BOM> bomList = new ArrayList<>();
    private int width;
    private int length;
    private ConnectionPool connectionPool;

    public CalculateBOM(int width, int length, ConnectionPool connectionPool ) {
        this.width = width;
        this.length = length;
        this.connectionPool = connectionPool;
    }

    public void calculateCarport(Order order) throws DatabaseException {
        calculatePoles(order);
        calculateBeams(order);
        calculateRafters(order);
    }

    //Stolper
    private void calculatePoles(Order order) throws DatabaseException {
        // Antal stolper
        int quantity = calculatePolesQuantity();

        //Finde længde på stolperne - dvs variant
        List<ProductVariant> productVariants = ProductMapper.getVariantsByProductIdAndMinLength(PRODUCT_ID_POLES, 300, connectionPool);
        ProductVariant productVariant = productVariants.get(0);
        BOM bom = new BOM(0, quantity, "Stolper nedgraves 90cm i jorden", order, productVariant);

        bomList.add(bom);
    }

    public int calculatePolesQuantity() {
        // Én stolpe i hvert hjørne, plus ekstra for hver 340 cm langs længden
        int ekstraStolper = (int) Math.ceil((length - 130) / 340.0);
        return 2 * (2 + ekstraStolper); // 2 rækker med (2 + ekstra)
    }

    //Remme
    private void calculateBeams(Order order) throws DatabaseException {
        // 1. Normalt bruges 2 lange remme (én på hver side), nogle gange opdelt i 3 hvis skur
        int quantity = 2; // evt. lav mere avanceret udregning ved skur

        // 2. Hent variant (fx 45x195, længde min. carportLength)
        List<ProductVariant> variants = ProductMapper.getVariantsByProductIdAndMinLength(PRODUCT_ID_BEAMS_AND_RAFTERS, order.getCarportLength(), connectionPool);
        ProductVariant variant = variants.get(0);

        // 3. Tilføj BOM-linje
        BOM bom = new BOM(0, quantity, "Remme i sider, sadles ned i stolper", order, variant);
        bomList.add(bom);
    }

    //Spær
    private void calculateRafters(Order order) throws DatabaseException {
        // 1. Beregn antal spær – vejledningen siger maks. 60 cm mellemrum
        int spacing = 60;
        int carportLength = order.getCarportLength(); // fx 780 cm
        int quantity = (int) Math.ceil((double) carportLength / spacing) + 1; // plus ét for første spær

        // 2. Hent passende spærvariant (fx 45x195, længde min. carportWidth)
        List<ProductVariant> variants = ProductMapper.getVariantsByProductIdAndMinLength(PRODUCT_ID_BEAMS_AND_RAFTERS, order.getCarportWidth(), connectionPool);
        ProductVariant variant = variants.get(0);

        // 3. Tilføj BOM-linje
        BOM bom = new BOM(0, quantity, "Spær monteres på rem", order, variant);
        bomList.add(bom);
    }

    public List<BOM> getBom() {
        return bomList;
    }


}
