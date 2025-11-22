# Social Network Design

## Problem Statement

Design a social networking platform supporting user profiles, friendships, posts, comments, likes, and news feed generation.

## Approach

- User management with profiles and privacy settings
- Friend connections with bidirectional relationships
- Post creation with text, images, and timestamps
- Engagement features (likes, comments, shares)
- News feed algorithm based on relevance and recency

## Solution

```java
import java.time.*;
import java.util.*;

class User {
    private String userId;
    private String name;
    private String email;
    private Set<String> friends;
    private List<Post> posts;
    
    public User(String userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.friends = new HashSet<>();
        this.posts = new ArrayList<>();
    }
    
    public void addFriend(String friendId) { friends.add(friendId); }
    public void removeFriend(String friendId) { friends.remove(friendId); }
    public void addPost(Post post) { posts.add(post); }
    
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public Set<String> getFriends() { return friends; }
    public List<Post> getPosts() { return posts; }
}

class Post {
    private String postId;
    private String userId;
    private String content;
    private LocalDateTime timestamp;
    private Set<String> likes;
    private List<Comment> comments;
    
    public Post(String postId, String userId, String content) {
        this.postId = postId;
        this.userId = userId;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.likes = new HashSet<>();
        this.comments = new ArrayList<>();
    }
    
    public void like(String userId) { likes.add(userId); }
    public void unlike(String userId) { likes.remove(userId); }
    public void addComment(Comment comment) { comments.add(comment); }
    
    public String getPostId() { return postId; }
    public String getUserId() { return userId; }
    public String getContent() { return content; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public int getLikeCount() { return likes.size(); }
    public List<Comment> getComments() { return comments; }
}

class Comment {
    private String commentId;
    private String userId;
    private String content;
    private LocalDateTime timestamp;
    
    public Comment(String commentId, String userId, String content) {
        this.commentId = commentId;
        this.userId = userId;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }
    
    public String getUserId() { return userId; }
    public String getContent() { return content; }
}

class SocialNetwork {
    private Map<String, User> users;
    private Map<String, Post> posts;
    private int postCounter;
    private int commentCounter;
    
    public SocialNetwork() {
        this.users = new HashMap<>();
        this.posts = new HashMap<>();
        this.postCounter = 1;
        this.commentCounter = 1;
    }
    
    public void registerUser(User user) {
        users.put(user.getUserId(), user);
        System.out.println("Registered user: " + user.getName());
    }
    
    public void addFriendship(String userId1, String userId2) {
        User user1 = users.get(userId1);
        User user2 = users.get(userId2);
        
        if (user1 != null && user2 != null) {
            user1.addFriend(userId2);
            user2.addFriend(userId1);
            System.out.println(user1.getName() + " and " + user2.getName() + " are now friends");
        }
    }
    
    public Post createPost(String userId, String content) {
        User user = users.get(userId);
        if (user == null) return null;
        
        String postId = "POST-" + (postCounter++);
        Post post = new Post(postId, userId, content);
        posts.put(postId, post);
        user.addPost(post);
        
        System.out.println(user.getName() + " created a post");
        return post;
    }
    
    public void likePost(String postId, String userId) {
        Post post = posts.get(postId);
        if (post != null) {
            post.like(userId);
            System.out.println("Post liked");
        }
    }
    
    public void commentOnPost(String postId, String userId, String content) {
        Post post = posts.get(postId);
        if (post != null) {
            String commentId = "COMMENT-" + (commentCounter++);
            Comment comment = new Comment(commentId, userId, content);
            post.addComment(comment);
            System.out.println("Comment added");
        }
    }
    
    public List<Post> getNewsFeed(String userId) {
        User user = users.get(userId);
        if (user == null) return new ArrayList<>();
        
        List<Post> feed = new ArrayList<>();
        
        // Add user's own posts
        feed.addAll(user.getPosts());
        
        // Add friends' posts
        for (String friendId : user.getFriends()) {
            User friend = users.get(friendId);
            if (friend != null) {
                feed.addAll(friend.getPosts());
            }
        }
        
        // Sort by timestamp (most recent first)
        feed.sort((p1, p2) -> p2.getTimestamp().compareTo(p1.getTimestamp()));
        
        return feed;
    }
}

class SocialNetworkDemo {
    public static void main(String[] args) {
        SocialNetwork network = new SocialNetwork();
        
        User alice = new User("U001", "Alice", "alice@example.com");
        User bob = new User("U002", "Bob", "bob@example.com");
        
        network.registerUser(alice);
        network.registerUser(bob);
        network.addFriendship("U001", "U002");
        
        Post post1 = network.createPost("U001", "Hello World!");
        Post post2 = network.createPost("U002", "Great day!");
        
        network.likePost(post1.getPostId(), "U002");
        network.commentOnPost(post1.getPostId(), "U002", "Nice post!");
        
        List<Post> feed = network.getNewsFeed("U001");
        System.out.println("\nNews Feed for Alice:");
        for (Post post : feed) {
            User author = network.users.get(post.getUserId());
            System.out.println(author.getName() + ": " + post.getContent() + 
                             " (" + post.getLikeCount() + " likes, " + 
                             post.getComments().size() + " comments)");
        }
    }
}
```

## Complexity Analysis

**Time Complexity**: O(u + f*p) for news feed where u is user posts, f is friends, p is posts per friend
**Space Complexity**: O(u + p + c) for users, posts, and comments

## Edge Cases and Pitfalls

- **Privacy Settings**: Implement post visibility controls
- **Scalability**: Use pagination for news feed and friend lists
- **Friend Suggestions**: Implement mutual friends algorithm
- **Notifications**: Notify users of likes, comments, and friend requests
- **Content Moderation**: Filter inappropriate content

## Interview-Ready Answer

"I'd design a social network with User, Post, and Comment classes. Use HashSet for O(1) friend lookups and HashMap for users/posts. Implement bidirectional friendships, post creation with likes/comments, and news feed generation by aggregating user and friends' posts sorted by timestamp. Support engagement features and privacy controls. Time complexity is O(u+f*p) for feed generation, space is O(u+p+c)."
