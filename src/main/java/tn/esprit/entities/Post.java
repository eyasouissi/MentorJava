package tn.esprit.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Post {
    private Integer id;
    private Forum forum;
    private User user;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer likes = 0;
    private String photos;
    private String gifUrl;
    private List<Comment> comments = new ArrayList<>();
    private Set<User> likedByUsers = new HashSet<>();
    private int user_id;
    private List<String> gifReactions = new ArrayList<>();


    public Post() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Post(String content, User user, Forum forum) {
        this();
        this.content = content;
        this.user = user;
        this.forum = forum;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Forum getForum() { return forum; }
    public void setForum(Forum forum) { this.forum = forum; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getContent() { return content; }
    public void setContent(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Integer getLikes() { return likes; }
    public void setLikes(Integer likes) { this.likes = likes; }

    public String getPhotos() { return photos; }
    public void setPhotos(String photos) { this.photos = photos; }

    public String getGifUrl() { return gifUrl; }
    public void setGifUrl(String gifUrl) { this.gifUrl = gifUrl; }

    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }

    public Set<User> getLikedByUsers() { return likedByUsers; }
    public void setLikedByUsers(Set<User> likedByUsers) { this.likedByUsers = likedByUsers; }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", forum=" + (forum != null ? forum.getId() : null) +
                ", user=" + (user != null ? user.getId() : null) +
                ", content='" + (content != null ? content.substring(0, Math.min(30, content.length())) + "..." : "") + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", likes=" + likes +
                ", gifUrl='" + gifUrl + '\'' +
                '}';
    }

    // Manual relationship management methods
    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setPost(this);
    }

    public void removeComment(Comment comment) {
        comments.remove(comment);
        comment.setPost(null);
    }

    public void addLike(User user) {
        likedByUsers.add(user);
        // If maintaining bidirectional relationship
        user.getLikedPosts().add(this);
    }

    public void removeLike(User user) {
        likedByUsers.remove(user);
        // If maintaining bidirectional relationship
        user.getLikedPosts().remove(this);
    }

    public int getUserId() { return user_id; }
    public void setUserId(int userId) { this.user_id = user_id; }
    public List<String> getGifReactions() {
        return gifReactions;
    }

    public void addGifReaction(String gifUrl) {
        gifReactions.add(gifUrl);
    }

    // In Post.java
    public boolean hasLiked(User user) {
        return likedByUsers.contains(user);
    }

    public void toggleLike(User user) {
        if (hasLiked(user)) {
            likedByUsers.remove(user);
            likes = Math.max(0, likes - 1);
        } else {
            likedByUsers.add(user);
            likes++;
        }
    }
}