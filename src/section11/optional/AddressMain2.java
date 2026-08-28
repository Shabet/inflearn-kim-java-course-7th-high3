package section11.optional;

import section11.optional.model.Address;
import section11.optional.model.User;

import java.util.Optional;

public class AddressMain2 {

    public static void main(String[] args) {
        User user1 = new User("user1", null);
        User user2 = new User("user2", new Address("hello street"));

        printStreet(user1);
        printStreet(user2);
    }

    private static void printStreet(User user) {
        Optional.ofNullable(user)
                .map(User::getAddress)
                .map(Address::getStreet)
                .ifPresentOrElse(
                        System.out::println,
                        () -> System.out.println("Unknown")
                );
    }

//    private static void printStreet(User user) {
//        getUserStreet(user).ifPresentOrElse(
//                System.out::println,
//                () -> System.out.println("Unknown")
//        );
//    }
//
//    private static Optional<String> getUserStreet(User user) {
//        return Optional.ofNullable(user)
//                .map(User::getAddress)
//                .map(Address::getStreet);
//    }
}
