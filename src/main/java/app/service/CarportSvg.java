package app.service;

public class CarportSvg {

    private double width;
    private double length;
    private Svg carportSvg;



    public CarportSvg(double width, double length) {
        this.width = width;
        this.length = length;
        carportSvg = new Svg(2, 2,"0 0 855 690", "100%", "auto");
        addBeams();
        addRafters();
        addPoles();
        addDimensions();

    }

    private void addBeams(){
        double beamThickness = 4.5;
        double beamYOffset = 35;
        carportSvg.addRectangle(0, beamYOffset, beamThickness, this.width, "stroke-width:1px; stroke:#000000; fill:#cccccc;");
        carportSvg.addRectangle(0, this.length - beamYOffset - beamThickness, beamThickness, this.width, "stroke-width:1px; stroke:#000000; fill:#cccccc;");
    }

    private void addRafters(){
        for (double i = 0; i< 600; i += 55){

            carportSvg.addRectangle(i, 0, 600, 4.5, "stroke: #000000; fill: #ffffff;");
        }
    }

    private void addPoles(){
        for (double i = 50; i< 600; i += + 130){
            carportSvg.addRectangle(i,30,20,20, "stroke-width: 4px; stroke: #000000; fill: #ffffff");
            carportSvg.addRectangle(i, 560, 20, 20, "stroke-width: 4px; stroke: #000000; fill: #ffffff");
        }
    }

    private void addDimensions(){
        carportSvg.addLine(this.width+50, 0, this.width+50, this.length,"stroke-width: 4px; stroke: #000000; fill: #ffffff" );
        carportSvg.addLine(this.width+80, +35, this.width+80, this.length-35,"stroke-width: 4px; stroke: #000000; fill: #ffffff" );
    }





    @Override
    public String toString() {
        return carportSvg.toString();
    }
}
