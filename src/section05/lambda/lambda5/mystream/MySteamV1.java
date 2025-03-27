package section05.lambda.lambda5.mystream;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class MySteamV1 {

    private List<Integer> internalList;

    public MySteamV1(List<Integer> internalList) {
        this.internalList = internalList;
    }

    public MySteamV1 filter(Predicate<Integer> predicate) {
      List<Integer> filtered = new ArrayList<>();
      for (Integer element : internalList) {
          if (predicate.test(element)) {
              filtered.add(element);
          }
      }
      return new MySteamV1(filtered);
    }
    
    public MySteamV1 map(Function<Integer, Integer> mapper) {
        List<Integer> mapped = new ArrayList<>();
        for (Integer element : internalList) {
            mapped.add(mapper.apply(element));
        }
        return new MySteamV1(mapped);
    }

    public List<Integer> toList() {
        return internalList;
    }
}
