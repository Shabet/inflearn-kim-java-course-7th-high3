package section08.stream.basic;

import java.util.List;

public class ImmutableMain {

    public static void main(String[] args) {
        List<Integer> orginList = List.of(1, 2, 3, 4, 5);
        System.out.println("orginList = " + orginList);

        List<Integer> filteredList = orginList.stream()
                .filter(n -> n % 2 == 0)
                .toList();

        System.out.println("filteredList = " + filteredList);
        System.out.println("orginList = " + orginList);
    }
}
