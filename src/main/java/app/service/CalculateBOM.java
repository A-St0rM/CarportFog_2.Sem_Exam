package app.service;

import app.entities.BOM;
import app.entities.ProductVariant;
import app.entities.Order;
import app.exceptions.DatabaseException;
import app.persistence.ProductMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateBOM {
    private final List<BOM> bomList = new ArrayList<>();
    private final ProductMapper _productMapper;

    public CalculateBOM(ProductMapper productMapper) throws DatabaseException {
        this._productMapper = productMapper;
    }

    public void calculateCarport(Order order) throws DatabaseException {
        calculatePoles(order);
        calculateBeams(order);
        calculateRafters(order);
        calculateRoofs(order);
    }

    public void calculatePoles(Order order) throws DatabaseException {
        int quantity = calculatePolesQuantity(order);

        int productId = _productMapper.getProductIdByName("97x97 mm. trykimp. Stolpe");

        List<ProductVariant> productVariants = _productMapper.getVariantsByProductIdAndMinLength(300, productId);

        if (productVariants.isEmpty()) {
            throw new DatabaseException("Product not found for id " + productId + " with min. length: " + 300);
        }

        ProductVariant productVariant = productVariants.get(0);
        BOM bom = new BOM(0, quantity, "Stolper nedgraves 90cm i jorden", order, productVariant);

        bomList.add(bom);
    }

    public int calculatePolesQuantity(Order order) throws DatabaseException {
        int overhangRear = 30;
        int distanceFirstPole = 100;
        int maxDistanceBetweenPoles = 130;

        int effectiveLength = order.getCarportLength() - distanceFirstPole - overhangRear;
        int polesBetween = (int) Math.ceil((double) effectiveLength / maxDistanceBetweenPoles);
        int totalAmountOfPoles = Math.max(2, polesBetween);

        return totalAmountOfPoles * 2;
    }

    public void calculateBeams(Order order) throws DatabaseException {
        int totalLength = order.getCarportLength();
        int sides = 2;

        int productId = _productMapper.getProductIdByName("45x195 mm. spærtræ ubh.");

        Map<Integer, Integer> combination = getOptimalBeamCombination(totalLength);

        for (Integer length : combination.keySet()) {
            int countForOneSide = combination.get(length);
            int totalCount = countForOneSide * sides;

            List<ProductVariant> variants = _productMapper.getVariantsByProductIdAndMinLength(length, productId);
            ProductVariant chosenVariant = null;
            for (ProductVariant variant : variants) {
                if (variant.getLength() == length) {
                    chosenVariant = variant;
                    break;
                }
            }

            if (chosenVariant == null) {
                throw new DatabaseException("Kunne ikke finde produktvariant med længde " + length + " cm.");
            }

            BOM bom = new BOM(0, totalCount, "Remme i sider, sadles ned i stolper", order, chosenVariant);
            bomList.add(bom);
        }
    }

    public Map<Integer, Integer> getOptimalBeamCombination(int length) {
        int[] beamLengths = _productMapper.getAvailableBeamLengths();

        Map<Integer, Integer> optimalCombination = new HashMap<>();
        int bestOvershoot = Integer.MAX_VALUE;

        for (int i = 0; i < beamLengths.length; i++) {
            for (int j = i; j < beamLengths.length; j++) {
                int totalLength = beamLengths[i] + beamLengths[j];

                if (totalLength >= length) {
                    int overshoot = totalLength - length;
                    if (overshoot < bestOvershoot) {
                        bestOvershoot = overshoot;
                        optimalCombination.clear();
                        optimalCombination.put(beamLengths[i], 1);
                        optimalCombination.put(beamLengths[j], 1);
                    }
                }
            }
        }
        return optimalCombination;
    }

    public void calculateRafters(Order order) throws DatabaseException {
        int spacing = 60;
        double rafterWidth = 4.5;
        int innerLength = (int) (order.getCarportLength() - (rafterWidth * 2));
        int quantity = (int) Math.ceil((double) innerLength / spacing) + 2;

        int productId = _productMapper.getProductIdByName("45x195 mm. spærtræ ubh.");

        // Her henter spærtræ ud fra bredden af carporten (hvilket inputtes som minLength i mapperen)
        List<ProductVariant> variants = _productMapper.getVariantsByProductIdAndMinLength(order.getCarportWidth(), productId);
        ProductVariant variant = variants.get(0);

        BOM bom = new BOM(0, quantity, "Spær monteres på rem", order, variant);
        bomList.add(bom);
    }

    // Denne metode tager kun den totale mængde af tagplader. Den siger ikke fx 2 240cm, 2 360cm
    private int calculateTrapezRoofQuantity(Order order) throws DatabaseException {
        return calculateRoofLengthQuantity(order) * calculateRoofWidthQuantity(order);
    }

    public int calculateRoofWidthQuantity(Order order) throws DatabaseException {
        int carportWidth = order.getCarportWidth();
        Map<Integer, Integer> combination = getOptimalRoofCombination(carportWidth);

        int amountOfRoofsWidth = 0;
        for (Integer count : combination.values()) {
            amountOfRoofsWidth += count;
        }
        return amountOfRoofsWidth;
    }

    public int calculateRoofLengthQuantity(Order order) throws DatabaseException {
        int carportLength = order.getCarportLength();
        int amountOfRoofsLength = (int) Math.ceil(carportLength / 109.0);

        return amountOfRoofsLength;
    }

    public Map<Integer, Integer> getOptimalRoofCombination(int width) {
        int[] roofWidths = _productMapper.getAvailableRoofWidths();

        Map<Integer, Integer> optimalCombination = new HashMap<>();
        int bestOvershoot = Integer.MAX_VALUE;

        // Vi starter med at tjekke om en enkelt plade kan dække hele bredden

        for (int roofWidth : roofWidths) {
            if (roofWidth >= width) {
                int overshoot = roofWidth - width;
                if (overshoot < bestOvershoot) {
                    bestOvershoot = overshoot;
                    optimalCombination.clear();
                    optimalCombination.put(roofWidth, 1); // Brug én plade
                }
            }
        }

        // Hvis ingen enkelt tagplade findes, så tjekker vi kombinationerne

        for (int i = 0; i < roofWidths.length; i++) {
            for (int j = i; j < roofWidths.length; j++) {
                int totalWidth = roofWidths[i] + roofWidths[j];
                if (totalWidth >= width) {
                    int overshoot = totalWidth - width;
                    if (overshoot < bestOvershoot) { // Sammenlign med den bedste løsning indtil videre
                        bestOvershoot = overshoot;
                        optimalCombination.clear();
                        optimalCombination.put(roofWidths[i], 1);
                        optimalCombination.put(roofWidths[j], 1);
                    }
                }
            }
        }

        return optimalCombination;
    }

    public void calculateRoofs(Order order) throws DatabaseException {
        int carportWidth = order.getCarportWidth();
        int productId = _productMapper.getProductIdByName("Plastmo Ecolite Blåtonet 109 mm.");
        Map<Integer, Integer> widthCombination = getOptimalRoofCombination(carportWidth);

        for (Map.Entry<Integer, Integer> entry : widthCombination.entrySet()) {
            int width = entry.getKey();
            int totalCount = entry.getValue() * calculateRoofLengthQuantity(order);

            // Henter vors product_variant med matchende bredde
            ProductVariant variant = _productMapper.getVariantByProductIdAndWidth(productId, width);

            BOM bom = new BOM(0, totalCount, "Tagplader monteres på spær", order, variant);
            bomList.add(bom);
        }
    }

    public List<BOM> getBom() {
        return bomList;
    }
}