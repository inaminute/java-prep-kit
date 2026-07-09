package coderdesk;
import java.util.*;

public class LevelOne {
    public static void main(String[] args) {
        
        reverseString();
        isPalindrome("madam");
        findLargestAndSmallest(new int[]{34, -2, 45, 0, 11, -9, 78});
        frequencyOfCharacters("hello world");
        removeDuplicates(new int[]{1, 2, 2, 3, 4, 4, 5});
        studentGradeExp();
    }

    private static void reverseString() {
        String str = "Hello, World!";
        StringBuilder reversed = new StringBuilder();
        for (int i = str.length() - 1; i >= 0; i--) {
            reversed.append(str.charAt(i));
        }
        System.out.println("Reversed String: " + reversed.toString());
    }

    private static void isPalindrome(String str) {
        // StringBuilder reversed = new StringBuilder();
        // for (int i = str.length() - 1; i >= 0; i--) {
        //     reversed.append(str.charAt(i));
        // }
        // if (str.equals(reversed.toString())) {
        //     System.out.println(str + " is a palindrome.");
        // } else {
        //     System.out.println(str + " is not a palindrome.");
        // }

        int left = 0;
        int right = str.length() - 1;
        boolean isPalindrome = true;
        while (left < right) {
            if (str.charAt(left) != str.charAt(right)) {
                isPalindrome = false;
                break;
            }
            left++;
            right--;
        }
    }

    private static void findLargestAndSmallest(int[] numbers) {
        if (numbers == null || numbers.length == 0) {
            System.out.println("Array is empty.");
            return;
        }

        // int largest = numbers[0];
        // int smallest = numbers[0];
        // for (int num : numbers) {
        //     if (num > largest) {
        //         largest = num;
        //     }
        //     if (num < smallest) {
        //         smallest = num;
        //     }
        // }
        // System.out.println("Largest number: " + largest);
        // System.out.println("Smallest number: " + smallest);
        
        // int largest = Integer.MIN_VALUE;
        // int smallest = Integer.MAX_VALUE;
        // for (int num : numbers) {
        //     if (num > largest) {
        //         largest = num;
        //     }
        //     if (num < smallest) {
        //         smallest = num;
        //     }
        // }

        // int largest = numbers[0];
        // int smallest = numbers[0];
        // for (int i = 1; i < numbers.length; i++) {
        //     if (numbers[i] > largest) {
        //         largest = numbers[i];
        //     }
        //     if (numbers[i] < smallest) {
        //         smallest = numbers[i];
        //     }
        // }

    }

    private static void frequencyOfCharacters(String str) {
        // int[] freq = new int[256]; // Assuming ASCII character set
        // for (int i = 0; i < str.length(); i++) {
        //     freq[str.charAt(i)]++;
        // }

        // System.out.println("Character Frequencies:");
        // for (int i = 0; i < freq.length; i++) {
        //     if (freq[i] > 0) {
        //         System.out.println((char) i + ": " + freq[i]);
        //     }
        // }

        Map<Character, Integer> frequencyMap = new HashMap<>();
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            frequencyMap.put(ch, frequencyMap.getOrDefault(ch, 0) + 1);
        }   
        System.out.println("Character Frequencies:");
        for (Map.Entry<Character, Integer> entry : frequencyMap.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    private static void removeDuplicates(int[] numbers) {
        // Set<Integer> uniqueNumbers = new LinkedHashSet<>();
        // for (int num : numbers) {
        //     uniqueNumbers.add(num);
        // }

        // System.out.println("Array after removing duplicates: " + uniqueNumbers);
        List<Integer> uniqueList = new ArrayList<>();
        for (int num : numbers) {
            if (!uniqueList.contains(num)) {
                uniqueList.add(num);
            }
        }
        System.out.println("Array after removing duplicates: " + uniqueList);
    }

    private static void studentGradeExp() {
        Student student1 = new Student(1, "Alice", 85);
        Student student2 = new Student(2, "Bob", 92);
        Student student3 = new Student(3, "Charlie", 78);

        List<Student> students = new ArrayList<>(Arrays.asList(student1, student2, student3));
        for(Student student: students){
            System.out.println(student.calculateGrade());
        }
    }
}

// Create a Student class with:

// id

// name

// marks

// Write a method to:

// Calculate grade based on marks

// Print student details
// Find the student with the highest marks
class Student {
    private int id;
    public String name;
    private int marks;

    public Student(int id, String name, int marks) {
        this.id = id;
        this.name = name;
        this.marks = marks;
    }

    public char calculateGrade() {
        if (marks >= 90) return 'A';
        else if (marks >= 80) return 'B';
        else if (marks >= 70) return 'C';
        else if (marks >= 60) return 'D';
        else return 'F';
    }
}