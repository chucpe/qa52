package lesson2_4_2;

public class Circle implements Shape {
    private double radius;
    private String fillColor;
    private String borderColor;

    // Конструкторы
    public Circle(double radius) {
        this(radius, Shape.DEFAULT_FILL_COLOR, Shape.DEFAULT_BORDER_COLOR);
    }

    public Circle(double radius, String fillColor, String borderColor) {
        if (radius <= 0) {
            throw new IllegalArgumentException("Радиус должен быть положительным числом");
        }
        this.radius = radius;
        this.fillColor = fillColor;
        this.borderColor = borderColor;
    }

    @Override
    public double getPerimeter() {
        return 2 * Math.PI * radius;
    }

    @Override
    public double getArea() {
        return Math.PI * radius * radius;
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
    public double getRadius() {
        return radius;
    }

    // Сеттеры для изменения цветов
    public void setFillColor(String fillColor) {
        this.fillColor = fillColor;
    }

    public void setBorderColor(String borderColor) {
        this.borderColor = borderColor;
    }
}
