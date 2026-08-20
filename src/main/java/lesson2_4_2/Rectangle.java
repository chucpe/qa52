package lesson2_4_2;

public class Rectangle implements Shape {
    private double width;
    private double height;
    private String fillColor;
    private String borderColor;

    // Конструкторы
    public Rectangle(double width, double height) {
        this(width, height, Shape.DEFAULT_FILL_COLOR, Shape.DEFAULT_BORDER_COLOR);
    }

    public Rectangle(double width, double height, String fillColor, String borderColor) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Ширина и высота должны быть положительными числами");
        }
        this.width = width;
        this.height = height;
        this.fillColor = fillColor;
        this.borderColor = borderColor;
    }

    @Override
    public double getPerimeter() {
        return 2 * (width + height);
    }

    @Override
    public double getArea() {
        return width * height;
    }

    @Override
    public String getFillColor() {
        return fillColor;
    }

    @Override
    public String getBorderColor() {
        return borderColor;
    }

    // Геттеры
    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    // Сеттеры
    public void setFillColor(String fillColor) {
        this.fillColor = fillColor;
    }

    public void setBorderColor(String borderColor) {
        this.borderColor = borderColor;
    }
}
