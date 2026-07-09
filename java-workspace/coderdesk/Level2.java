package coderdesk;

import java.util.*;

public class Level2 {
    public static void main(String[] args) {
        secondLargest();
        // Check if Two Strings are Anagrams
        areAnagrams("listen", "silent");

        //Reverse a integer
        reverseInteger(123);

        findMissingNumber(new int[]{1, 2, 4, 6, 3, 7, 8});
        vowelConsonantCount();
        mergeSortedArrays();
        findFirstNonRepeating("swiss");
        isSorted(new int[]{1, 2, 3, 5, 4, 6});
        containsDuplicates();
    }

    private static void secondLargest() {
        int largest = Integer.MIN_VALUE;
        int second = Integer.MIN_VALUE;

        int[] numbers = { 5, 3, 8, 8, 2, 1 };
        for (int n : numbers) {
            if (n > largest) {
                second = largest;
                largest = n;
            } else if (n > second && n != largest) {
                second = n;
            }
        }
        System.out.println("Second Largest: " + second);
    }

    private static void areAnagrams(String str1, String str2) {
        if (str1.length() != str2.length()) {
            System.out.println(str1 + " and " + str2 + " are not anagrams.");
            return;
        }

        // int[] charCount = new int[256]; // Assuming ASCII character set

        // for (int i = 0; i < str1.length(); i++) {
        // charCount[str1.charAt(i)]++;
        // charCount[str2.charAt(i)]--;
        // }

        // for (int count : charCount) {
        // if (count != 0) {
        // System.out.println(str1 + " and " + str2 + " are not anagrams.");
        // return;
        // }
        // }

        // System.out.println(str1 + " and " + str2 + " are anagrams.");

        Map<Character, Integer> map = new HashMap<>();

        for (char c : str1.toCharArray())
            map.put(c, map.getOrDefault(c, 0) + 1);

        for (char c : str2.toCharArray()) {
            map.put(c, map.getOrDefault(c, 0) - 1);
            if (map.get(c) < 0)
                return;
        }

        System.out.println(str1 + " and " + str2 + " are anagrams.");
    }

    private static void reverseInteger(int num) {
        int reversed = 0;
        while (num != 0) {
            int digit = num % 10;
            reversed = reversed * 10 + digit;
            num /= 10;
        }
        System.out.println("Reversed Integer: " + reversed);
    }

    private static void findMissingNumber(int[] numbers) {
        int n = numbers.length + 1; // Since one number is missing
        int expectedSum = n * (n + 1) / 2;
        int actualSum = 0;
        for (int num : numbers) {
            actualSum += num;
        }
        int missingNumber = expectedSum - actualSum;
        System.out.println("Missing Number: " + missingNumber);
    }

    private static void vowelConsonantCount () {
        String s = "Hello World";
        int vowels = 0, consonants = 0;

        for (char c : s.toLowerCase().toCharArray()) {
            if (Character.isLetter(c)) {
                if ("aeiou".indexOf(c) >= 0) vowels++;
                else consonants++;
            }
        }

        System.out.println("Vowels: " + vowels);
        System.out.println("Consonants: " + consonants);
    }

    private static void mergeSortedArrays() {
        int[] a = {1,2,3,4, 5};
        int[] b = {8,9,10};
        int[] r = new int[a.length+b.length]; //Initialize result array with size of both array size.
        
        int i = 0, j = 0, k = 0;
        
        while(i<a.length && j < b.length){
            r[k++] = a[i] < b[j] ? a[i++] : b[j++];
        }
        
        while(i<a.length)r[k++] = a[i++];
        while(j<b.length)r[k++] = b[j++];
        
        System.out.println(Arrays.toString(r));
	}

    private static char findFirstNonRepeating(String s) {
        Map<Character, Integer> map = new LinkedHashMap<>();

        for (char c : s.toCharArray())
            map.put(c, map.getOrDefault(c, 0) + 1);

        for (Map.Entry<Character, Integer> entry : map.entrySet()) {
            if (entry.getValue() == 1)
                return entry.getKey();
        }
        return '\0';
    }

    private static void isSorted(int[] arr) {
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] < arr[i - 1]){
                System.out.println(arr[i-1] +" is not less than "+ arr[i]);
		        break;
            }
        }
        return;
    }

    private static void containsDuplicates() {
		int[] nums = {1,2,3,4,6,5,3,4};
		
		Set<Integer> seen = new HashSet<>();
		Set<Integer> duplicates = new HashSet<>();
		
		for(int n: nums){
		    if(seen.contains(n)){
		        duplicates.add(n);
		    }else{
		        seen.add(n);
		    }
		}
		
		System.out.println(duplicates);
		
	}
}
