# Dynamic Proxy

## Problem Statement

Explain Java dynamic proxies and demonstrate how to create proxy objects at runtime. Show use cases like AOP, lazy loading, and logging. Compare with static proxies and CGLIB.

**Requirements**:
- Create dynamic proxy using Proxy class
- Implement InvocationHandler
- Show practical use cases (logging, caching, security)
- Compare with static proxies

## Approach

- Dynamic proxies created at runtime using Proxy.newProxyInstance()
- InvocationHandler intercepts method calls
- Works only with interfaces (JDK proxy limitation)
- CGLIB can proxy classes without interfaces
- Common for AOP, lazy loading, remote proxies

## Solution

```java
import java.lang.reflect.*;

interface UserService {
    void createUser(String name);
    String getUser(int id);
}

class UserServiceImpl implements UserService {
    public void createUser(String name) {
        System.out.println("Creating user: " + name);
    }
    
    public String getUser(int id) {
        return "User" + id;
    }
}

// Logging proxy
class LoggingHandler implements InvocationHandler {
    private Object target;
    
    public LoggingHandler(Object target) {
        this.target = target;
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("Before: " + method.getName());
        Object result = method.invoke(target, args);
        System.out.println("After: " + method.getName());
        return result;
    }
}

// Caching proxy
class CachingHandler implements InvocationHandler {
    private Object target;
    private java.util.Map<String, Object> cache = new java.util.HashMap<>();
    
    public CachingHandler(Object target) {
        this.target = target;
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String key = method.getName() + java.util.Arrays.toString(args);
        
        if (cache.containsKey(key)) {
            System.out.println("Cache hit for: " + key);
            return cache.get(key);
        }
        
        Object result = method.invoke(target, args);
        cache.put(key, result);
        System.out.println("Cache miss, stored: " + key);
        return result;
    }
}

// Security proxy
class SecurityHandler implements InvocationHandler {
    private Object target;
    private String currentUser;
    
    public SecurityHandler(Object target, String currentUser) {
        this.target = target;
        this.currentUser = currentUser;
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("createUser") && !currentUser.equals("admin")) {
            throw new SecurityException("Only admin can create users");
        }
        return method.invoke(target, args);
    }
}

public class DynamicProxy {
    
    public static void main(String[] args) {
        demonstrateLoggingProxy();
        demonstrateCachingProxy();
        demonstrateSecurityProxy();
        explainComparison();
    }
    
    public static void demonstrateLoggingProxy() {
        System.out.println("=== Logging Proxy ===");
        
        UserService service = new UserServiceImpl();
        UserService proxy = (UserService) Proxy.newProxyInstance(
            service.getClass().getClassLoader(),
            service.getClass().getInterfaces(),
            new LoggingHandler(service)
        );
        
        proxy.createUser("Alice");
        proxy.getUser(1);
    }
    
    public static void demonstrateCachingProxy() {
        System.out.println("\n=== Caching Proxy ===");
        
        UserService service = new UserServiceImpl();
        UserService proxy = (UserService) Proxy.newProxyInstance(
            service.getClass().getClassLoader(),
            service.getClass().getInterfaces(),
            new CachingHandler(service)
        );
        
        proxy.getUser(1); // Cache miss
        proxy.getUser(1); // Cache hit
    }
    
    public static void demonstrateSecurityProxy() {
        System.out.println("\n=== Security Proxy ===");
        
        UserService service = new UserServiceImpl();
        UserService adminProxy = (UserService) Proxy.newProxyInstance(
            service.getClass().getClassLoader(),
            service.getClass().getInterfaces(),
            new SecurityHandler(service, "admin")
        );
        
        UserService userProxy = (UserService) Proxy.newProxyInstance(
            service.getClass().getClassLoader(),
            service.getClass().getInterfaces(),
            new SecurityHandler(service, "user")
        );
        
        adminProxy.createUser("Bob"); // Allowed
        
        try {
            userProxy.createUser("Charlie"); // Denied
        } catch (SecurityException e) {
            System.out.println("Security exception: " + e.getMessage());
        }
    }
    
    public static void explainComparison() {
        System.out.println("\n=== Static vs Dynamic Proxy ===");
        
        System.out.println("Static Proxy:");
        System.out.println("  - Written at compile time");
        System.out.println("  - One proxy class per interface");
        System.out.println("  - More boilerplate code");
        
        System.out.println("\nDynamic Proxy (JDK):");
        System.out.println("  - Created at runtime");
        System.out.println("  - One handler for multiple interfaces");
        System.out.println("  - Requires interface");
        
        System.out.println("\nCGLIB Proxy:");
        System.out.println("  - Can proxy classes without interfaces");
        System.out.println("  - Uses bytecode generation");
        System.out.println("  - Used by Spring AOP");
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) for proxy creation, slight overhead per method call

**Space Complexity**: O(1) for proxy object

## Edge Cases and Pitfalls

- **Interface requirement**: JDK proxy requires interface
- **Performance**: Slight overhead from reflection
- **Final methods**: Cannot be proxied with CGLIB

## Interview-Ready Answer

"Dynamic proxies are created at runtime using Proxy.newProxyInstance() with InvocationHandler to intercept method calls. JDK proxies require interfaces. Common uses: AOP (logging, transactions), lazy loading, caching, security checks. CGLIB can proxy classes without interfaces using bytecode generation. Spring AOP uses both depending on whether target implements interface."
