package core;

import java.util.*;

public class IteratorUsage {
    public void initialize() {
        demonstrateIterator();
        demonstrateSafeRemoval();
    }

    public void demonstrateIterator() {
        System.out.println("=== Iterator Demonstration ===");
        List<String> fruitsList = new ArrayList<>(Arrays.asList("Apple", "Banana", "Cherry", "Date", "Elderberry"));
        Iterator<String> iterator = fruitsList.iterator();
        while (iterator.hasNext()) {
            String fruit = iterator.next();
            System.out.println("Fruit: " + fruit);
        }

        System.out.println("\n=== For Exclusive loop ===");
        for (String fruit : fruitsList) {
            System.out.println("Fruit: " + fruit);
        }
    }

    public void demonstrateSafeRemoval(){
        System.out.println("\n=== Safe Removal Demonstration ===");
        List<Integer> numsList = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        Iterator<Integer> numIterator = numsList.iterator();

        System.out.println("Original List: " + numsList);
        while(numIterator.hasNext()){
            Integer num = numIterator.next();
            if(num % 2==0){
                numIterator.remove();
            }
        }

        System.out.println("After removing even numbers: " + numsList);

    }
}
