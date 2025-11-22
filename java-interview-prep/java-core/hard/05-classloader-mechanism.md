# ClassLoader Mechanism

## Problem Statement

Explain the Java ClassLoader mechanism. Demonstrate how classes are loaded, the delegation model, and how to create custom ClassLoaders. Show practical use cases for custom class loading.

**Requirements**:
- Explain ClassLoader hierarchy
- Demonstrate parent delegation model
- Create custom ClassLoader
- Show class loading process

## Approach

- ClassLoaders load classes into JVM
- Three built-in loaders: Bootstrap, Extension, Application
- Parent delegation: child asks parent before loading
- Custom ClassLoaders for dynamic loading, isolation, hot-swapping
- loadClass() delegates, findClass() does actual loading

## Solution

```java
import java.io.*;

public class ClassLoaderMechanism {
    
    // Custom ClassLoader
    static class CustomClassLoader extends ClassLoader {
        private String classPath;
        
        public CustomClassLoader(String classPath) {
            this.classPath = classPath;
        }
        
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            try {
                byte[] classData = loadClassData(name);
                return defineClass(name, classData, 0, classData.length);
            } catch (IOException e) {
                throw new ClassNotFoundException("Class not found: " + name, e);
            }
        }
        
        private byte[] loadClassData(String className) throws IOException {
            String fileName = classPath + File.separator + 
                className.replace('.', File.separatorChar) + ".class";
            
            try (FileInputStream fis = new FileInputStream(fileName);
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                return baos.toByteArray();
            }
        }
    }
    
    public static void main(String[] args) throws Exception {
        demonstrateClassLoaderHierarchy();
        demonstrateParentDelegation();
        demonstrateCustomClassLoader();
    }
    
    public static void demonstrateClassLoaderHierarchy() {
        System.out.println("=== ClassLoader Hierarchy ===");
        
        ClassLoader appLoader = ClassLoaderMechanism.class.getClassLoader();
        System.out.println("Application ClassLoader: " + appLoader);
        
        ClassLoader extLoader = appLoader.getParent();
        System.out.println("Extension ClassLoader: " + extLoader);
        
        ClassLoader bootstrapLoader = extLoader.getParent();
        System.out.println("Bootstrap ClassLoader: " + bootstrapLoader); // null
        
        // Bootstrap loader is native, represented as null
    }
    
    public static void demonstrateParentDelegation() {
        System.out.println("\n=== Parent Delegation Model ===");
        
        // String is loaded by Bootstrap ClassLoader
        System.out.println("String ClassLoader: " + 
            String.class.getClassLoader()); // null (Bootstrap)
        
        // Custom class loaded by Application ClassLoader
        System.out.println("Custom class ClassLoader: " + 
            ClassLoaderMechanism.class.getClassLoader());
    }
    
    public static void demonstrateCustomClassLoader() {
        System.out.println("\n=== Custom ClassLoader ===");
        
        try {
            CustomClassLoader customLoader = new CustomClassLoader("./classes");
            
            // Load class using custom loader
            // Class<?> clazz = customLoader.loadClass("com.example.MyClass");
            // Object instance = clazz.getDeclaredConstructor().newInstance();
            
            System.out.println("Custom ClassLoader created");
            System.out.println("Can load classes from: ./classes");
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    // Demonstrate class loading process
    public static void demonstrateLoadingProcess() {
        System.out.println("\n=== Class Loading Process ===");
        System.out.println("1. Loading: Read .class file and create Class object");
        System.out.println("2. Linking:");
        System.out.println("   - Verification: Verify bytecode");
        System.out.println("   - Preparation: Allocate memory for static fields");
        System.out.println("   - Resolution: Resolve symbolic references");
        System.out.println("3. Initialization: Execute static initializers");
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) for class lookup in loaded classes, O(n) for loading new class

**Space Complexity**: O(n) where n is size of class bytecode

## Edge Cases and Pitfalls

- **Class identity**: Same class loaded by different loaders are different types
- **Memory leaks**: ClassLoaders holding references prevent garbage collection
- **Security**: Custom loaders must handle security properly

## Interview-Ready Answer

"ClassLoader loads classes into JVM using parent delegation model. Three built-in loaders: Bootstrap (core Java classes), Extension (ext directory), Application (classpath). Child loaders delegate to parent before loading. Custom ClassLoaders enable dynamic loading, class isolation, and hot-swapping. Override findClass() for custom loading logic. Class identity depends on both class name and ClassLoader."
