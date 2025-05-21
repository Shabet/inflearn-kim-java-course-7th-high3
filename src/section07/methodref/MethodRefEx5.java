package section07.methodref;

import section05.lambda.lambda5.mystream.MySteamV3;

import java.util.List;

public class MethodRefEx5 {

    public static void main(String[] args) {
        List<Person> personList = List.of(new Person("Kim"),
                new Person("Park"),
                new Person("Lee")
        );

        List<String> result1 = MySteamV3.of(personList)
                .map(person -> person.introduce())
                .map(str -> str.toUpperCase())
                .toList();
        System.out.println("result1 = " + result1);

        List<String> result2 = MySteamV3.of(personList)
                .map(Person::introduce)
                .map(String::toUpperCase)
                .toList();
        System.out.println("result2 = " + result2);

    }
}
