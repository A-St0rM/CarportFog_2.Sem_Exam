package app.service;

import java.util.Locale;

public class Svg {
    private StringBuilder svg;

    public Svg(double x, double y, String viewBox, String width, String height) {
        this.svg = new StringBuilder();
        this.svg.append("<svg");
        this.svg.append(" x=\"" + x + "\"");
        this.svg.append(" y=\"" + y + "\"");
        this.svg.append(" viewBox=\"" + viewBox + "\"");
        this.svg.append(" width=\"" + width + "\"");
        this.svg.append(" height=\"" + height + "\"");
        this.svg.append(" xmlns=\"http://www.w3.org/2000/svg\">\n"); // (best practise) Nødvendigt for at alle browsere forstår sproget/svg formatet.
    }


    public void addRectangle(double x, double y, double height, double width, String style) {
        this.svg.append("  <rect");
        this.svg.append(" x=\"" + x + "\"");
        this.svg.append(" y=\"" + y + "\"");
        this.svg.append(" height=\"" + height + "\"");
        this.svg.append(" width=\"" + width + "\"");
        this.svg.append(" style=\"" + style + "\"");
        this.svg.append(" />\n");
    }

    public void addLine(double x1, double y1, double x2, double y2, String style) {
        if (this.svg == null) {
            System.err.println("Svg.addLine ERROR: svg is not initialized!");
            return;
        }
        this.svg.append("  <line");
        this.svg.append(String.format(Locale.US, " x1=\"%.2f\"", x1));
        this.svg.append(String.format(Locale.US, " y1=\"%.2f\"", y1));
        this.svg.append(String.format(Locale.US, " x2=\"%.2f\"", x2));
        this.svg.append(String.format(Locale.US, " y2=\"%.2f\"", y2));
        this.svg.append(" style=\"" + style + "\"");
        this.svg.append(" />\n");
    }

    public void addText(double x, double y, String textContent, String style, String transform) {
        // font-family:Arial,sans-serif; font-size:10px; fill:black;
        String transformAttribute = (transform != null && !transform.isEmpty()) ? String.format(Locale.US, " transform=\"%s\"", transform) : "";
        this.svg.append(String.format(Locale.US,
                " <text x=\"%.2f\" y=\"%.2f\" style=\"%s\"%s>%s</text>\n",
                x, y, style, transformAttribute, textContent));
    }


    public String toString(){
        this.svg.append("</svg>\n");
        return svg.toString();
    }

    // til  Routing Controller:    app.get(ROUTE_DETAILS, ctx -> OrderController.showSvg(ctx, connectionPool));
}