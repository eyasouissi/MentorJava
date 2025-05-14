package tn.esprit.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Comment {

    private Long id;
    private Post post;
    private String content;
    private LocalDateTime createdAt;
    private String photo;
    private User user;
    private List<Reply> replies = new ArrayList<>();
    private File file;
    // java.io.File used instead of MultipartFile
    private boolean isEdited = false;

    public boolean isEdited() { return isEdited; }
    public void setEdited(boolean edited) { isEdited = edited; }
    // Constructors
    public Comment() {
        this.createdAt = LocalDateTime.now();
    }

    public Comment(String content, Post post, User user) {
        this();
        this.content = content;
        this.post = post;
        this.user = user;
    }

    public Comment(String content, Post post, User user, File file) {
        this(content, post, user);
        this.file = file;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Reply> getReplies() {
        return replies;
    }

    public void setReplies(List<Reply> replies) {
        this.replies = replies;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    // Relationship management methods
    public void addReply(Reply reply) {
        if (!replies.contains(reply)) {
            replies.add(reply);
            reply.setComment(this);
        }
    }

    public void removeReply(Reply reply) {
        if (replies.remove(reply)) {
            reply.setComment(null);
        }
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", postId=" + (post != null ? post.getId() : null) +
                ", content='" + (content != null ? content.substring(0, Math.min(content.length(), 30)) + "..." : "null") + '\'' +
                ", createdAt=" + createdAt +
                ", photo='" + photo + '\'' +
                ", userId=" + (user != null ? user.getId() : null) +
                ", repliesCount=" + replies.size() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Comment)) return false;
        Comment comment = (Comment) o;
        return Objects.equals(id, comment.id) &&
                Objects.equals(content, comment.content) &&
                Objects.equals(createdAt, comment.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, content, createdAt);
    }

    public int getReplyCount() {
        return replies.size();
    }
}
