package section03.lambda.ex2;

import java.util.List;

public class ReduceExample {

    public static int reduce(List<Integer> list, int initial, MyReducer reducer) {
        int result = initial;

        for (int val : list) {
            result = reducer.reduce(result, val);
        }

        return result;
    }

    public static void main(String[] args) {
        List<Integer> numbers = List.of(1, 2, 3, 4);
        System.out.println("리스트: " + numbers);

        // 1. 합 구하기 (초기값 0, 덧셈 로직)
        int add = reduce(numbers, 0, (x, y) -> x + y);
        System.out.println("합: " + add);

        // 2. 곱 구하기 (초기값 1, 곱셈 로직)
        int mul = reduce(numbers, 1, (x, y) -> x * y);
        System.out.println("곱: " + mul);
    }
}
