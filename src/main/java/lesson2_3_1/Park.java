package lesson2_3_1;

public class Park {
    private String parkName;
    private String address;

    // Конструктор парка
    public Park(String parkName, String address) {
        this.parkName = parkName;
        this.address = address;
    }

    // ВНУТРЕННИЙ КЛАСС "Аттракцион"
    public class Attraction {
        private String name;
        private String workingHours;
        private double cost;

        // Конструктор внутреннего класса
        public Attraction(String name, String workingHours, double cost) {
            this.name = name;
            this.workingHours = workingHours;
            this.cost = cost;
        }

        // Метод для вывода информации об аттракционе
        public void printAttractionInfo() {
            System.out.println("Аттракцион: " + name);
            System.out.println("Время работы: " + workingHours);
            System.out.println("Стоимость билета: " + cost + " руб.");
            System.out.println("-------------------------");
        }
    }

    // Пример метода, который позволяет добавить и показать аттракцион (для теста)
    public static void main(String[] args) {
        // Создаем парк
        Park myPark = new Park("Горки", "ул. Ленина, 1");

        // Создаем аттракцион (сначала создаем объект парка, потом через него объект аттракциона)
        Park.Attraction rollerCoaster = myPark.new Attraction("Американские горки", "10:00 - 21:00", 750.0);
        Park.Attraction carousel = myPark.new Attraction("Карусель", "10:00 - 20:00", 250.0);

        // Выводим информацию об аттракционах
        System.out.println("--- АТТРАКЦИОНЫ ПАРКА \"" + myPark.parkName + "\" ---");
        rollerCoaster.printAttractionInfo();
        carousel.printAttractionInfo();
    }
}