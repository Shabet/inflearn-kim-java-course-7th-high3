package section05.lambda.lambda5.mystream;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

// static factory 추가
public class MySteamV2 {

    private List<Integer> internalList;

    private MySteamV2(List<Integer> internalList) {
        this.internalList = internalList;
    }

    // static factory
    public static MySteamV2 of(List<Integer> internalList) {
        return new MySteamV2(internalList);
    }

    public MySteamV2 filter(Predicate<Integer> predicate) {
      List<Integer> filtered = new ArrayList<>();
      for (Integer element : internalList) {
          if (predicate.test(element)) {
              filtered.add(element);
          }
      }
      return new MySteamV2(filtered);
    }
    
    public MySteamV2 map(Function<Integer, Integer> mapper) {
        List<Integer> mapped = new ArrayList<>();
        for (Integer element : internalList) {
            mapped.add(mapper.apply(element));
        }
        return new MySteamV2(mapped);
    }

    public List<Integer> toList() {
        return internalList;
    }
}
