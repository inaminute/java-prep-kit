# Composite Pattern

## Problem Statement

Implement the Composite design pattern to create a file system structure where both individual files and directories (containing files and subdirectories) can be treated uniformly. The pattern should allow building tree structures and performing operations recursively.

**Requirements:**
- Create a component interface for both files and directories
- Implement leaf nodes (files) and composite nodes (directories)
- Support adding/removing children in composites
- Implement operations that work recursively on the tree
- Demonstrate uniform treatment of individual and composite objects

## Approach

- Define a Component interface with common operations
- Create Leaf class for individual objects (files)
- Create Composite class that can contain other components
- Composite maintains a list of child components
- Operations on composite recursively call operations on children
- Client code treats leaves and composites uniformly

## Solution

```java
import java.util.ArrayList;
import java.util.List;

// Component interface
interface FileSystemComponent {
    void display(String indent);
    int getSize();
    String getName();
}

// Leaf - File
class File implements FileSystemComponent {
    private String name;
    private int size;
    
    public File(String name, int size) {
        this.name = name;
        this.size = size;
    }
    
    @Override
    public void display(String indent) {
        System.out.println(indent + "📄 " + name + " (" + size + " KB)");
    }
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getName() {
        return name;
    }
}

// Composite - Directory
class Directory implements FileSystemComponent {
    private String name;
    private List<FileSystemComponent> children;
    
    public Directory(String name) {
        this.name = name;
        this.children = new ArrayList<>();
    }
    
    public void add(FileSystemComponent component) {
        children.add(component);
    }
    
    public void remove(FileSystemComponent component) {
        children.remove(component);
    }
    
    public List<FileSystemComponent> getChildren() {
        return children;
    }
    
    @Override
    public void display(String indent) {
        System.out.println(indent + "📁 " + name + "/");
        for (FileSystemComponent child : children) {
            child.display(indent + "  ");
        }
    }
    
    @Override
    public int getSize() {
        int totalSize = 0;
        for (FileSystemComponent child : children) {
            totalSize += child.getSize();
        }
        return totalSize;
    }
    
    @Override
    public String getName() {
        return name;
    }
}

// Another example: Organization hierarchy

interface Employee {
    void showDetails(String indent);
    double getSalary();
    String getName();
}

class Developer implements Employee {
    private String name;
    private double salary;
    private String technology;
    
    public Developer(String name, double salary, String technology) {
        this.name = name;
        this.salary = salary;
        this.technology = technology;
    }
    
    @Override
    public void showDetails(String indent) {
        System.out.println(indent + "Developer: " + name + " (" + technology + ") - $" + salary);
    }
    
    @Override
    public double getSalary() {
        return salary;
    }
    
    @Override
    public String getName() {
        return name;
    }
}

class Designer implements Employee {
    private String name;
    private double salary;
    private String specialty;
    
    public Designer(String name, double salary, String specialty) {
        this.name = name;
        this.salary = salary;
        this.specialty = specialty;
    }
    
    @Override
    public void showDetails(String indent) {
        System.out.println(indent + "Designer: " + name + " (" + specialty + ") - $" + salary);
    }
    
    @Override
    public double getSalary() {
        return salary;
    }
    
    @Override
    public String getName() {
        return name;
    }
}

class ManagerComposite implements Employee {
    private String name;
    private double salary;
    private List<Employee> subordinates;
    
    public ManagerComposite(String name, double salary) {
        this.name = name;
        this.salary = salary;
        this.subordinates = new ArrayList<>();
    }
    
    public void addSubordinate(Employee employee) {
        subordinates.add(employee);
    }
    
    public void removeSubordinate(Employee employee) {
        subordinates.remove(employee);
    }
    
    @Override
    public void showDetails(String indent) {
        System.out.println(indent + "Manager: " + name + " - $" + salary);
        for (Employee subordinate : subordinates) {
            subordinate.showDetails(indent + "  ");
        }
    }
    
    @Override
    public double getSalary() {
        double totalSalary = salary;
        for (Employee subordinate : subordinates) {
            totalSalary += subordinate.getSalary();
        }
        return totalSalary;
    }
    
    @Override
    public String getName() {
        return name;
    }
}

// Demo class
class CompositePatternDemo {
    public static void main(String[] args) {
        System.out.println("=== File System Example ===\n");
        
        // Create files
        File file1 = new File("document.txt", 10);
        File file2 = new File("image.jpg", 500);
        File file3 = new File("video.mp4", 2000);
        File file4 = new File("readme.md", 5);
        File file5 = new File("config.json", 2);
        
        // Create directories
        Directory root = new Directory("root");
        Directory documents = new Directory("documents");
        Directory media = new Directory("media");
        Directory images = new Directory("images");
        Directory videos = new Directory("videos");
        
        // Build tree structure
        root.add(documents);
        root.add(media);
        root.add(file4);  // readme.md in root
        
        documents.add(file1);
        documents.add(file5);
        
        media.add(images);
        media.add(videos);
        
        images.add(file2);
        videos.add(file3);
        
        // Display structure
        root.display("");
        
        // Calculate total size recursively
        System.out.println("\nTotal size: " + root.getSize() + " KB");
        System.out.println("Documents size: " + documents.getSize() + " KB");
        System.out.println("Media size: " + media.getSize() + " KB");
        
        System.out.println("\n=== Organization Hierarchy Example ===\n");
        
        // Create employees
        Developer dev1 = new Developer("Alice", 80000, "Java");
        Developer dev2 = new Developer("Bob", 75000, "Python");
        Developer dev3 = new Developer("Charlie", 70000, "JavaScript");
        Designer designer1 = new Designer("Diana", 65000, "UI/UX");
        Designer designer2 = new Designer("Eve", 60000, "Graphic");
        
        // Create managers
        ManagerComposite techLead = new ManagerComposite("Frank", 100000);
        ManagerComposite designLead = new ManagerComposite("Grace", 95000);
        ManagerComposite cto = new ManagerComposite("Henry", 150000);
        
        // Build organization tree
        techLead.addSubordinate(dev1);
        techLead.addSubordinate(dev2);
        techLead.addSubordinate(dev3);
        
        designLead.addSubordinate(designer1);
        designLead.addSubordinate(designer2);
        
        cto.addSubordinate(techLead);
        cto.addSubordinate(designLead);
        
        // Display organization
        cto.showDetails("");
        
        // Calculate total payroll
        System.out.println("\nTotal payroll: $" + cto.getSalary());
        System.out.println("Tech team payroll: $" + techLead.getSalary());
        System.out.println("Design team payroll: $" + designLead.getSalary());
        
        System.out.println("\n=== Benefits of Composite Pattern ===");
        System.out.println("- Treat individual objects and compositions uniformly");
        System.out.println("- Easy to add new component types");
        System.out.println("- Recursive operations work naturally");
        System.out.println("- Simplifies client code");
    }
}
```

## Complexity Analysis

**Time Complexity**: O(n) - Operations like display() and getSize() traverse all n nodes in the tree. Adding/removing a single component is O(1).

**Space Complexity**: O(n) - The tree structure stores n components. The recursion depth for display() is O(h) where h is the tree height.

## Edge Cases and Pitfalls

- **Null Children**: Always initialize the children list in composite constructors to avoid NullPointerException.
- **Circular References**: Be careful not to add a composite as its own child (directly or indirectly), which would cause infinite loops.
- **Leaf Operations**: Leaves don't have children. Calling add/remove on leaves should either throw an exception or be no-ops, depending on design.
- **Type Safety**: Clients may need to check if a component is a leaf or composite before calling composite-specific methods.
- **Performance**: For large trees, caching computed values (like total size) can improve performance if the tree doesn't change frequently.
- **Thread Safety**: If the tree is modified concurrently, synchronize add/remove operations or use concurrent collections.

## Interview-Ready Answer

"The Composite pattern composes objects into tree structures to represent part-whole hierarchies, allowing clients to treat individual objects and compositions uniformly. I'd create a FileSystemComponent interface with display() and getSize() methods, implement File as a leaf, and Directory as a composite that contains other components. Operations like display() and getSize() work recursively on the tree. This enables building complex hierarchies while keeping client code simple. Time complexity is O(n) for tree traversal, space is O(n) for storing n components."
