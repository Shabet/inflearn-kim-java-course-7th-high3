package section11.optional;

import java.util.HashMap;
import java.util.Map;

public class OptionalStartMain1 {

    private static final Map<Long, String> map = new HashMap<>();

    static {
        map.put(1L, "Kim");
        map.put(2L, "Seo");
    }

    public static void main(String[] args) {
        findAndPrint(1L);
        findAndPrint(3L);
    }

    private static void findAndPrint(long id) {
        String name = findNameById(id);
        // 1. NullPointerException 유발
//        System.out.println("name = " + name.toUpperCase());

        // 2. if 문 사용
        if (name != null) {
            System.out.println(id + ": " + name.toUpperCase());
        } else {
            System.out.println(id + ": " + "UNKNOWN");
        }
    }

    private static String findNameById(long id) {
        return map.get(id);
    }


}
