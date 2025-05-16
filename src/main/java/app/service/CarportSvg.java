package app.service;

import app.entities.Order;
import app.exceptions.DatabaseException;
import app.persistence.ConnectionPool;
import io.javalin.http.Context;

import java.util.Locale;

import static app.Main.connectionPool;
import static java.util.Collections.rotate;

public class CarportSvg {

    private double carportWidth;
    private double carportLength;
    private Svg carportSvg;


    public CarportSvg(Context ctx) {
        Integer length = ctx.sessionAttribute("carportLength");
        Integer width = ctx.sessionAttribute("carportWidth");
        this.carportWidth = width.doubleValue();
        this.carportLength = length.doubleValue();

        double padding = 25.0;
        double minX = 0.0 - padding;
        double minY = -20.0 - padding;
        double viewboxWidth = this.carportLength + (3 * padding); //padding er til ekstra plads
        double viewboxHeight = this.carportWidth - (-20.0) + (2 * padding); //padding er til ekstra plads
        String viewBoxValue = String.format(Locale.US, "%.0f %.0f %.0f %.0f", minX, minY, viewboxWidth, viewboxHeight);
        this.carportSvg = new Svg(0, 0, viewBoxValue, "100%", "auto");
        addPoles();
        addBeams();
        addRafters();
        addDimensions();
        addMetrics();

    }

    private int calculatePoles() {
        int overhangRear = 30;
        int distanceFirstPole = 100;
        int maxDistanceBetweenPoles = 130;

        int effectiveLength = (int) this.carportLength - distanceFirstPole - overhangRear;
        int polesBetween = (int) Math.ceil((double) effectiveLength / maxDistanceBetweenPoles);
        int polesPerRow = Math.max(2, polesBetween);
        return polesPerRow;
    }

    public void addPoles() {
        int polesPerRow = calculatePoles();
        double distributionStart = 100; // 100 cm from the front (left)
        double distributionEnd = this.carportLength - 30.0; //30 cm from the back (right side)
        double lengthToDistributeOn = distributionEnd - distributionStart;

        double spaceBetweenPoles = 0;
        if (polesPerRow > 1) {
            spaceBetweenPoles = lengthToDistributeOn / (polesPerRow - 1);
        }

        // draw each pole
        for (int i = 0; i < polesPerRow; i++) {
            double poleX = distributionStart + (i * spaceBetweenPoles);

            // If it's the last pole place it exactly at the end position.
            if (i == polesPerRow - 1 && polesPerRow > 1) {
                poleX = distributionEnd;
            }
            // Draw top poles (-3.5 is for the visiual perfectionism.
            carportSvg.addRectangle(poleX, 35.0-3.5, 9.7, 9.7, "stroke-width:1px; stroke:#000000; fill:#000000;");
            // Draw the bottom (+1.5 is for the visiual perfectionism.
            carportSvg.addRectangle(poleX, this.carportWidth - 35.0 - 9.7 +1.5, 9.7, 9.7, "stroke-width:1px; stroke:#000000; fill:#000000;");
        }
    }

    public void addRafters() {
        int numberOfRaftersToDraw;
        // Number of gaps = ceiling( (total length) / 59.5cm )
        int gaps = (int) Math.ceil(this.carportLength / (55.0 + 4.5));
        numberOfRaftersToDraw = gaps + 1;
        // Must have at least 2 if we reach here (one at start, one at end)
        numberOfRaftersToDraw = Math.max(2, numberOfRaftersToDraw);

        double spaceBetweenRafters = 0;
        if (numberOfRaftersToDraw > 1) {
            // The spacing between them from the first rafter to the last is the length, minus one rafters thickness(4.5 cm).
            spaceBetweenRafters = (this.carportLength - 4.5) / (numberOfRaftersToDraw - 1);
        }

        // Draws them
        for (int i = 0; i < numberOfRaftersToDraw; i++) {
            double xCoordinate;
            xCoordinate = i * spaceBetweenRafters;
            if (i == numberOfRaftersToDraw - 1) {
                // Ensure the last one is exactly at (carportLength - rafter_thickness)
                xCoordinate = this.carportLength - 4.5;

            }
            // Add rafter: (x, y_is_0, svg_height_is_carportWidth, svg_width_is_rafter_thickness_4.5, style)
            carportSvg.addRectangle(xCoordinate, 0.0, this.carportWidth, 4.5, "stroke:#000000; fill:#ffffff;");
        }
    }

    public void addBeams() {
        // Top beam (height is fatness of the line)
        carportSvg.addRectangle(0.0, 35.0, 2, this.carportLength, "stroke-width:1px; stroke:#000000; fill:#cccccc;");
        // bottom beam (height is fatness of the line)
        carportSvg.addRectangle(0.0, this.carportWidth - 35.0 - 4.5, 2, this.carportLength, "stroke-width:1px; stroke:#000000; fill:#cccccc;");
    }

    private void addDimensions() {
        // linje i toppen (fra første pole til sidste)
        carportSvg.addLine(55.0, -20.0, this.carportLength - 30.0, -20.0, "stroke:black; stroke-width:1px;");
        carportSvg.addLine(55.0, -20.0, 55, -10.0, "stroke:black; stroke-width:1px;"); // små indhak
        carportSvg.addLine(this.carportLength - 30.0, -20.0, this.carportLength - 30, -10.0, "stroke:black; stroke-width:1px;");//små indhak

        // linje til højre (hele carportens bredde
        carportSvg.addLine(this.carportLength + 35.0, 0.0, this.carportLength + 35, this.carportWidth, "stroke:black; stroke-width:1px;");
        carportSvg.addLine(this.carportLength + 35.0, 0.0, this.carportLength + 25, 0, "stroke:black; stroke-width:1px;");//små indhak
        carportSvg.addLine(this.carportLength + 35.0, this.carportWidth, this.carportLength + 25, this.carportWidth, "stroke:black; stroke-width:1px;");//små indhak

        //linje i toppen (hele carportens længde
        carportSvg.addLine(0, -30.0, this.carportLength, -30.0, "stroke:black; stroke-width:1px;");
        carportSvg.addLine(0, -30.0, 0, -10.0, "stroke:black; stroke-width:1px;");//små indhak
        carportSvg.addLine(this.carportLength, -30.0, this.carportLength, -10.0, "stroke:black; stroke-width:1px;");//små indhak

        //linje der viser bredden imellem pælene
        carportSvg.addLine(this.carportLength + 20, 35, this.carportLength + 20, this.carportWidth - 40, "stroke-width:1px; stroke:#000000; fill:#cccccc;");
        carportSvg.addLine(this.carportLength + 20, 35, this.carportLength + 10, 35, "stroke-width:1px; stroke:#000000; fill:#cccccc;"); //små indhak
        carportSvg.addLine(this.carportLength + 20, this.carportWidth - 40, carportLength + 10, this.carportWidth - 40, "stroke-width:1px; stroke:#000000; fill:#cccccc;");//små indhak

    }

    private void addMetrics(){
        carportSvg.addText(this.carportLength/2, -33, this.carportLength + " cm", "font-family:Arial sans-serif font-size:10px; fill:black", null ); //length
        carportSvg.addText(this.carportLength/2, -8, this.carportLength - (55 + 30) + " cm","font-family:Arial sans-serif font-size:10px; fill:black", null ); //between poles
        carportSvg.addText(this.carportLength + 20, (this.carportWidth / 2) -40, this.carportWidth + " cm", "font-family:Arial, sans-serif; font-size:10px; fill:black;", String.format(Locale.US, "rotate(90, %.2f, %.2f)", this.carportLength + 20 , (this.carportWidth / 2) -20));
        carportSvg.addText(this.carportLength + 20, (this.carportWidth / 2) - 25, this.carportWidth - 70 + " cm", "font-family:Arial, sans-serif; font-size:10px; fill:black;", String.format(Locale.US, "rotate(90, %.2f, %.2f)", this.carportLength + 20, (this.carportWidth / 2) - 20));    }


    @Override
    public String toString() {
        return carportSvg.toString();
    }


}

