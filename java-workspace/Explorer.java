
import core.HashSetVsTreeSet;
import core.IteratorUsage;
import core.OptionalClass;
import core.ParallelStreams;
import core.StreamOperations;
import oops.BuilderPattern;
import oops.InheritanceDemo;
import threads.JoinExps;
import threads.ThreadDemo;

public class Explorer {
    public static void main(String[] args) {
        // executeHasHashVsTreeSetDemo();
        IteratorUsage iteratorDemo = new IteratorUsage();
        // iteratorDemo.initialize();

        StreamOperations streamDemo = new StreamOperations();
        // streamDemo.initialize();

        OptionalClass optionalDemo = new OptionalClass();
        // optionalDemo.initialize();

        ParallelStreams parallelStreamsDemo = new ParallelStreams();
        // parallelStreamsDemo.initialize();

        // OOPS
        BuilderPattern builderPatternDemo = new BuilderPattern();
        // builderPatternDemo.initialize();

        // InheritanceDemo.main();

        ThreadDemo threadDemo = new ThreadDemo();
        // threadDemo.initializeDemo();

        JoinExps.main(new String[]{});
    }

    private static void executeHasHashVsTreeSetDemo() {
        HashSetVsTreeSet demo = new HashSetVsTreeSet();
        demo.demonstrteOrdering();
        demo.demonstrateTreeSetOperations();
    }
}
