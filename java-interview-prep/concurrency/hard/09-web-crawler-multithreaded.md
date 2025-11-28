# Web Crawler Multithreaded

## Problem Statement

Implement a multithreaded web crawler that crawls URLs in parallel while avoiding duplicate visits and respecting the same-hostname constraint. Use a thread pool to manage worker threads, a concurrent set to track visited URLs, and proper synchronization to coordinate crawling.

**Input**: Starting URL and maximum depth

**Output**: Set of all crawled URLs

**Constraints**: 
- Must crawl in parallel
- Must avoid duplicate visits
- Should respect hostname constraint

## Approach

- Use ExecutorService for thread pool management
- Use ConcurrentHashMap.newKeySet() for thread-safe visited set
- Use BlockingQueue for URLs to crawl
- Extract hostname and only crawl same-hostname URLs
- Use CountDownLatch or Future to track completion
- Implement proper shutdown of thread pool

## Solution

```java
import java.util.*;
import java.util.concurrent.*;

interface HtmlParser {
    List<String> getUrls(String url);
}

class WebCrawler {
    private Set<String> visited = ConcurrentHashMap.newKeySet();
    private ExecutorService executor;
    private HtmlParser parser;
    private String hostname;
    
    public WebCrawler(HtmlParser parser, int threadCount) {
        this.parser = parser;
        this.executor = Executors.newFixedThreadPool(threadCount);
    }
    
    public List<String> crawl(String startUrl) {
        this.hostname = extractHostname(startUrl);
        visited.add(startUrl);
        
        CountDownLatch latch = new CountDownLatch(1);
        crawlUrl(startUrl, latch);
        
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        executor.shutdown();
        return new ArrayList<>(visited);
    }
    
    private void crawlUrl(String url, CountDownLatch latch) {
        executor.submit(() -> {
            try {
                List<String> urls = parser.getUrls(url);
                List<CountDownLatch> childLatches = new ArrayList<>();
                
                for (String nextUrl : urls) {
                    if (isSameHostname(nextUrl) && visited.add(nextUrl)) {
                        CountDownLatch childLatch = new CountDownLatch(1);
                        childLatches.add(childLatch);
                        crawlUrl(nextUrl, childLatch);
                    }
                }
                
                for (CountDownLatch childLatch : childLatches) {
                    childLatch.await();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                latch.countDown();
            }
        });
    }
    
    private String extractHostname(String url) {
        int start = url.indexOf("//") + 2;
        int end = url.indexOf('/', start);
        return end == -1 ? url.substring(start) : url.substring(start, end);
    }
    
    private boolean isSameHostname(String url) {
        return extractHostname(url).equals(hostname);
    }
}

// Alternative solution using CompletableFuture
class WebCrawlerFuture {
    private Set<String> visited = ConcurrentHashMap.newKeySet();
    private HtmlParser parser;
    private String hostname;
    private ExecutorService executor;
    
    public WebCrawlerFuture(HtmlParser parser, int threadCount) {
        this.parser = parser;
        this.executor = Executors.newFixedThreadPool(threadCount);
    }
    
    public List<String> crawl(String startUrl) {
        this.hostname = extractHostname(startUrl);
        visited.add(startUrl);
        
        CompletableFuture<Void> future = crawlAsync(startUrl);
        future.join();
        
        executor.shutdown();
        return new ArrayList<>(visited);
    }
    
    private CompletableFuture<Void> crawlAsync(String url) {
        return CompletableFuture.runAsync(() -> {
            List<String> urls = parser.getUrls(url);
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            
            for (String nextUrl : urls) {
                if (isSameHostname(nextUrl) && visited.add(nextUrl)) {
                    futures.add(crawlAsync(nextUrl));
                }
            }
            
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }, executor);
    }
    
    private String extractHostname(String url) {
        int start = url.indexOf("//") + 2;
        int end = url.indexOf('/', start);
        return end == -1 ? url.substring(start) : url.substring(start, end);
    }
    
    private boolean isSameHostname(String url) {
        return extractHostname(url).equals(hostname);
    }
}

// Mock HTML parser for testing
class MockHtmlParser implements HtmlParser {
    private Map<String, List<String>> graph;
    
    public MockHtmlParser(Map<String, List<String>> graph) {
        this.graph = graph;
    }
    
    @Override
    public List<String> getUrls(String url) {
        try {
            Thread.sleep(100); // Simulate network delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return graph.getOrDefault(url, new ArrayList<>());
    }
}

public class WebCrawlerDemo {
    public static void main(String[] args) {
        Map<String, List<String>> graph = new HashMap<>();
        graph.put("http://example.com", Arrays.asList(
            "http://example.com/page1",
            "http://example.com/page2",
            "http://other.com/page"
        ));
        graph.put("http://example.com/page1", Arrays.asList(
            "http://example.com/page3"
        ));
        graph.put("http://example.com/page2", Arrays.asList(
            "http://example.com/page3",
            "http://example.com/page4"
        ));
        
        HtmlParser parser = new MockHtmlParser(graph);
        
        System.out.println("=== CountDownLatch Solution ===");
        WebCrawler crawler1 = new WebCrawler(parser, 4);
        List<String> result1 = crawler1.crawl("http://example.com");
        System.out.println("Crawled URLs: " + result1.size());
        result1.forEach(System.out::println);
        
        System.out.println("\n=== CompletableFuture Solution ===");
        WebCrawlerFuture crawler2 = new WebCrawlerFuture(parser, 4);
        List<String> result2 = crawler2.crawl("http://example.com");
        System.out.println("Crawled URLs: " + result2.size());
        result2.forEach(System.out::println);
    }
}
```

## Complexity Analysis

**Time Complexity**: O(V + E) where V is URLs and E is links, parallelized across threads

**Space Complexity**: O(V) for visited set and pending tasks

## Edge Cases and Pitfalls

- **Duplicate visits**: Use ConcurrentHashMap.newKeySet() for thread-safe duplicate detection with add() returning boolean.
- **Thread pool sizing**: Too few threads underutilize parallelism, too many cause overhead. Balance based on I/O vs CPU work.
- **Shutdown coordination**: Use CountDownLatch or CompletableFuture to track when all crawling completes before shutting down executor.
- **Hostname extraction**: Handle URLs with and without trailing slashes, ports, and protocols correctly.

## Interview-Ready Answer

"A multithreaded web crawler uses a thread pool to crawl URLs in parallel. Use ConcurrentHashMap.newKeySet() for thread-safe visited tracking. Each thread fetches a URL, extracts links, and submits new URLs to crawl. Use CountDownLatch or CompletableFuture to track completion of all crawling tasks. Extract hostname from URLs and only crawl same-hostname links. The key challenges are avoiding duplicate visits, coordinating thread completion, and properly shutting down the executor."
