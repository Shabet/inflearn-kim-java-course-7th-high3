package section05.lambda.lambda5.mystream;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class MyStreamV4<T> {

    /**
     * Sink: 원소 1개를 받아서 처리하는 "통로".
     * 각 단계는 자기 일을 하고 다음 Sink 로 원소를 넘긴다.
     */
    interface Sink<T> {
        void accept(T element);
    }

    // ===== 이 단계가 들고 있는 정보 =====
    private final MyStreamV4<?> previousStage;      // 이전 단계 (Head 는 null)
    private final List<T> source;                   // 소스 리스트 (Head 만 가짐)
    private final Function<Sink, Sink> opWrapSink;  // downstream 을 감싸 upstream Sink 생성

    // Head(소스) 단계용 생성자
    private MyStreamV4(List<T> source) {
        this.previousStage = null;
        this.source = source;
        this.opWrapSink = null;
    }

    // 중간 연산 단계용 생성자
    private MyStreamV4(MyStreamV4<?> previousStage, Function<Sink, Sink> opWrapSink) {
        this.previousStage = previousStage;
        this.source = null;
        this.opWrapSink = opWrapSink;
    }

    public static <T> MyStreamV4<T> of(List<T> source) {
        return new MyStreamV4<>(source);
    }

    // ===== 중간 연산: 계산하지 않는다. 통로 만드는 "방법"만 저장 =====

    public MyStreamV4<T> filter(Predicate<T> predicate) {
        return new MyStreamV4<>(this, downstream -> (Sink<T>) element -> {
            System.out.println("  filter 검사: " + element);
            if (predicate.test(element)) {
                downstream.accept(element);   // 통과한 것만 다음 통로로
            }
            // 탈락하면 그냥 버림 = 다음 단계로 안 감
        });
    }

    public <R> MyStreamV4<R> map(Function<T, R> mapper) {
        return new MyStreamV4<>(this, downstream -> (Sink<T>) element -> {
            R result = mapper.apply(element);
            System.out.println("  map 변환: " + element + " -> " + result);
            downstream.accept(result);        // 변환해서 다음 통로로
        });
    }

    // ===== 최종 연산: 여기서 비로소 실행 =====

    public void forEach(Consumer<T> consumer) {
        // 1. 맨 끝 Sink(소비자) 생성
        Sink<T> terminalSink = consumer::accept;

        // 2. 뒤에서 앞으로 거슬러 올라가며 Sink 를 감싼다
        Sink chainedSink = wrapSink(terminalSink);

        // 3. 소스를 딱 한 번만 순회하며 통로에 밀어넣는다
        System.out.println("=== 실행 시작 ===");
        for (Object element : sourceStage().source) {
            chainedSink.accept(element);
        }
    }

    public List<T> toList() {
        List<T> result = new ArrayList<>();
        forEach(result::add);   // forEach 재사용
        return result;
    }

    // ===== 내부 헬퍼 =====

    /**
     * 마지막 Sink 를 받아, 앞 단계로 거슬러 올라가며 하나씩 감싼다.
     * 조립은 역방향, 데이터 흐름은 정방향이 된다.
     */
    private Sink wrapSink(Sink terminalSink) {
        Sink sink = terminalSink;
        for (MyStreamV4<?> stage = this;
             stage.previousStage != null;      // Head 직전까지
             stage = stage.previousStage) {
            sink = stage.opWrapSink.apply(sink);
        }
        return sink;
    }

    /** 연결 리스트를 타고 맨 앞(Head)까지 올라간다 */
    private MyStreamV4<?> sourceStage() {
        MyStreamV4<?> stage = this;
        while (stage.previousStage != null) {
            stage = stage.previousStage;
        }
        return stage;
    }
}