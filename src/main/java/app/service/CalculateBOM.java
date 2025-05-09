package app.service;

import app.entities.BOM;
import app.entities.ProductVariant;
import app.entities.Order;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.ProductMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateBOM {

    private int polesProductId;
    private int beamsProductId;
    private int raftersProductId;
    private int roofProductId;


    private List<BOM> bomList = new ArrayList<>();
    private int width;
    private int length;
    private ConnectionPool connectionPool;

    public CalculateBOM(int width, int length, ConnectionPool connectionPool) throws DatabaseException {
        this.width = width;
        this.length = length;
        this.connectionPool = connectionPool;

        // Slå ID'er op fra databasen én gang. Kan nok gøres bedre. Men bare midlertidligt
        this.polesProductId = ProductMapper.getProductIdByName("97x97 mm. trykimp. Stolpe", connectionPool);
        this.beamsProductId = ProductMapper.getProductIdByName("45x195 mm. spærtræ ubh.", connectionPool);
        this.raftersProductId = ProductMapper.getProductIdByName("45x195 mm. spærtræ ubh.", connectionPool);
        this.roofProductId = ProductMapper.getProductIdByName("Plastmo Ecolite Blåtonet 109 mm.", connectionPool);
    }

    public void calculateCarport(Order order) throws DatabaseException {
        calculatePoles(order);
        calculateBeams(order);
        calculateRafters(order);
        calculateRoofs(order);
    }

    //Stolper
    private void calculatePoles(Order order) throws DatabaseException {
        // Antal stolper
        int quantity = calculatePolesQuantity();

        //Finde længde på stolperne - dvs variant
        List<ProductVariant> productVariants = ProductMapper.getVariantsByProductIdAndMinLength(polesProductId, 300, connectionPool);
        ProductVariant productVariant = productVariants.get(0);
        BOM bom = new BOM(0, quantity, "Stolper nedgraves 90cm i jorden", order, productVariant);

        bomList.add(bom);
    }

    public int calculatePolesQuantity() {
        // Udregnes i cm
        double overhangRear = 30;
        double distanceFirstPole = 100;
        double maxDistanceBetweenPoles = 130;

        double effectiveLength = length - distanceFirstPole - overhangRear;
        int polesBetween = (int) Math.ceil(effectiveLength / maxDistanceBetweenPoles);
        int totalAmountOfPoles = Math.max(2 + polesBetween, 2);

        // Ganger med 2 fordi udregningen tager udgangspunkt i én side
        return totalAmountOfPoles * 2;
    }

    //Remme
//    private void calculateBeams(Order order) throws DatabaseException {
//        // 1. Normalt bruges 2 lange remme (én på hver side), nogle gange opdelt i 3 hvis skur
//        int quantity = 2; // evt. lav mere avanceret udregning ved skur
//
//        // 2. Hent variant (fx 45x195, længde min. carportLength)
//        List<ProductVariant> variants = ProductMapper.getVariantsByProductIdAndMinLength(beamsProductId, order.getCarportLength(), connectionPool);
//        ProductVariant variant = variants.get(0);
//
//        // 3. Tilføj BOM-linje
//        BOM bom = new BOM(0, quantity, "Remme i sider, sadles ned i stolper", order, variant);
//        bomList.add(bom);
//    }

    private void calculateBeams(Order order) throws DatabaseException {
        double totalLength = order.getCarportLength(); // Henter længden i cm
        int sides = 2; // Der er 2 sider med remme

        // 1. Hent optimal kombination af længder (fx 480 + 300)
        Map<Integer, Integer> combination = getOptimalBeamCombination(totalLength);

        // 2. Gå gennem alle længder nøgler og slå antal op (værdi)
        for (Integer length : combination.keySet()) {
            int countForOneSide = combination.get(length);
            int totalCount = countForOneSide * sides;

            // Hent produktvariant (med mindst denne længde)
            List<ProductVariant> variants = ProductMapper.getVariantsByProductIdAndMinLength(beamsProductId, length, connectionPool);

            // Finder den præcise variant
            ProductVariant chosenVariant = null;
            for (ProductVariant variant : variants) {
                if (variant.getLength() == length) {
                    chosenVariant = variant;
                    break;
                }
            }

            if (chosenVariant == null) {
                throw new DatabaseException("Kunne ikke finde produktvariant med længde " + length + " cm.", e.getMessage());
            }

            BOM bom = new BOM(0, totalCount, "Remme i sider, sadles ned i stolper", order, chosenVariant);
            bomList.add(bom);
        }
    }


    private Map<Integer, Integer> getOptimalBeamCombination(double length) {
        // Henter alle rem-længder fra Mapper i en array
        int[] beamLengths = ProductMapper.getAvailableBeamLengths(connectionPool);

        // Find de bedste kombinationer
        Map<Integer, Integer> optimalCombination = new HashMap<>();
        int bestOvershoot = Integer.MAX_VALUE;

        for (int i = 0; i < beamLengths.length; i++) {
            for (int j = i; j < beamLengths.length; j++) {
                int totalLength = beamLengths[i] + beamLengths[j];

                if (totalLength >= length) {
                    int overshoot = totalLength - (int) length;
                    if (overshoot < bestOvershoot) {
                        bestOvershoot = overshoot;
                        optimalCombination.clear();
                        optimalCombination.put(beamLengths[i], 1);
                        optimalCombination.put(beamLengths[j], 1);
                    }
                }
            }
        }
        // Returnerer den optimale kombination. Fx 300 + 480 på en længde af 780
        return optimalCombination;
    }

    //Spær
    private void calculateRafters(Order order) throws DatabaseException {
        // 1. Beregn antal spær – vejledningen siger maks. 60 cm mellemrum
        int spacing = 60;
        double rafterWidth = 4.5;
        int innerLength = (int) (order.getCarportLength() - (rafterWidth * 2)); // fx 780cm minus de 2 spærs bredde i hver ende
        int quantity = (int) Math.ceil((double) innerLength / spacing) + 2; // plus to spær (et i hvert ende)

        // 2. Hent passende spærvariant (fx 45x195, længde min. carportWidth)
        List<ProductVariant> variants = ProductMapper.getVariantsByProductIdAndMinLength(raftersProductId, order.getCarportWidth(), connectionPool);
        ProductVariant variant = variants.get(0);

        // 3. Tilføj BOM-linje
        BOM bom = new BOM(0, quantity, "Spær monteres på rem", order, variant);
        bomList.add(bom);
    }

    //Tag
    // TODO: Denne metode tager kun den totale mængde af tagplader. Den siger ikke fx 2 240cm, 2 360cm
    private int calculateTrapezRoofQuantity(Order order) throws DatabaseException {
     return calculateRoofLengthQuantity(order) * calculateRoofWidthQuantity(order);
    }

    private int calculateRoofWidthQuantity(Order order) throws DatabaseException {
        // Udregning af hvor mange tagplader der skal bruges på bredden
        int carportWidth = order.getCarportWidth();
        Map<Integer, Integer> combination = getOptimalRoofCombination(carportWidth);

        int amountOfRoofsWidth = 0;
        for (Integer count : combination.values()) {
            amountOfRoofsWidth += count;
        }
        return amountOfRoofsWidth;
    }

    private int calculateRoofLengthQuantity(Order order) throws DatabaseException {
        int carportLength = order.getCarportLength();
        int amountOfRoofsLength = (int) Math.ceil(carportLength / 109.0);

        return amountOfRoofsLength * amountOfRoofsLength;
    }

    private Map<Integer, Integer> getOptimalRoofCombination(double width) {
        // Henter alle trapez tag-længder fra Mapper i en array
        int[] roofWidths = ProductMapper.getAvailableRoofWidths(connectionPool);

        // Find de bedste kombinationer
        Map<Integer, Integer> optimalCombination = new HashMap<>();
        int bestOvershoot = Integer.MAX_VALUE;

        for (int i = 0; i < roofWidths.length; i++) {
            for (int j = i; j < roofWidths.length; j++) {
                int totalLength = roofWidths[i] + roofWidths[j];

                if (totalLength >= width) {
                    int overshoot = totalLength - (int) width;
                    if (overshoot < bestOvershoot) {
                        bestOvershoot = overshoot;
                        optimalCombination.clear();
                        optimalCombination.put(roofWidths[i], 1);
                        optimalCombination.put(roofWidths[j], 1);
                    }
                }
            }
        }
        // Returnerer den optimale kombination af tagplader baseret på bredden. Fx 300+300 på en bredde af 6m
        return optimalCombination;
    }

    private void calculateRoofs(Order order) throws DatabaseException {
        int carportWidth = order.getCarportWidth();

        int amountOfRoofsLength = calculateRoofLengthQuantity(order);

        Map<Integer, Integer> widthCombination = getOptimalRoofCombination(carportWidth);

        for (Map.Entry<Integer, Integer> entry : widthCombination.entrySet()) {
            int width = entry.getKey();
            int countPerRow = entry.getValue();
            int totalCount = countPerRow * amountOfRoofsLength;

            List<ProductVariant> variants = ProductMapper.getVariantsByProductIdAndMinLength(roofProductId, width, connectionPool);
            ProductVariant chosenVariant = null;
            for (ProductVariant variant : variants) {
                if (variant.getLength() == width) {
                    chosenVariant = variant;
                    break;
                }
            }

            if (chosenVariant == null) {
                throw new DatabaseException("Kunne ikke finde produktvariant med bredde" + width + "cm.", e.getMessage());
            }
            BOM bom = new BOM(0, totalCount, "Tagplader monteres på spær", order, chosenVariant);
        }
    }


    public List<BOM> getBom() {
        return bomList;
    }

}
