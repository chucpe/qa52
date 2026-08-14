package lesson2_4_2;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Расчет характеристик геометрических фигур ===\n");

        // Создаем список фигур
        List<Shape> shapes = new ArrayList<>();

        // Создаем фигуры с разными цветами
        shapes.add(new Circle(5.0, "Красный", "Золотой"));
        shapes.add(new Circle(3.5, "Синий", "Серебряный"));

        shapes.add(new Rectangle(4.0, 6.0, "Зеленый", "Черный"));
        shapes.add(new Rectangle(7.0, 3.0, "Желтый", "Фиолетовый"));
        shapes.add(new Rectangle(5.0, 5.0)); // квадрат с цветами по умолчанию

        shapes.add(new Triangle(3.0, 4.0, 5.0, "Оранжевый", "Коричневый"));
        shapes.add(new Triangle(5.0, 5.0, 6.0, "Розовый", "Белый"));
        shapes.add(new Triangle(6.0, 8.0, 10.0)); // прямоугольный треугольник с цветами по умолчанию

        // Выводим информацию о каждой фигуре
        System.out.println("Информация о фигурах:\n");
        for (Shape shape : shapes) {
            shape.printInfo();
        }

        // Дополнительная статистика
        System.out.println("=== Статистика ===");
        System.out.println("Всего фигур: " + shapes.size());

        // Подсчет фигур каждого типа
        long circleCount = shapes.stream().filter(s -> s instanceof Circle).count();
        long rectangleCount = shapes.stream().filter(s -> s instanceof Rectangle).count();
        long triangleCount = shapes.stream().filter(s -> s instanceof Triangle).count();

        System.out.println("Кругов: " + circleCount);
        System.out.println("Прямоугольников: " + rectangleCount);
        System.out.println("Треугольников: " + triangleCount);

        // Находим фигуру с максимальной площадью
        Shape maxAreaShape = shapes.stream()
                .max((s1, s2) -> Double.compare(s1.getArea(), s2.getArea()))
                .orElse(null);

        if (maxAreaShape != null) {
            System.out.println("\nФигура с максимальной площадью:");
            System.out.println("Тип: " + maxAreaShape.getClass().getSimpleName());
            System.out.println("Площадь: " + String.format("%.2f", maxAreaShape.getArea()));
        }

        // Демонстрация работы с цветами
        System.out.println("\n=== Изменение цветов ===");
        Circle circle = new Circle(2.0, "Белый", "Черный");
        System.out.println("До изменения:");
        circle.printInfo();

        circle.setFillColor("Золотой");
        circle.setBorderColor("Красный");
        System.out.println("После изменения:");
        circle.printInfo();
    }
}