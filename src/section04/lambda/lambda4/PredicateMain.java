package section04.lambda.lambda4;

import java.util.function.Function;
import java.util.function.Predicate;

public class PredicateMain {

    public static void main(String[] args) {
        // 익명 클래스
        Predicate<Integer> predicate1 = new Predicate<Integer>() {

            @Override
            public boolean test(Integer value) {
                return value % 2 == 0;
            }
        };
        System.out.println(predicate1.test(10));

        // 람다
        Predicate<Integer> predicate2 = value -> value % 2 == 0;
        System.out.println(predicate2.test(10));

        Function<Integer, Boolean> predicate3 = x -> x % 2 == 0;
        System.out.println(predicate3.apply(10));
    }
}
