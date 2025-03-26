package section03.lambda.ex2;

public class BuildGreeterExample {

    // 고차 함수, greeting 문자열을 받아 "새로운 함수"를 반환
    public static StringFunction buildGreeter(String greeting) {
        return name -> greeting + ", " + name;
    }

    public static void main(String[] args) {

        StringFunction helloGreeter = buildGreeter("Hello");
        StringFunction hiGreeter = buildGreeter("Hi");

        // 함수가 반환되었으므로, apply()를 호출해 실제로 사용
        System.out.println(helloGreeter.apply("Java"));
        System.out.println(hiGreeter.apply("Lambda"));
    }
}
