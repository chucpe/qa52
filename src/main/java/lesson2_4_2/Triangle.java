package lesson2_4_2;

public class Triangle implements Shape {
    private double sideA;
    private double sideB;
    private double sideC;
    private String fillColor;
    private String borderColor;

    // Конструктор с цветами по умолчанию
    public Triangle(double sideA, double sideB, double sideC) {
        this(sideA, sideB, sideC, Shape.DEFAULT_FILL_COLOR, Shape.DEFAULT_BORDER_COLOR);
    }

    // Основной конструктор
    public Triangle(double sideA, double sideB, double sideC, String fillColor, String borderColor) {
        if (!isValidTriangle(sideA, sideB, sideC)) {
            throw new IllegalArgumentException("Некорректные стороны треугольника: " +
                    sideA + ", " + sideB + ", " + sideC);
        }

        this.sideA = sideA;
        this.sideB = sideB;
        this.sideC = sideC;
        this.fillColor = fillColor;
        this.borderColor = borderColor;
    }

    // Проверка существования треугольника
    private boolean isValidTriangle(double a, double b, double c) {
        return a > 0 && b > 0 && c > 0 &&
                (a + b > c) && (a + c > b) && (b + c > a);
    }

    @Override
    public double getPerimeter() {
        return sideA + sideB + sideC;
    }

    @Override
    public double getArea() {
        // Формула Герона
        double semiPerimeter = getPerimeter() / 2;
        return Math.sqrt(semiPerimeter * (semiPerimeter - sideA) *
                (semiPerimeter - sideB) * (semiPerimeter - sideC));
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
    public double getSideA() {
        return sideA;
    }

    public double getSideB() {
        return sideB;
    }

    public double getSideC() {
        return sideC;
    }

    // Сеттеры
    public void setFillColor(String fillColor) {
        this.fillColor = fillColor;
    }

    public void setBorderColor(String borderColor) {
        this.borderColor = borderColor;
    }
}
