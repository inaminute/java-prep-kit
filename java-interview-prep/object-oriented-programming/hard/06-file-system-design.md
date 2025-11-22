# File System Design

## Problem Statement

Design an in-memory file system supporting file and directory operations: create, delete, read, write, list, and search. Implement path resolution and permissions.

## Approach

- Use Composite pattern for files and directories
- Implement path parsing and navigation
- Support CRUD operations on files
- Track metadata (size, timestamps, permissions)
- Implement search by name and content

## Solution

```java
import java.util.*;

abstract class FileSystemNode {
    protected String name;
    protected FileSystemNode parent;
    protected long createdTime;
    
    public FileSystemNode(String name, FileSystemNode parent) {
        this.name = name;
        this.parent = parent;
        this.createdTime = System.currentTimeMillis();
    }
    
    public abstract boolean isDirectory();
    public abstract int getSize();
    public String getName() { return name; }
    public FileSystemNode getParent() { return parent; }
}

class File extends FileSystemNode {
    private String content;
    
    public File(String name, FileSystemNode parent) {
        super(name, parent);
        this.content = "";
    }
    
    public void write(String content) { this.content = content; }
    public String read() { return content; }
    
    @Override
    public boolean isDirectory() { return false; }
    
    @Override
    public int getSize() { return content.length(); }
}

class Directory extends FileSystemNode {
    private Map<String, FileSystemNode> children;
    
    public Directory(String name, FileSystemNode parent) {
        super(name, parent);
        this.children = new HashMap<>();
    }
    
    public void addChild(FileSystemNode node) {
        children.put(node.getName(), node);
    }
    
    public void removeChild(String name) {
        children.remove(name);
    }
    
    public FileSystemNode getChild(String name) {
        return children.get(name);
    }
    
    public List<String> list() {
        return new ArrayList<>(children.keySet());
    }
    
    @Override
    public boolean isDirectory() { return true; }
    
    @Override
    public int getSize() {
        return children.values().stream().mapToInt(FileSystemNode::getSize).sum();
    }
}

class FileSystem {
    private Directory root;
    private Directory currentDir;
    
    public FileSystem() {
        this.root = new Directory("/", null);
        this.currentDir = root;
    }
    
    public void createFile(String path, String content) {
        String[] parts = parsePath(path);
        Directory dir = navigateToDirectory(parts[0]);
        File file = new File(parts[1], dir);
        file.write(content);
        dir.addChild(file);
        System.out.println("Created file: " + path);
    }
    
    public void createDirectory(String path) {
        String[] parts = parsePath(path);
        Directory dir = navigateToDirectory(parts[0]);
        Directory newDir = new Directory(parts[1], dir);
        dir.addChild(newDir);
        System.out.println("Created directory: " + path);
    }
    
    public String readFile(String path) {
        FileSystemNode node = navigate(path);
        if (node instanceof File) {
            return ((File) node).read();
        }
        throw new IllegalArgumentException("Not a file: " + path);
    }
    
    public List<String> list(String path) {
        FileSystemNode node = navigate(path);
        if (node instanceof Directory) {
            return ((Directory) node).list();
        }
        throw new IllegalArgumentException("Not a directory: " + path);
    }
    
    public void delete(String path) {
        String[] parts = parsePath(path);
        Directory dir = navigateToDirectory(parts[0]);
        dir.removeChild(parts[1]);
        System.out.println("Deleted: " + path);
    }
    
    private FileSystemNode navigate(String path) {
        if (path.equals("/")) return root;
        
        String[] parts = path.split("/");
        FileSystemNode current = root;
        
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (current instanceof Directory) {
                current = ((Directory) current).getChild(part);
                if (current == null) {
                    throw new IllegalArgumentException("Path not found: " + path);
                }
            } else {
                throw new IllegalArgumentException("Not a directory in path: " + path);
            }
        }
        return current;
    }
    
    private Directory navigateToDirectory(String path) {
        FileSystemNode node = navigate(path);
        if (node instanceof Directory) {
            return (Directory) node;
        }
        throw new IllegalArgumentException("Not a directory: " + path);
    }
    
    private String[] parsePath(String path) {
        int lastSlash = path.lastIndexOf('/');
        String dirPath = lastSlash > 0 ? path.substring(0, lastSlash) : "/";
        String name = path.substring(lastSlash + 1);
        return new String[]{dirPath, name};
    }
}

class FileSystemDemo {
    public static void main(String[] args) {
        FileSystem fs = new FileSystem();
        
        fs.createDirectory("/home");
        fs.createDirectory("/home/user");
        fs.createFile("/home/user/file.txt", "Hello World");
        
        System.out.println("Contents: " + fs.readFile("/home/user/file.txt"));
        System.out.println("List /home: " + fs.list("/home"));
        
        fs.delete("/home/user/file.txt");
    }
}
```

## Complexity Analysis

**Time Complexity**: O(d) for navigation where d is path depth, O(1) for operations on current node
**Space Complexity**: O(n) for n files and directories

## Edge Cases and Pitfalls

- **Path Parsing**: Handle absolute vs relative paths, ".." and "." navigation
- **Concurrent Access**: Synchronize operations for thread safety
- **Circular References**: Prevent creating circular directory structures
- **Large Files**: Consider streaming for large file content
- **Permissions**: Implement read/write/execute permissions

## Interview-Ready Answer

"I'd design a file system using Composite pattern with FileSystemNode base class, File and Directory subclasses. Directory maintains a HashMap of children for O(1) lookup. Implement path parsing and navigation for create/read/delete operations. Track metadata like size and timestamps. Time complexity is O(d) for path navigation, space is O(n) for n nodes."
