# JVM Internals

## Problem Statement

Explain Java Virtual Machine internals including bytecode, JIT compilation, and runtime optimizations. Demonstrate how Java code is executed and optimized by the JVM.

**Requirements**:
- Explain JVM architecture
- Describe bytecode and class file format
- Explain JIT compilation
- Show runtime optimizations

## Approach

- JVM executes bytecode, not source code
- Class files contain bytecode and metadata
- Interpreter executes bytecode initially
- JIT compiler optimizes hot code paths
- HotSpot identifies and optimizes frequently executed code

## Solution

```java
public class JVMInternals {
    
    public static void main(String[] args) {
        explainJVMArchitecture();
        explainBytecode();
        explainJITCompilation();
        explainOptimizations();
    }
    
    public static void explainJVMArchitecture() {
        System.out.println("=== JVM Architecture ===");
        
        System.out.println("Class Loader Subsystem:");
        System.out.println("  - Loading: Read .class files");
        System.out.println("  - Linking: Verification, preparation, resolution");
        System.out.println("  - Initialization: Execute static initializers");
        
        System.out.println("\nRuntime Data Areas:");
        System.out.println("  - Method Area: Class metadata, static variables");
        System.out.println("  - Heap: Objects and arrays");
        System.out.println("  - Stack: Method frames per thread");
        System.out.println("  - PC Register: Current instruction per thread");
        System.out.println("  - Native Method Stack: Native method calls");
        
        System.out.println("\nExecution Engine:");
        System.out.println("  - Interpreter: Execute bytecode");
        System.out.println("  - JIT Compiler: Compile hot code to native");
        System.out.println("  - Garbage Collector: Reclaim memory");
    }
    
    public static void explainBytecode() {
        System.out.println("\n=== Bytecode ===");
        
        System.out.println("Java source -> javac -> bytecode (.class)");
        System.out.println("Bytecode is platform-independent");
        
        System.out.println("\nCommon bytecode instructions:");
        System.out.println("  - iload: Load int from local variable");
        System.out.println("  - istore: Store int to local variable");
        System.out.println("  - iadd: Add two ints");
        System.out.println("  - invokevirtual: Invoke instance method");
        System.out.println("  - invokestatic: Invoke static method");
        System.out.println("  - new: Create object");
        System.out.println("  - return: Return from method");
        
        // Example method to see bytecode
        // Use: javap -c JVMInternals.class
    }
    
    public static int add(int a, int b) {
        return a + b;
    }
    
    public static void explainJITCompilation() {
        System.out.println("\n=== JIT Compilation ===");
        
        System.out.println("Tiered Compilation:");
        System.out.println("  - Level 0: Interpreter");
        System.out.println("  - Level 1-3: C1 compiler (client)");
        System.out.println("  - Level 4: C2 compiler (server)");
        
        System.out.println("\nC1 Compiler:");
        System.out.println("  - Fast compilation");
        System.out.println("  - Basic optimizations");
        System.out.println("  - Quick startup");
        
        System.out.println("\nC2 Compiler:");
        System.out.println("  - Aggressive optimizations");
        System.out.println("  - Slower compilation");
        System.out.println("  - Better peak performance");
        
        System.out.println("\nCompilation Threshold:");
        System.out.println("  - Method invocation count");
        System.out.println("  - Back-edge count (loops)");
        System.out.println("  - -XX:CompileThreshold=10000 (default)");
    }
    
    public static void explainOptimizations() {
        System.out.println("\n=== Runtime Optimizations ===");
        
        System.out.println("Inlining:");
        System.out.println("  - Replace method call with method body");
        System.out.println("  - Reduces call overhead");
        
        System.out.println("\nEscape Analysis:");
        System.out.println("  - Determine if object escapes method");
        System.out.println("  - Allocate on stack if doesn't escape");
        System.out.println("  - Eliminate locks on non-escaping objects");
        
        System.out.println("\nLoop Optimizations:");
        System.out.println("  - Loop unrolling");
        System.out.println("  - Loop invariant code motion");
        
        System.out.println("\nDead Code Elimination:");
        System.out.println("  - Remove unreachable code");
        
        System.out.println("\nConstant Folding:");
        System.out.println("  - Evaluate constants at compile time");
        
        System.out.println("\nDevirtualization:");
        System.out.println("  - Convert virtual calls to direct calls");
        System.out.println("  - When receiver type is known");
    }
    
    // Demonstrate optimization
    public static void demonstrateOptimization() {
        // This loop will be optimized by JIT
        long sum = 0;
        for (int i = 0; i < 100000; i++) {
            sum += i;
        }
        System.out.println("Sum: " + sum);
    }
}
```

## Complexity Analysis

**Time Complexity**: Varies by optimization, generally improves with JIT

**Space Complexity**: O(1) for JIT metadata

## Edge Cases and Pitfalls

- **Warmup time**: JIT needs time to optimize
- **Deoptimization**: JIT may revert to interpreter if assumptions invalid
- **Code cache**: Limited size for compiled code

## Interview-Ready Answer

"JVM executes bytecode through interpreter initially, then JIT compiles hot code to native. Tiered compilation uses C1 (fast, basic optimizations) and C2 (slow, aggressive optimizations). Key optimizations: inlining, escape analysis, loop optimizations, dead code elimination. HotSpot identifies frequently executed code for compilation. Use -XX:+PrintCompilation to see JIT activity."
