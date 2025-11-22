# Design Twitter

## Problem Statement

Design a simplified version of Twitter where users can post tweets, follow/unfollow other users, and see the 10 most recent tweets in their news feed. Implement the Twitter class:
- `Twitter()` Initializes the object
- `void postTweet(int userId, int tweetId)` Composes a new tweet
- `List<Integer> getNewsFeed(int userId)` Retrieves the 10 most recent tweet IDs in the user's news feed
- `void follow(int followerId, int followeeId)` User followerId follows user followeeId
- `void unfollow(int followerId, int followeeId)` User followerId unfollows user followeeId

**Constraints:**
- 1 ≤ userId, followerId, followeeId ≤ 500
- 0 ≤ tweetId ≤ 10⁴
- At most 3 * 10⁴ calls to postTweet, getNewsFeed, follow, and unfollow

## Approach

- Use hash map to store user to list of tweets
- Use hash map to store user to set of followees
- Use min heap to merge tweets from user and all followees
- Store tweets with timestamp for ordering
- Each user automatically follows themselves

## Solution

```java
import java.util.*;

class Twitter {
    private static int timestamp = 0;
    
    class Tweet {
        int id;
        int time;
        Tweet next;
        
        Tweet(int id) {
            this.id = id;
            this.time = timestamp++;
            this.next = null;
        }
    }
    
    private Map<Integer, Tweet> tweets;
    private Map<Integer, Set<Integer>> following;
    
    public Twitter() {
        tweets = new HashMap<>();
        following = new HashMap<>();
    }
    
    public void postTweet(int userId, int tweetId) {
        Tweet tweet = new Tweet(tweetId);
        tweet.next = tweets.get(userId);
        tweets.put(userId, tweet);
    }
    
    public List<Integer> getNewsFeed(int userId) {
        List<Integer> result = new ArrayList<>();
        PriorityQueue<Tweet> maxHeap = new PriorityQueue<>((a, b) -> b.time - a.time);
        
        // Add user's own tweets
        if (tweets.containsKey(userId)) {
            maxHeap.offer(tweets.get(userId));
        }
        
        // Add tweets from followees
        Set<Integer> followees = following.get(userId);
        if (followees != null) {
            for (int followeeId : followees) {
                if (tweets.containsKey(followeeId)) {
                    maxHeap.offer(tweets.get(followeeId));
                }
            }
        }
        
        // Get 10 most recent tweets
        while (!maxHeap.isEmpty() && result.size() < 10) {
            Tweet tweet = maxHeap.poll();
            result.add(tweet.id);
            if (tweet.next != null) {
                maxHeap.offer(tweet.next);
            }
        }
        
        return result;
    }
    
    public void follow(int followerId, int followeeId) {
        if (followerId == followeeId) return;
        following.putIfAbsent(followerId, new HashSet<>());
        following.get(followerId).add(followeeId);
    }
    
    public void unfollow(int followerId, int followeeId) {
        if (following.containsKey(followerId)) {
            following.get(followerId).remove(followeeId);
        }
    }
}
```

## Complexity Analysis

**Time Complexity**: 
- postTweet: O(1)
- getNewsFeed: O(N log K) where N is total tweets and K is number of followees
- follow/unfollow: O(1)

**Space Complexity**: O(U + T) - Where U is number of users and T is total tweets

## Edge Cases and Pitfalls

- **Edge Case 1**: User follows themselves - Should be prevented or handled
- **Edge Case 2**: User has no tweets or followees - Returns empty list
- **Edge Case 3**: Less than 10 tweets available - Returns all available tweets
- **Common Pitfall 1**: Not including user's own tweets in feed - User should see their own tweets
- **Common Pitfall 2**: Not using timestamp for ordering - Can't determine most recent

## Interview-Ready Answer

I would use a hash map for user-to-tweets and another for user-to-followees. Tweets are stored as a linked list with timestamps. For news feed, I use a max heap to merge tweets from the user and followees, extracting the 10 most recent. PostTweet is O(1), getNewsFeed is O(N log K).
