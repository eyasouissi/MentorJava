package tn.esprit.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "prohibited_word")
public class ProhibitedWord {

    public enum Category {
        PROFANITY,
        HATE_SPEECH,
        PERSONAL_INFO,
        OTHER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255, unique = true, nullable = false)
    @NotBlank(message = "Word cannot be blank")
    private String word;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private Category category;

    @Column(nullable = false)
    @Min(value = 1, message = "Severity must be at least 1")
    @Max(value = 5, message = "Severity must be at most 5")
    private Integer severity = 1;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    // Lifecycle callback
    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    // Constructors
    public ProhibitedWord() {
    }

    public ProhibitedWord(String word, Category category) {
        this.word = word;
        this.category = category;
    }

    public ProhibitedWord(String word, Category category, Integer severity) {
        this(word, category);
        this.severity = severity;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Integer getSeverity() {
        return severity;
    }

    public void setSeverity(Integer severity) {
        this.severity = severity;
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
        return "ProhibitedWord{" +
                "id=" + id +
                ", word='" + word + '\'' +
                ", category=" + category +
                ", severity=" + severity +
                ", createdAt=" + createdAt +
                '}';
    }

    // equals() method
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProhibitedWord that = (ProhibitedWord) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(word, that.word);
    }

    // hashCode() method
    @Override
    public int hashCode() {
        return Objects.hash(id, word);
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String word;
        private Category category;
        private Integer severity = 1;

        public Builder word(String word) {
            this.word = word;
            return this;
        }

        public Builder category(Category category) {
            this.category = category;
            return this;
        }

        public Builder severity(Integer severity) {
            this.severity = severity;
            return this;
        }

        public ProhibitedWord build() {
            return new ProhibitedWord(word, category, severity);
        }
    }
}