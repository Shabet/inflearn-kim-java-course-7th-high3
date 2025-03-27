package section05.lambda.lambda5.mystream;

import section05.lambda.lambda5.filter.GenericFilter;
import section05.lambda.lambda5.map.GenericMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Ex1_Number {

    public static void main(String[] args) {
        // 짝수만 남기고, 남은 값의 2배를 반환
        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        List<Integer> directResult = direct(numbers);
        System.out.println("directResult = " + directResult);

        List<Integer> lambdaResult = lambda(numbers);
        System.out.println("lambdaResult = " + lambdaResult);
    }

    private static List<Integer> direct(List<Integer> numbers) {
        List<Integer> result = new ArrayList<>();

        for (Integer number : numbers) {
            if (number % 2 == 0) {
                result.add(number*2);
            }
        }
        return result;
    }

    private static List<Integer> lambda(List<Integer> numbers) {
        List<Integer> filteedrList = GenericFilter.filter(numbers, n -> n % 2 == 0);
        List<Integer> mappedList = GenericMapper.map(filteedrList, n -> n * 2);

        return mappedList;
    }
}
