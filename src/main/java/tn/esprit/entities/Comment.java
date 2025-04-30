package tn.esprit.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.web.multipart.MultipartFile;

@Entity
@Table(name = "comment")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Post post;

    @Column(length = 2000, nullable = false)
    @NotBlank(message = "Content cannot be empty.")
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(length = 255, nullable = true)
    private String photo;

    @ManyToOne
    @JoinColumn(nullable = false)
    private User user;

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reply> replies = new ArrayList<>();

    @Transient
    private MultipartFile file;

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

    public Comment(String content, Post post, User user, MultipartFile file) {
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

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
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

    // toString() method
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

    // equals() method
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return Objects.equals(id, comment.id) &&
                Objects.equals(content, comment.content) &&
                Objects.equals(createdAt, comment.createdAt);
    }

    // hashCode() method
    @Override
    public int hashCode() {
        return Objects.hash(id, content, createdAt);
    }

    // Business methods
    public int getReplyCount() {
        return replies.size();
    }

    // File upload handling would be implemented in a service layer
    // rather than directly in the entity in Java/Spring
}