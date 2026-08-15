package lesson2_4_2;

public interface Shape {
    // Цвета по умолчанию
    String DEFAULT_FILL_COLOR = "Белый";
    String DEFAULT_BORDER_COLOR = "Черный";

    // Основные методы (должны быть реализованы в классах)
    double getPerimeter();
    double getArea();
    String getFillColor();
    String getBorderColor();

    // Дефолтный метод для вывода информации о фигуре
    default void printInfo() {
        System.out.println("Тип фигуры: " + getClass().getSimpleName());
        System.out.println("Периметр: " + String.format("%.2f", getPerimeter()));
        System.out.println("Площадь: " + String.format("%.2f", getArea()));
        System.out.println("Цвет заливки: " + getFillColor());
        System.out.println("Цвет границы: " + getBorderColor());
        System.out.println("------------------------");
    }

    // Дефолтный метод для расчета периметра (может быть переопределен)
    default double calculateDefaultPerimeter() {
        return 0;
    }
}
