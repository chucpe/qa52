import java.util.*;

class Student {
    private String name;
    private String group;
    private int course;
    private List<Integer> grades;

    public Student(String name, String group, int course, List<Integer> grades) {
        this.name = name;
        this.group = group;
        this.course = course;
        this.grades = new ArrayList<>(grades);
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }

    public int getCourse() {
        return course;
    }

    public List<Integer> getGrades() {
        return grades;
    }

    public double getAverageGrade() {
        if (grades.isEmpty()) {
            return 0;
        }
        int sum = 0;
        for (int grade : grades) {
            sum += grade;
        }
        return (double) sum / grades.size();
    }

    public void setCourse(int course) {
        this.course = course;
    }

    @Override
    public String toString() {
        return String.format("Student{name='%s', group='%s', course=%d, avgGrade=%.2f}",
                name, group, course, getAverageGrade());
    }
}

public class StudentManager {

    // Метод для удаления студентов со средним баллом < 3
    public static void removeStudentsWithLowAverage(Collection<Student> students) {
        Iterator<Student> iterator = students.iterator();
        while (iterator.hasNext()) {
            Student student = iterator.next();
            if (student.getAverageGrade() < 3.0) {
                iterator.remove();
            }
        }
    }

    // Метод для перевода студентов на следующий курс, если средний балл >= 3
    public static void promoteStudents(Collection<Student> students) {
        for (Student student : students) {
            if (student.getAverageGrade() >= 3.0) {
                student.setCourse(student.getCourse() + 1);
            }
        }
    }

    // Метод для печати студентов на заданном курсе
    public static void printStudents(Set<Student> students, int course) {
        System.out.println("Студенты на " + course + " курсе:");
        boolean found = false;
        for (Student student : students) {
            if (student.getCourse() == course) {
                System.out.println(" - " + student.getName() + " (группа: " + student.getGroup() + ")");
                found = true;
            }
        }
        if (!found) {
            System.out.println(" - Студентов на " + course + " курсе не найдено");
        }
    }

    public static void main(String[] args) {
        // Создаем список студентов
        List<Student> studentsList = new ArrayList<>();

        // Добавляем студентов
        studentsList.add(new Student("Иван Петров", "Группа 101", 1, Arrays.asList(5, 4, 5, 3, 4)));
        studentsList.add(new Student("Мария Иванова", "Группа 101", 1, Arrays.asList(2, 3, 2, 3, 2)));
        studentsList.add(new Student("Петр Сидоров", "Группа 102", 2, Arrays.asList(3, 3, 4, 3, 3)));
        studentsList.add(new Student("Анна Смирнова", "Группа 102", 2, Arrays.asList(5, 5, 5, 4, 5)));
        studentsList.add(new Student("Сергей Козлов", "Группа 103", 3, Arrays.asList(2, 2, 2, 3, 2)));
        studentsList.add(new Student("Елена Новикова", "Группа 103", 3, Arrays.asList(4, 4, 5, 4, 4)));

        System.out.println("=== Исходный список студентов ===");
        for (Student s : studentsList) {
            System.out.println(s);
        }

        // Демонстрация работы метода printStudents
        System.out.println("\n=== Печать студентов на курсах ===");
        Set<Student> studentsSet = new HashSet<>(studentsList);
        printStudents(studentsSet, 1);
        printStudents(studentsSet, 2);
        printStudents(studentsSet, 3);
        printStudents(studentsSet, 4);

        // Удаляем студентов с низким средним баллом
        System.out.println("\n=== Удаление студентов со средним баллом < 3 ===");
        removeStudentsWithLowAverage(studentsList);
        System.out.println("Студентов после удаления: " + studentsList.size());
        for (Student s : studentsList) {
            System.out.println(s);
        }

        // Переводим студентов на следующий курс
        System.out.println("\n=== Перевод студентов на следующий курс (средний балл >= 3) ===");
        promoteStudents(studentsList);
        for (Student s : studentsList) {
            System.out.println(s);
        }

        // Обновляем Set после изменений
        studentsSet = new HashSet<>(studentsList);
        System.out.println("\n=== Печать студентов после перевода ===");
        printStudents(studentsSet, 2);
        printStudents(studentsSet, 3);
        printStudents(studentsSet, 4);
    }
}
