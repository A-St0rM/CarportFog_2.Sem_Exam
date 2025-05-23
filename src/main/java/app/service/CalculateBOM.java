package app.service;

import app.entities.BOM;
import app.entities.Product;
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

    // Calls all the methods for calculation of the entire BOM for the carport
    public void calculateCarport(Order order) throws DatabaseException {
        bomList.clear();
        calculatePoles(order);
        calculateBeams(order);
        calculateRafters(order);

        if (order.getTrapezeRoof()) {
            calculateRoofs(order);
            calculateScrewsRoofs(order);
        }

        calculateHoleBands(order);
        calculateFittingsBeams(order);
        calculateBoltsBeams(order);
        calculateFittingsRaftersLeft(order);
        calculateFittingsRaftersRight(order);
        calculateScrewsRafters(order);
    }

    // Calculates how many poles we need and puts the quantity and correct product in the bill of materials
    private void calculatePoles(Order order) throws DatabaseException {
        int quantity = calculatePolesQuantity(order);
        int productId = _productMapper.getProductIdByName("97x97 mm. trykimp. Stolpe");

        List<ProductVariant> productVariants = _productMapper.getVariantsByProductIdAndMinLength(300, productId);

        if (productVariants.isEmpty()) {
            throw new DatabaseException("Produkt ikke fundet med ID: " + productId + " med minimum længde på: " + 300);
        }

        ProductVariant productVariant = productVariants.get(0);
        BOM bom = new BOM(0, quantity, "Stolper nedgraves 90cm i jorden", order, productVariant);

        bomList.add(bom);
    }

    // Calculates quantity of poles that is needed based on the length of carport
    private int calculatePolesQuantity(Order order) throws DatabaseException {
        int overhangRear = 30;
        int distanceFirstPole = 100;
        int maxDistanceBetweenPoles = 130;

        int effectiveLength = order.getCarportLength() - distanceFirstPole - overhangRear;
        int polesBetween = (int) Math.ceil((double) effectiveLength / maxDistanceBetweenPoles);
        int totalAmountOfPoles = Math.max(2, polesBetween);

        return totalAmountOfPoles * 2;
    }

    // Calculates which beams would be needed and puts the correct lengths and quantities in the bill of materials
    private void calculateBeams(Order order) throws DatabaseException {
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

    // Finds the best length-combination to have the least amount of wasted material (overshoot)
    private Map<Integer, Integer> getOptimalBeamCombination(int length) {
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

    // Calculates the amount of rafters needed based on the length of the carport
    private int calculateRafterQuantity(Order order) throws DatabaseException {
        // We calculate with 60cm between each rafter
        int spacing = 60;

        // Each rafter is 45mm (4.5cm) wide
        double rafterWidth = 4.5;

        // Here we calculate the actual inner-length, by removing the width of rafters in each end (hence the times 2 at the end)
        int innerLength = (int) (order.getCarportLength() - (rafterWidth * 2));

        // We calculate how many spaces there are, but there's always 1 more rafter than there are spaces, hence the + 1 at the end.
        int quantity = (int) Math.ceil((double) innerLength / spacing) + 1;

        return quantity;
    }

    // Retrieves the amount of rafters and puts them correctly into the bill of materials
    private void calculateRafters(Order order) throws DatabaseException {
        int quantity = calculateRafterQuantity(order);

        // Here we retrieve the productID of the product we're using.
        int productId = _productMapper.getProductIdByName("45x195 mm. spærtræ ubh.");

        // Uses the carports width as length for rafters. Keep in mind we're using the same product for rafters & beams.
        List<ProductVariant> variants = _productMapper.getVariantsByProductIdAndMinLength(order.getCarportWidth(), productId);

        if (variants.isEmpty()) {
            throw new DatabaseException("Ingen spær fundet med længden: " + order.getCarportWidth());
        }

        ProductVariant variant = variants.get(0);
        BOM bom = new BOM(0, quantity, "Spær monteres på rem", order, variant);
        bomList.add(bom);
    }

    // Calculates how many roof-plates (109cm length) is needed based on the length of the carport
    private int calculateRoofLengthQuantity(Order order) throws DatabaseException {
        int carportLength = order.getCarportLength();
        int amountOfRoofsLength = (int) Math.ceil(carportLength / 109.0);

        return amountOfRoofsLength;
    }

    // Finds the optimal roof-combination (in relation to width) with the least amount of wasted material (overshoot).
    private Map<Integer, Integer> getOptimalRoofCombination(int width) {
        int[] roofWidths = _productMapper.getAvailableRoofWidths();

        Map<Integer, Integer> optimalCombination = new HashMap<>();
        int bestOvershoot = Integer.MAX_VALUE;

        // We start by checking if a singular roof-plate can cover the entire width
        for (int roofWidth : roofWidths) {
            if (roofWidth >= width) {
                int overshoot = roofWidth - width;
                if (overshoot < bestOvershoot) {
                    bestOvershoot = overshoot;
                    optimalCombination.clear();
                    optimalCombination.put(roofWidth, 1); // Use 1 roof-plate
                }
            }
        }

        // If no singular roof-plate can cover the width, then we check the combinations of our available ones
        for (int i = 0; i < roofWidths.length; i++) {
            for (int j = i; j < roofWidths.length; j++) {
                int totalWidth = roofWidths[i] + roofWidths[j];
                if (totalWidth >= width) {
                    int overshoot = totalWidth - width;
                    if (overshoot < bestOvershoot) { // Compares with the best solution so far
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

    // Calculates which roof-plates are needed and puts the correct amounts and widths in the bill of materials.
    private void calculateRoofs(Order order) throws DatabaseException {
        int carportWidth = order.getCarportWidth();
        int productId = _productMapper.getProductIdByName("Plastmo Ecolite Blåtonet 109 mm.");
        Map<Integer, Integer> widthCombination = getOptimalRoofCombination(carportWidth);

        for (Map.Entry<Integer, Integer> entry : widthCombination.entrySet()) {
            int width = entry.getKey();
            int totalCount = entry.getValue() * calculateRoofLengthQuantity(order);

            // Gathers our product_variant with matching width.
            ProductVariant variant = _productMapper.getVariantByProductIdAndWidth(productId, width);

            BOM bom = new BOM(0, totalCount, "Tagplader monteres på spær", order, variant);
            bomList.add(bom);
        }
    }

    // We will always need 2 rolls of hole bands. 1 roll for each line.
    private void calculateHoleBands(Order order) throws DatabaseException {
        int quantity = 2;

        int productId = _productMapper.getProductIdByName("hulbånd 1x20 mm. 10 meter");
        ProductVariant variant = _productMapper.getVariantsByProductIdAndMinLength(0, productId).get(0);
        BOM bom = new BOM(0, quantity, "Til vindkryds på spær", order, variant);

        bomList.add(bom);
    }

    // Uses "plastmo bundskruer" (200 screws per pack). Calculates based on the roof-area and screws per m².
    private void calculateScrewsRoofs(Order order) throws DatabaseException {
        int screwsPerM2 = 12; // recommended amount of screws per m².
        int screwsPerPackage = 200;

        // Calculates the area of the carport in m²
        double areaM2 = (order.getCarportWidth() / 100.0) * (order.getCarportLength() / 100.0);
        int totalScrewsNeeded = (int) Math.ceil(areaM2 * screwsPerM2);

        // Calculate amount of needed packs (always rounds up)
        int numberOfPackages = (int) Math.ceil((double) totalScrewsNeeded / screwsPerPackage);

        int productId = _productMapper.getProductIdByName("plastmo bundskruer 200 stk.");
        ProductVariant variant = _productMapper.getVariantsByProductIdAndMinLength(0, productId).get(0);
        BOM bom = new BOM(0, numberOfPackages, "Skruer til tagplader", order, variant);

        bomList.add(bom);
    }

    // Calculates the amount of square washers (firkantskiver) based on the amount of carriage bolts (bræddebolte). 1 carriage bolt uses 1 square washer.
    private void calculateFittingsBeams(Order order) throws DatabaseException {
        // Removes 4 from the total amount of poles, because the ending poles uses less carriage bolts.
        int poleAmountInBetween = calculatePolesQuantity(order) - 4;

        // Amount of carriage bolts for the ending poles. Only uses 2 in each ending pole.
        int boltsOnEnds = 4 * 2;

        // Amount of carriage bolts for poles in between the ending poles.
        int boltsInBetween = poleAmountInBetween * 4;

        // Calculates total amount of carriage bolts, which is the same amount as the square washers (fittings)
        int totalAmountOfFittings = boltsInBetween + boltsOnEnds;

        int productId = _productMapper.getProductIdByName("firkantskiver 40x40x11 mm.");
        ProductVariant variant = _productMapper.getVariantsByProductIdAndMinLength(0, productId).get(0);
        BOM bom = new BOM(0, totalAmountOfFittings, "Til montering af rem på stolper", order, variant);

        bomList.add(bom);

    }

    // Uses 2 carriage bolts (bræddebolte) in the poles of each end, and 4 carriage bolts on the poles in between those.
    private void calculateBoltsBeams(Order order) throws DatabaseException {
        // Removes 4 from the total amount of poles, because the ending poles uses less carriage bolts.
        int poleAmountInBetween = calculatePolesQuantity(order) - 4;

        // Amount of carriage bolts for the ending poles. Only uses 2 in each ending pole.
        int boltsOnEnds = 4 * 2;

        // Amount of carriage bolts for poles in between the ending poles.
        int boltsInBetween = poleAmountInBetween * 4;

        // Calculates total amount of carriage bolts.
        int totalAmountOfBolts = boltsInBetween + boltsOnEnds;

        int productId = _productMapper.getProductIdByName("bræddebolt 10x120 mm.");
        ProductVariant variant = _productMapper.getVariantsByProductIdAndMinLength(0, productId).get(0);
        BOM bom = new BOM(0, totalAmountOfBolts, "Til montering af rem på stolper", order, variant);

        bomList.add(bom);
    }

    // Uses universal bracket left (universalbeslag venstre). Needs to use 1 for each rafter.
    private void calculateFittingsRaftersLeft(Order order) throws DatabaseException {
        int amountOfRafters = calculateRafterQuantity(order);
        int quantityOfLeftFittings = amountOfRafters;

        int productId = _productMapper.getProductIdByName("universalbeslag 190 mm. venstre");
        ProductVariant variant = _productMapper.getVariantsByProductIdAndMinLength(0, productId).get(0);
        BOM bom = new BOM(0, quantityOfLeftFittings, "Til montering af spær på rem", order, variant);

        bomList.add(bom);
    }

    // Uses universal bracket right (universalbeslag right). Needs to use 1 for each rafter.
    private void calculateFittingsRaftersRight(Order order) throws DatabaseException {
        int amountOfRafters = calculateRafterQuantity(order);
        int quantityOfRightFittings = amountOfRafters;

        int productId = _productMapper.getProductIdByName("universalbeslag 190 mm. højre");
        ProductVariant variant = _productMapper.getVariantsByProductIdAndMinLength(0, productId).get(0);
        BOM bom = new BOM(0, quantityOfRightFittings, "Til montering af spær på rem", order, variant);

        bomList.add(bom);
    }

    // Calculates how many screws are needed, based on the amount of rafters, and adds it to the bill of materials.
    private void calculateScrewsRafters(Order order) throws DatabaseException {
        // Rafters needs to use screws based on the amount of fittings. 3 screws per side of each fitting (3 sides times 3 screws = 9 screws per rafter).
        int amountOfRafters = calculateRafterQuantity(order);
        int amountOfScrews = amountOfRafters * 9;

        // We're using bracket screws (beslagskruer), which are 250 screws per pack.
        int screwsPerPackage = 250;
        // Calculate how many packs are needed. Rounds up.
        int numberOfPackages = (int) Math.ceil((double) amountOfScrews / screwsPerPackage);

        int productId = _productMapper.getProductIdByName("4x50 mm. skruer 250 stk.");
        ProductVariant variant = _productMapper.getVariantsByProductIdAndMinLength(0, productId).get(0);
        BOM bom = new BOM(0, numberOfPackages, "Til montering af universalbeslag + hulbånd", order, variant);

        bomList.add(bom);
    }

    // Calculates the total price based on the products in the bill of materials
    public int calculateTotalPriceFromBOM() {
        double unroundedTotal = 0;

        for (BOM bom : bomList) {
            Product p = bom.getProductVariant().getProduct();
            int price = p.getPrice();
            int lengthInCm = bom.getProductVariant().getLength();
            int quantity = bom.getQuantity();
            double line;

            // It's only productId 2 (rafters and beams) that are sold based on price per meter.
            if (p.getProductId() == 2) {
                line = price * (lengthInCm / 100.0) * quantity;
            } else {
                line = price * quantity;
            }

            unroundedTotal += line;
        }

        int roundedTotal = (int) Math.round(unroundedTotal);

        return roundedTotal;
    }

    // This method returns the entire bill of materials.
    public List<BOM> getBom() {
        return bomList;
    }
}