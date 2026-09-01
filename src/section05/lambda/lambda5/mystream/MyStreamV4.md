# MyStreamV4 — 지연 평가 스트림 파이프라인 구현

> V3의 즉시 평가 방식을 버리고, JDK `java.util.stream`과 동일한 구조인
> **Sink 체인 기반 지연 평가 파이프라인**을 직접 구현한 학습용 스트림.

---

## 목차

1. [V3의 한계](#1-v3의-한계)
2. [핵심 아이디어](#2-핵심-아이디어)
3. [전체 소스 코드](#3-전체-소스-코드)
4. [구성 요소 설명](#4-구성-요소-설명)
5. [동작 흐름 추적](#5-동작-흐름-추적)
6. [실행 예제와 출력](#6-실행-예제와-출력)
7. [V3 vs V4 비교](#7-v3-vs-v4-비교)
8. [JDK Stream과의 대응 관계](#8-jdk-stream과의-대응-관계)
9. [확장 과제](#9-확장-과제)
10. [정리](#10-정리)

---

## 1. V3의 한계

V3는 각 중간 연산이 호출되는 즉시 계산을 수행하고, 결과를 새 `ArrayList`에 담아 다음 단계로 넘겼다.

```java
public MySteamV3<T> filter(Predicate<T> predicate) {
    List<T> filtered = new ArrayList<>();     // 중간 리스트 생성
    for (T element : internalList) {          // 루프 발생
        if (predicate.test(element)) {
            filtered.add(element);
        }
    }
    return MySteamV3.of(filtered);
}
```

이 구조에서 다음 코드를 실행하면,

```java
MySteamV3.of(List.of(1, 2, 3, 4, 5))
    .filter(i -> i % 2 == 0)   // 새 ArrayList [2, 4] 생성 + 루프 1회
    .map(i -> i * 10)          // 새 ArrayList [20, 40] 생성 + 루프 1회
    .toList();
```

세 가지 문제가 발생한다.

| 문제 | 설명 |
|------|------|
| **중간 리스트 낭비** | 연산을 N개 연결하면 임시 `ArrayList`가 N개 생성된다 |
| **다중 순회** | 연산을 N개 연결하면 소스를 N번 순회한다 |
| **즉시 평가** | `getFirst()`처럼 1개만 필요해도 전체를 계산한다 |

**V4의 목표: 중간 리스트 0개, 소스 순회 1번, 최종 연산 전까지 계산 없음.**

---

## 2. 핵심 아이디어

### 데이터를 넘기지 말고, 통로를 연결하라

V3는 단계 사이로 **데이터(List)** 를 넘겼다.

```
[소스 List] --데이터--> [filter] --데이터--> [map] --데이터--> [결과]
```

V4는 데이터를 넘기지 않는다. 대신 **"원소 하나를 받으면 무엇을 할지"를 정의한 통로(Sink)** 를 미리 연결해 둔다.

```
[filter 통로] -> [map 통로] -> [forEach 통로]
```

이렇게 연결된 통로에 원소 `1`을 한 번 던지면 끝까지 관통한다. 중간 저장소가 필요 없다.

### 두 단계로 나뉜 실행

| 시점 | 하는 일 |
|------|---------|
| **중간 연산 호출 시** (`filter`, `map`) | 계산하지 않음. "통로를 만드는 방법"만 저장하고 단계를 연결 |
| **최종 연산 호출 시** (`forEach`, `toList`) | 저장해 둔 방법으로 통로를 조립하고, 소스를 1번 순회하며 밀어넣음 |

---

## 3. 전체 소스 코드

```java
package section05.lambda.lambda5.mystream;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class MyStreamV4<T> {

    /**
     * Sink: 원소 1개를 받아서 처리하는 "통로".
     * 각 단계는 자기 일을 하고 다음 Sink로 원소를 넘긴다.
     */
    interface Sink<T> {
        void accept(T element);
    }

    // ===== 이 단계가 들고 있는 정보 =====
    private final MyStreamV4<?> previousStage;      // 이전 단계 (Head는 null)
    private final List<T> source;                   // 소스 리스트 (Head만 가짐)
    private final Function<Sink, Sink> opWrapSink;  // downstream을 감싸 upstream Sink 생성

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

        // 2. 뒤에서 앞으로 거슬러 올라가며 Sink를 감싼다
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
     * 마지막 Sink를 받아, 앞 단계로 거슬러 올라가며 하나씩 감싼다.
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
```

---

## 4. 구성 요소 설명

### 4.1 `Sink<T>` — 파이프라인의 한 칸

```java
interface Sink<T> {
    void accept(T element);
}
```

원소 1개를 받아 처리하는 단일 메서드 인터페이스다. 중요한 점은 **각 Sink가 자기 다음 Sink(`downstream`)를 필드로 들고 있다**는 것이다. 처리를 마치면 `downstream.accept(...)`를 호출해 원소를 다음 칸으로 밀어낸다.

`Consumer<T>`와 시그니처가 같지만 별도로 정의한 이유는, 나중에 `cancellationRequested()`, `begin()`, `end()` 같은 메서드를 추가할 자리를 마련하기 위해서다. (JDK도 같은 이유로 `Sink extends Consumer`를 별도 정의한다.)

### 4.2 세 개의 필드

```java
private final MyStreamV4<?> previousStage;
private final List<T> source;
private final Function<Sink, Sink> opWrapSink;
```

| 필드 | 역할 | Head 단계 | 중간 단계 |
|------|------|-----------|-----------|
| `previousStage` | 이전 단계 참조 (연결 리스트) | `null` | 이전 스테이지 |
| `source` | 원본 데이터 | 실제 List | `null` |
| `opWrapSink` | downstream을 받아 자기 Sink를 만드는 함수 | `null` | 람다 |

각 스트림 객체는 **자기 자신과 이전 단계만 안다.** 다음 단계는 모른다. 그래서 `wrapSink`이 뒤에서 앞으로 거슬러 올라가는 방식으로 조립한다.

### 4.3 `opWrapSink` — 이 구현의 심장

타입이 `Function<Sink, Sink>`인 것에 주목하자.

- **입력**: downstream Sink (다음 단계의 통로)
- **출력**: 그것을 감싼 새 Sink (이 단계의 통로)

`filter`가 저장하는 람다를 풀어 쓰면 이렇다.

```java
downstream -> {
    return (Sink<T>) element -> {
        if (predicate.test(element)) {
            downstream.accept(element);
        }
    };
}
```

즉 **"다음 통로를 주면, 그 앞에 붙일 통로를 만들어 주겠다"** 는 약속만 저장해 둔 것이다. 이 시점에 `predicate`는 한 번도 실행되지 않는다.

### 4.4 `wrapSink()` — 역방향 조립

```java
private Sink wrapSink(Sink terminalSink) {
    Sink sink = terminalSink;
    for (MyStreamV4<?> stage = this;
         stage.previousStage != null;
         stage = stage.previousStage) {
        sink = stage.opWrapSink.apply(sink);
    }
    return sink;
}
```

**마지막 단계부터 시작해 앞으로 거슬러 올라가며** 저장해 둔 `opWrapSink`을 하나씩 적용한다. 루프 조건이 `previousStage != null`인 이유는 Head 단계에는 `opWrapSink`이 없기 때문이다.

> **여기가 제일 헷갈리는 지점이다.**
> 조립 순서는 `map -> filter` (역방향), 데이터가 흐르는 순서는 `filter -> map` (정방향).
> 안쪽부터 감싸 나가므로, 마지막에 감싼 것이 가장 바깥이 되어 제일 먼저 실행된다.

### 4.5 `forEach()` — 유일한 루프

```java
for (Object element : sourceStage().source) {
    chainedSink.accept(element);
}
```

연산을 몇 개를 걸든 **실제 루프는 이 하나뿐이다.** 원소 하나가 파이프라인 전체를 관통한 뒤 다음 원소로 넘어간다.

---

## 5. 동작 흐름 추적

### Step 1 — 중간 연산 등록 (아직 계산 없음)

```java
MyStreamV4<String> stream = MyStreamV4.of(List.of(1, 2, 3, 4, 5))
        .filter(i -> i % 2 == 0)
        .map(i -> "[" + i * 10 + "]");
```

이 시점에 메모리에 만들어진 것은 다음 구조뿐이다.

```
   [Head]                [filter 단계]              [map 단계]
   ┌──────────────┐      ┌──────────────────┐      ┌──────────────────┐
   │ previous=null│ <──  │ previous=Head    │ <──  │ previous=filter  │
   │ source=[1..5]│      │ source=null      │      │ source=null      │
   │ opWrap=null  │      │ opWrap=λ_filter  │      │ opWrap=λ_map     │
   └──────────────┘      └──────────────────┘      └──────────────────┘
                                                            ↑
                                                     stream 변수가 참조
```

`predicate`도 `mapper`도 **단 한 번도 실행되지 않았다.**

### Step 2 — 최종 연산 호출, Sink 역방향 조립

```java
stream.forEach(System.out::println);
```

`wrapSink(terminalSink)` 내부 루프의 진행 상황:

| 회차 | 현재 stage | 적용 | 결과 `sink` |
|------|-----------|------|-------------|
| 시작 | — | — | `forEachSink` |
| 1회차 | map 단계 | `λ_map.apply(forEachSink)` | `mapSink { downstream = forEachSink }` |
| 2회차 | filter 단계 | `λ_filter.apply(mapSink)` | `filterSink { downstream = mapSink }` |
| 종료 | Head (previous == null) | — | `filterSink` 반환 |

최종 조립 결과를 중첩으로 표현하면 이렇다.

```
filterSink {
    downstream = mapSink {
        downstream = forEachSink
    }
}
```

### Step 3 — 단 한 번의 순회

```
element 1 → filterSink.accept(1)
              └ 1 % 2 != 0 → 버림 (mapSink 도달 안 함)

element 2 → filterSink.accept(2)
              └ 통과 → mapSink.accept(2)
                         └ "[20]" → forEachSink.accept("[20]")
                                      └ 출력: [20]

element 3 → filterSink.accept(3)  → 버림
element 4 → filterSink.accept(4)  → mapSink → forEachSink → 출력: [40]
element 5 → filterSink.accept(5)  → 버림
```

**원소 하나가 파이프라인 끝까지 갔다가 돌아와서, 그다음 원소를 처리한다.** 이것이 loop fusion이다.

---

## 6. 실행 예제와 출력

### 예제 코드

```java
package section05.lambda.lambda5.mystream;

import java.util.List;

public class MyStreamV4Main {
    public static void main(String[] args) {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5);

        System.out.println("--- 중간 연산 등록 (아직 실행 안 됨) ---");
        MyStreamV4<String> stream = MyStreamV4.of(numbers)
                .filter(i -> i % 2 == 0)
                .map(i -> "[" + i * 10 + "]");
        System.out.println("등록 완료. 출력된 게 하나도 없죠?\n");

        stream.forEach(System.out::println);
    }
}
```

### 출력

```
--- 중간 연산 등록 (아직 실행 안 됨) ---
등록 완료. 출력된 게 하나도 없죠?

=== 실행 시작 ===
  filter 검사: 1
  filter 검사: 2
  map 변환: 2 -> [20]
[20]
  filter 검사: 3
  filter 검사: 4
  map 변환: 4 -> [40]
[40]
  filter 검사: 5
```

### 출력이 증명하는 것

1. **지연 평가** — 중간 연산 등록 시점에 로그가 하나도 안 찍혔다
2. **loop fusion** — `filter 1~5` 후 `map 2, 4`가 아니라, 원소별로 번갈아 찍혔다
3. **불필요한 연산 제거** — `map`은 filter를 통과한 2번만 실행됐다
4. **중간 리스트 없음** — 어디에도 임시 `ArrayList`가 생기지 않았다

---

## 7. V3 vs V4 비교

`List.of(1,2,3,4,5)`에 `filter(짝수)` + `map(×10)`을 적용한 경우:

| 항목 | V3 (즉시 평가) | V4 (지연 평가) |
|------|---------------|---------------|
| 중간 `ArrayList` 생성 | 2개 (`[2,4]`, `[20,40]`) | **0개** |
| 소스 순회 횟수 | 2번 (filter용 + map용) | **1번** |
| `predicate.test()` 호출 | 5회 | 5회 |
| `mapper.apply()` 호출 | 2회 | 2회 |
| 계산 시작 시점 | `filter()` 호출 즉시 | `forEach()` 호출 시 |
| 연산 N개 연결 시 리스트 | N개 | **0개** |
| 연산 N개 연결 시 루프 | N번 | **1번** |

> 연산 개수가 늘어날수록 격차가 선형으로 벌어진다.
> `filter().map().filter().map()` 이면 V3는 리스트 4개 + 루프 4번, V4는 리스트 0개 + 루프 1번.

---

## 8. JDK Stream과의 대응 관계

MyStreamV4는 `java.util.stream`의 축소판이다. 이름까지 거의 일치한다.

| MyStreamV4 | JDK `java.util.stream` |
|------------|------------------------|
| `interface Sink<T>` | `java.util.stream.Sink<T>` (이름 동일) |
| `previousStage` 필드 | `AbstractPipeline.previousStage` (이름 동일) |
| `opWrapSink` 필드 | `AbstractPipeline.opWrapSink()` 추상 메서드 |
| `wrapSink()` | `AbstractPipeline.wrapSink()` (이름 동일) |
| `sourceStage()` | `AbstractPipeline.sourceStage` 필드 |
| `source` (`List<T>`) | `Spliterator<T>` |
| `forEach()`의 for 루프 | `AbstractPipeline.copyInto()` |
| `MyStreamV4.of()` | `ReferencePipeline.Head` 생성 |
| `filter()`가 반환하는 객체 | `ReferencePipeline.StatelessOp` |

### JDK의 실제 `filter` 구현

```java
public final Stream<P_OUT> filter(Predicate<? super P_OUT> predicate) {
    Objects.requireNonNull(predicate);
    return new StatelessOp<P_OUT, P_OUT>(this, StreamShape.REFERENCE,
                                         StreamOpFlag.NOT_SIZED) {
        @Override
        Sink<P_OUT> opWrapSink(int flags, Sink<P_OUT> downstream) {
            return new Sink.ChainedReference<P_OUT, P_OUT>(downstream) {
                @Override public void begin(long size) {
                    downstream.begin(-1);
                }
                @Override public void accept(P_OUT u) {
                    if (predicate.test(u))
                        downstream.accept(u);   // ← MyStreamV4와 동일한 구조
                }
            };
        }
    };
}
```

`opWrapSink`을 **필드(람다)로 저장**하느냐 **메서드 오버라이드**로 두느냐의 차이일 뿐, 발상은 완전히 같다.

### JDK가 추가로 가진 것

| 요소 | 목적 |
|------|------|
| `Spliterator` | `List` 대신 사용. 배열·파일·무한 생성기 등 모든 소스 지원 + `trySplit()`으로 병렬 분할 |
| `Sink.begin(long size)` | 크기 힌트 전달. `toArray()`가 배열을 한 번에 정확히 할당 |
| `Sink.end()` | `sorted()` 같은 stateful 연산이 "이제 다 받았다"를 아는 시점 |
| `cancellationRequested()` | `limit`, `findFirst`, `anyMatch`의 조기 종료 |
| `StreamOpFlag` | `SIZED / SORTED / DISTINCT / ORDERED / SHORT_CIRCUIT` 비트마스크 최적화 |
| `linkedOrConsumed` | 스트림 1회용 보장 |
| `AbstractTask` | `ForkJoinPool` 기반 병렬 실행 |

---

## 9. 확장 과제

### 9.1 스트림 1회용 만들기

JDK 스트림을 두 번 쓰면 `IllegalStateException`이 난다. 필드 하나로 구현할 수 있다.

```java
private boolean linkedOrConsumed;

private void checkAndMark() {
    if (linkedOrConsumed) {
        throw new IllegalStateException(
            "stream has already been operated upon or closed");
    }
    linkedOrConsumed = true;
}
```

`filter`, `map`, `forEach` 진입부에서 `checkAndMark()`를 호출하면 된다.

### 9.2 조기 종료 (`getFirst`, `limit`)

`Sink`에 취소 신호를 추가한다.

```java
interface Sink<T> {
    void accept(T element);
    default boolean cancellationRequested() { return false; }
}
```

`forEach`의 루프를 다음과 같이 바꾼다.

```java
for (Object element : sourceStage().source) {
    if (chainedSink.cancellationRequested()) break;
    chainedSink.accept(element);
}
```

각 중간 단계는 자기 `downstream`의 취소 요청을 그대로 위로 전파하면 된다.

### 9.3 stateful 연산의 함정 — `sorted()`, `distinct()`

이 연산들은 **loop fusion이 깨진다.** 정렬하려면 모든 원소를 다 받아야 하기 때문이다.

```
begin()  → 내부 버퍼 생성
accept() → downstream으로 안 넘기고 버퍼에 쌓기만 함
end()    → 버퍼 정렬 후, 그제서야 downstream으로 순차 방출
```

즉 **여기서는 중간 배열이 실제로 생긴다.** JDK가 `Sink`에 `begin`/`end`를 둔 핵심 이유가 이것이다. 현재 MyStreamV4의 `Sink`에는 `accept`만 있어서 stateful 연산을 구현할 수 없다.

### 9.4 소스 추상화

`List<T> source`를 `Iterator<T>` 또는 자체 `MySpliterator<T>`로 바꾸면 배열, 무한 생성기(`Stream.iterate` 같은) 등 다양한 소스를 지원할 수 있다.

---

## 10. 정리

- `List`는 **데이터를 담는 자료구조**, `Stream`은 **데이터를 어떻게 처리할지 적어둔 실행 계획**이다.
- 중간 연산은 계산하지 않는다. `previousStage`로 단계를 연결하고 `opWrapSink`이라는 **"통로 만드는 방법"만 저장**한다.
- 최종 연산 시 `wrapSink()`이 **뒤에서 앞으로 거슬러 올라가며** Sink를 감싸 체인을 완성한다. **조립은 역방향, 데이터 흐름은 정방향.**
- 완성된 체인에 소스를 **단 한 번** 밀어넣으면, 원소 하나가 파이프라인 전체를 관통한다 → **loop fusion**.
- 그 결과 **중간 컬렉션 0개, 순회 1번, 지연 평가, 불필요한 연산 제거**가 자연히 따라온다.
- JDK `Stream`도 정확히 같은 구조다. 여기에 `Spliterator`(소스 추상화 + 병렬), `begin`/`end`(stateful 연산), `cancellationRequested`(조기 종료), `StreamOpFlag`(최적화)가 더해진 것뿐이다.
