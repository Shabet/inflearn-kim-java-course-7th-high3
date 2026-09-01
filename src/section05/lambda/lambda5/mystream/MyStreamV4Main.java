package section05.lambda.lambda5.mystream;

import java.util.List;

public class MyStreamV4Main {

    public static void main(String[] args) {
        List<Student> students = List.of(
                new Student("Apple", 100),
                new Student("Banana", 80),
                new Student("Berry", 50),
                new Student("Tomato", 40)
        );

        // 점수가 80점 이상한 학생의 이름을 추출해라.
        List<String> result1 = ex1(students);
        System.out.println("result1 = " + result1);

        // 점수가 80점 이상이면서, 이름이 5글자인 학생의 이름을 대문자로 추출해라.
        List<String> result2 = ex2(students);
        System.out.println("result2 = " + result2);

        System.out.println("--- 중간 연산 등록 (아직 실행 안 됨) ---");
        List<Integer> numbers = List.of(1, 2, 3, 4, 5);
        MyStreamV4<String> stream = MyStreamV4.of(numbers)
                .filter(i -> i % 2 == 0)
                .map(i -> "[" + i * 10 + "]");
        System.out.println("등록 완료. 출력된 게 하나도 없죠?\n");

        stream.forEach(System.out::println);
    }

    private static List<String> ex1(List<Student> students) {
        return MyStreamV4.of(students)
                .filter(s -> s.getScore() >= 80)
                .map(s -> s.getName())
                .toList();
    }

    private static List<String> ex2(List<Student> students) {
        return MyStreamV3.of(students)
                .filter(s -> s.getScore() >= 80)
                .filter(s -> s.getName().length() == 5)
                .map(s -> s.getName())
                .map(name -> name.toUpperCase())
                .toList();
    }
}
