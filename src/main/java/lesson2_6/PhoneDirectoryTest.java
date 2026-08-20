package lesson2_6;

import java.util.*;

class PhoneDirectory {
    private Map<String, List<String>> directory;

    public PhoneDirectory() {
        this.directory = new HashMap<>();
    }

    // Метод для добавления записи
    public void add(String lastName, String phoneNumber) {
        // Если фамилия уже существует, добавляем телефон к существующему списку
        if (directory.containsKey(lastName)) {
            List<String> phones = directory.get(lastName);
            // Проверяем, чтобы не было дубликатов
            if (!phones.contains(phoneNumber)) {
                phones.add(phoneNumber);
            }
        } else {
            // Иначе создаем новую запись
            List<String> phones = new ArrayList<>();
            phones.add(phoneNumber);
            directory.put(lastName, phones);
        }
    }

    // Метод для поиска телефонов по фамилии
    public List<String> get(String lastName) {
        return directory.getOrDefault(lastName, new ArrayList<>());
    }

    // Метод для получения всех записей (для демонстрации)
    public void printAll() {
        System.out.println("Телефонный справочник:");
        for (Map.Entry<String, List<String>> entry : directory.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    // Дополнительный метод для удаления номера
    public boolean removeNumber(String lastName, String phoneNumber) {
        if (directory.containsKey(lastName)) {
            List<String> phones = directory.get(lastName);
            boolean removed = phones.remove(phoneNumber);
            if (phones.isEmpty()) {
                directory.remove(lastName);
            }
            return removed;
        }
        return false;
    }
}

public class PhoneDirectoryTest {
    public static void main(String[] args) {
        PhoneDirectory phoneDirectory = new PhoneDirectory();

        // Добавляем записи
        System.out.println("=== Добавление записей ===");
        phoneDirectory.add("Иванов", "+7-999-123-45-67");
        phoneDirectory.add("Петров", "+7-999-234-56-78");
        phoneDirectory.add("Сидоров", "+7-999-345-67-89");
        phoneDirectory.add("Иванов", "+7-999-456-78-90"); // Второй телефон для Иванова
        phoneDirectory.add("Иванов", "+7-999-567-89-01"); // Третий телефон для Иванова
        phoneDirectory.add("Смирнов", "+7-999-678-90-12");
        phoneDirectory.add("Петров", "+7-999-789-01-23"); // Второй телефон для Петрова

        phoneDirectory.printAll();

        // Поиск телефонов по фамилии
        System.out.println("\n=== Поиск телефонов ===");

        String lastName = "Иванов";
        List<String> phones = phoneDirectory.get(lastName);
        System.out.println("Телефоны для фамилии " + lastName + ": " + phones);

        lastName = "Петров";
        phones = phoneDirectory.get(lastName);
        System.out.println("Телефоны для фамилии " + lastName + ": " + phones);

        lastName = "Козлов";
        phones = phoneDirectory.get(lastName);
        System.out.println("Телефоны для фамилии " + lastName + ": " + (phones.isEmpty() ? "не найдены" : phones));

        // Демонстрация удаления номера
        System.out.println("\n=== Удаление номера ===");
        System.out.println("Удаляем номер +7-999-456-78-90 у Иванова: " +
                phoneDirectory.removeNumber("Иванов", "+7-999-456-78-90"));
        System.out.println("Удаляем номер +7-999-000-00-00 у Иванова: " +
                phoneDirectory.removeNumber("Иванов", "+7-999-000-00-00"));

        phoneDirectory.printAll();

        // Проверка удаления фамилии, если не осталось номеров
        System.out.println("\n=== Удаление последнего номера ===");
        phoneDirectory.removeNumber("Смирнов", "+7-999-678-90-12");
        phoneDirectory.printAll();
    }
}
