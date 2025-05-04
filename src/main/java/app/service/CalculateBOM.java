package app.service;

import app.entities.BOM;
import app.entities.MaterialVariant;
import app.entities.Order;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import app.persistence.MaterialMapper;

import java.util.ArrayList;
import java.util.List;

public class CalculateBOM {

    //TODO: kan gøres pænere
    private static final int POLES = 1;
    private static final int RAFTERS = 2;
    private static final int BEAMS = 2;


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
        List<MaterialVariant> materialVariants = MaterialMapper.getVariantsByProductIdAndMinLength(0, POLES, connectionPool);
        MaterialVariant materialVariant = materialVariants.get(0);
        BOM bom = new BOM(0, quantity, "Stolper nedgraves 90cm i jorden", order, materialVariant);

        bomList.add(bom);
    }

    //TODO: Lave den rigtig beregning til stolperne
    public int calculatePolesQuantity() {
        return 2 * (2 + (length - 130) / 340);
    }

    //Remme
    private void calculateBeams(Order order){
        //Det samme steps som ovenover
    }
    //Spær
    private void calculateRafters(Order order){

    }

    public List<BOM> getBom() {
        return bomList;
    }


}
