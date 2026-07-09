package coderdesk;

import java.util.*;
import java.util.stream.Collectors;
public class Level3 {
    public static void main(String[] args) {
        listToMap();
        gcd(24, 36);
    }

    private static void listToMap() {
        List<String> fruits = List.of("apple", "banana", "orange", "kiwi");

        Map<String, Integer> map = fruits.stream()
                .collect(Collectors.toMap(
                        s -> s,
                        String::length
                ));

        System.out.println(map);
    }

    private static int gcd(int a, int b) {
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    private static void printLCM(int a, int b) {
        int gcdValue = gcd(a, b);
        int lcm = (a * b) / gcdValue;
        System.out.println("LCM of " + a + " and " + b + " is: " + lcm);
    }

    public static void allSubstringsOfString(String[] args) {
        String input = "abc";

        for (int i = 0; i < input.length(); i++) {
            for (int j = i + 1; j <= input.length(); j++) {
                System.out.print(input.substring(i, j) + " ");
            }
        }
    }
}
