package section05.lambda.lambda5.mystream;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

// Generic 추가
public class MySteamV3<T> {

    private List<T> internalList;

    private MySteamV3(List<T> internalList) {
        this.internalList = internalList;
    }

    // static factory
    public static <T> MySteamV3<T> of(List<T> internalList) {
        return new MySteamV3<>(internalList);
    }

    public MySteamV3<T> filter(Predicate<T> predicate) {
      List<T> filtered = new ArrayList<>();
      for (T element : internalList) {
          if (predicate.test(element)) {
              filtered.add(element);
          }
      }
//      return new MySteamV3<>(filtered); // 동일한 코드임.
      return MySteamV3.of(filtered);
    }
    
    public <R> MySteamV3<R> map(Function<T, R> mapper) {
        List<R> mapped = new ArrayList<>();
        for (T element : internalList) {
            mapped.add(mapper.apply(element));
        }
//        return new MySteamV3<>(mapped); // 동일한 코드임.
        return MySteamV3.of(mapped);
    }

    public List<T> toList() {
        return internalList;
    }

    // 추가
    public void forEach(Consumer<T> consumer) {
        for (T element : internalList) {
            consumer.accept(element);
        }
    }
}
