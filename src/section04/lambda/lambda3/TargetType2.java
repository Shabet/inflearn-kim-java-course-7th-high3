package section04.lambda.lambda3;

import java.util.function.Function;

// 자바가 기본으로 제곰하는 Function 사용
public class TargetType2 {

    public static void main(String[] args) {
        Function<String, String> upperCase = s -> s.toUpperCase();
        String result1 = upperCase.apply("Hello");
        System.out.println("result1 = " + result1);

        Function<Integer, Integer> square = i -> i * i;
        Integer result2 = square.apply(3);
        System.out.println("result2 = " + result2);
    }
}
