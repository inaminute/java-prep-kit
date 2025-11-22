# Annotation Processing

## Problem Statement

Explain compile-time annotation processing in Java. Demonstrate how to create an annotation processor that generates code during compilation. Show the difference between runtime and compile-time annotation processing.

**Requirements**:
- Explain annotation processing API
- Show how processors work at compile-time
- Demonstrate code generation
- Compare with runtime reflection

## Approach

- Annotation processors run during compilation
- Implement AbstractProcessor to create custom processor
- Use ProcessingEnvironment to access compiler utilities
- Generate source files using JavaFileObject
- More efficient than runtime reflection

## Solution

```java
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.JavaFileObject;
import java.io.Writer;
import java.lang.annotation.*;
import java.util.Set;

// Custom annotation for builder pattern
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@interface GenerateBuilder {
}

// Annotation processor (runs at compile-time)
@SupportedAnnotationTypes("GenerateBuilder")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class BuilderProcessor extends AbstractProcessor {
    
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(GenerateBuilder.class)) {
            if (element.getKind() == ElementKind.CLASS) {
                TypeElement typeElement = (TypeElement) element;
                try {
                    generateBuilderClass(typeElement);
                } catch (Exception e) {
                    processingEnv.getMessager().printMessage(
                        javax.tools.Diagnostic.Kind.ERROR,
                        "Error generating builder: " + e.getMessage()
                    );
                }
            }
        }
        return true;
    }
    
    private void generateBuilderClass(TypeElement typeElement) throws Exception {
        String className = typeElement.getSimpleName().toString();
        String builderClassName = className + "Builder";
        String packageName = processingEnv.getElementUtils()
            .getPackageOf(typeElement).toString();
        
        JavaFileObject builderFile = processingEnv.getFiler()
            .createSourceFile(packageName + "." + builderClassName);
        
        try (Writer writer = builderFile.openWriter()) {
            writer.write("package " + packageName + ";\n\n");
            writer.write("public class " + builderClassName + " {\n");
            
            // Generate fields and methods
            for (Element enclosed : typeElement.getEnclosedElements()) {
                if (enclosed.getKind() == ElementKind.FIELD) {
                    String fieldName = enclosed.getSimpleName().toString();
                    String fieldType = enclosed.asType().toString();
                    
                    writer.write("    private " + fieldType + " " + fieldName + ";\n");
                    writer.write("    public " + builderClassName + " " + fieldName + 
                        "(" + fieldType + " " + fieldName + ") {\n");
                    writer.write("        this." + fieldName + " = " + fieldName + ";\n");
                    writer.write("        return this;\n");
                    writer.write("    }\n");
                }
            }
            
            writer.write("    public " + className + " build() {\n");
            writer.write("        return new " + className + "();\n");
            writer.write("    }\n");
            writer.write("}\n");
        }
    }
}

// Example usage
@GenerateBuilder
class Person {
    private String name;
    private int age;
    
    public Person() {}
}

public class AnnotationProcessing {
    
    public static void main(String[] args) {
        System.out.println("=== Annotation Processing ===");
        System.out.println("Annotation processors run at compile-time");
        System.out.println("They generate code before runtime");
        
        // At runtime, we can use the generated builder
        // PersonBuilder builder = new PersonBuilder()
        //     .name("Alice")
        //     .age(30)
        //     .build();
    }
}
```

## Complexity Analysis

**Time Complexity**: O(n) where n is number of annotated elements

**Space Complexity**: O(1) for processor, O(m) for generated code

## Edge Cases and Pitfalls

- **Multiple rounds**: Processor may run multiple rounds
- **Error handling**: Use Messager to report errors
- **Generated code**: Must be valid Java code

## Interview-Ready Answer

"Annotation processing happens at compile-time using AbstractProcessor. Processors analyze annotated elements and can generate source files using Filer. More efficient than runtime reflection as code generation happens during compilation. Common uses include builder pattern generation, dependency injection frameworks like Dagger, and ORM tools. Use @SupportedAnnotationTypes to specify which annotations to process."
