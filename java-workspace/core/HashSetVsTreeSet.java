package core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class HashSetVsTreeSet {
    public void demonstrteOrdering(){
        System.out.println("Start ordering demonstration");
        Set<Integer> hashSet = new HashSet<>();
        hashSet.add(5);
        hashSet.add(11);
        hashSet.add(3);
        hashSet.add(2);
        hashSet.add(4);

        for(Integer num : hashSet){
            System.out.println("HashSet element: " + num);
        }
        System.out.println("Now demonstrating TreeSet ordering");
        Set<Integer> treeSet = new TreeSet<>();
        treeSet.add(5);
        treeSet.add(1);
        treeSet.add(30);
        treeSet.add(2);
        treeSet.add(4);
        for(Integer num : treeSet){
            System.out.println("TreeSet element: " + num);
        }
    }

     public void demonstrateTreeSetOperations() {
        System.out.println("\n=== TreeSet Special Operations ===");
        
        TreeSet<Integer> treeSet = new TreeSet<>();
        treeSet.addAll(Arrays.asList(10, 20, 30, 40, 50, 60, 70, 80, 90));
        
        System.out.println("First element: " + treeSet.first());
        System.out.println("Last element: " + treeSet.last());
        System.out.println("Lower than 50: " + treeSet.lower(50));
        System.out.println("Higher than 50: " + treeSet.higher(50));
        System.out.println("Floor of 55: " + treeSet.floor(55));
        System.out.println("Ceiling of 55: " + treeSet.ceiling(55));
        System.out.println("Subset [30, 70): " + treeSet.subSet(30, 70));
        System.out.println("HeadSet < 50: " + treeSet.headSet(50));
        System.out.println("TailSet >= 50: " + treeSet.tailSet(50));
    }
}
