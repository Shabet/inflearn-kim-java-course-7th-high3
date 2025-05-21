package section07.methodref;

import java.util.function.BiFunction;

// 매개변수 추가
public class MethodRefEx6 {

    public static void main(String[] args) {
        // 4. 임의 객체의 인스턴스 메서드 참조(특정 타입의)
        Person person = new Person("Kim");

        // 람다
        BiFunction<Person, Integer, String> func1 = (Person p, Integer n) -> p.introduceWithNumber(n);

        System.out.println("person.upperName = " + func1.apply(person, 10));

        // 메서드 참조, 타입이 첫 번째 매개변수가 됨, 그리고 첫번째 매개변수의 메서드를 호출
        // 나머지는 순서대로 매개변수에 전달
        BiFunction<Person, Integer, String> func2 = Person::introduceWithNumber; //타입::인스턴스메서드
        System.out.println("person.upperName = " + func2.apply(person, 10));
    }
}
