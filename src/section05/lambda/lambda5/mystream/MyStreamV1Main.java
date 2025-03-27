package section05.lambda.lambda5.mystream;

import java.util.List;

public class MyStreamV1Main {

    public static void main(String[] args) {
        // 짝수만 남기고, 남은 값의 2배를 반환
        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        returnValue(numbers);
        methodChain(numbers);
    }

    private static void returnValue(List<Integer> numbers) {
        MySteamV1 stream = new MySteamV1(numbers);

        MySteamV1 filteredStream = stream.filter(n -> n % 2 == 0);
        System.out.println("filteredStream = " + filteredStream.toList());

        MySteamV1 mappedStream = filteredStream.map(n -> n * 2);
        System.out.println("mappedStream = " + mappedStream.toList());
    }

    private static void methodChain(List<Integer> numbers) {
        List<Integer> result = new MySteamV1(numbers).filter(n -> n % 2 == 0).map(n -> n * 2).toList();
        System.out.println("result = " + result);
    }
}
