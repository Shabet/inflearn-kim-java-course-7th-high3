package section05.lambda.lambda5.filter;

import java.util.ArrayList;
import java.util.List;

public class FilterMainV1 {

    public static void main(String[] args) {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        System.out.println("numbers = " + numbers);

        // 짝수만 거르기
        List<Integer> evenNumbers = filterEvenNumber(numbers);
        System.out.println("evenNumbers = " + evenNumbers);

        // 홀수만 거르기
        List<Integer> oddNumbers = filterOddNumber(numbers);
        System.out.println("oddNumbers = " + oddNumbers);
    }

    private static List<Integer> filterEvenNumber(List<Integer> numbers) {
        List<Integer> filtered = new ArrayList<>();
        for (Integer number : numbers) {
            if (number % 2 == 0) {
                filtered.add(number);
            }
        }
        return filtered;
    }

    private static List<Integer> filterOddNumber(List<Integer> numbers) {
        List<Integer> filtered = new ArrayList<>();
        for (Integer number : numbers) {
            if (number % 2 == 1) {
                filtered.add(number);
            }
        }
        return filtered;
    }
}
