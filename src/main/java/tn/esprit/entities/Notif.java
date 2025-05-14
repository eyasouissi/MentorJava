package tn.esprit.entities;

import java.time.Instant;
import java.util.Objects;

public class Notif {

    private Long id;
    private User user;
    private User triggeredBy;
    private String type;
    private Post post;
    private boolean isRead = false;
    private Instant createdAt;

    // Constructors
    public Notif() {
        this.createdAt = Instant.now();
    }

    public Notif(User user, User triggeredBy, String type, Post post) {
        this();
        this.user = user;
        this.triggeredBy = triggeredBy;
        this.type = type;
        this.post = post;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(User triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    // toString() method
    @Override
    public String toString() {
        return "Notif{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", triggeredById=" + (triggeredBy != null ? triggeredBy.getId() : null) +
                ", type='" + type + '\'' +
                ", postId=" + (post != null ? post.getId() : null) +
                ", isRead=" + isRead +
                ", createdAt=" + createdAt +
                '}';
    }

    // equals() method
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notif notif = (Notif) o;
        return Objects.equals(id, notif.id) &&
                Objects.equals(type, notif.type) &&
                Objects.equals(createdAt, notif.createdAt);
    }

    // hashCode() method
    @Override
    public int hashCode() {
        return Objects.hash(id, type, createdAt);
    }

    // Builder pattern (optional)
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private User user;
        private User triggeredBy;
        private String type;
        private Post post;

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder triggeredBy(User triggeredBy) {
            this.triggeredBy = triggeredBy;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder post(Post post) {
            this.post = post;
            return this;
        }

        public Notif build() {
            return new Notif(user, triggeredBy, type, post);
        }
    }
}
