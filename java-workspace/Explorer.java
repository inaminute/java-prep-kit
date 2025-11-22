import core.HashSetVsTreeSet;
import core.IteratorUsage;

public class Explorer {
    public static void main(String[] args) {
        executeHasHashVsTreeSetDemo();
        IteratorUsage iteratorDemo = new IteratorUsage();
        iteratorDemo.initialize();
    }

    private static void executeHasHashVsTreeSetDemo() {
        HashSetVsTreeSet demo = new HashSetVsTreeSet();
        demo.demonstrteOrdering();
        demo.demonstrateTreeSetOperations();
    }
}
