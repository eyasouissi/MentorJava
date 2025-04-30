package tn.esprit.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "courses")
public class Courses {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255, nullable = false)
    @NotBlank(message = "Title cannot be blank")
    @Size(min = 3, max = 255, message = "Title must be between {min} and {max} characters")
    private String title;

    @Column(columnDefinition = "TEXT", nullable = true)
    @Size(max = 1000, message = "Description cannot exceed {max} characters")
    private String description;

    @Column(nullable = false)
    private Boolean isPublished = true;

    @Column(nullable = true)
    @PositiveOrZero(message = "Progress points must be positive or zero")
    private Integer progressPointsRequired = 0;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, foreignKey = @ForeignKey(name = "fk_course_category"))
    @NotNull(message = "Category is required")
    private Category category;

    @OneToMany(mappedBy = "course", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Level> levels = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    private List<Rating> ratings = new ArrayList<>();

    @Column(nullable = false)
    private Boolean isPremium = false;

    @Column(length = 255, nullable = true)
    private String tutorName;

    public Courses() {
    }

    public Courses(String title, Category category) {
        this.title = title;
        this.category = category;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getIsPublished() { return isPublished; }
    public void setIsPublished(Boolean published) { isPublished = published; }
    public Integer getProgressPointsRequired() { return progressPointsRequired; }
    public void setProgressPointsRequired(Integer points) { this.progressPointsRequired = points; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public List<Level> getLevels() { return levels; }
    public void setLevels(List<Level> levels) { this.levels = levels; }
    public List<Rating> getRatings() { return ratings; }
    public void setRatings(List<Rating> ratings) { this.ratings = ratings; }
    public Boolean getIsPremium() { return isPremium; }
    public void setIsPremium(Boolean premium) { isPremium = premium; }
    public String getTutorName() { return tutorName; }
    public void setTutorName(String tutorName) { this.tutorName = tutorName; }

    public Double getAverageRating() {
        if (ratings.isEmpty()) return 0.0;
        return ratings.stream()
                .mapToDouble(Rating::getRating)
                .average()
                .orElse(0.0);
    }

    public void addLevel(Level level) {
        if (!levels.contains(level)) {
            levels.add(level);
            level.setCourse(this);
        }
    }

    public void removeLevel(Level level) {
        levels.remove(level);
        level.setCourse(null);
    }

    public void addRating(Rating rating) {
        if (!ratings.contains(rating)) {
            ratings.add(rating);
            rating.setCourse(this);
        }
    }

    public void removeRating(Rating rating) {
        ratings.remove(rating);
        rating.setCourse(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Courses courses = (Courses) o;
        return Objects.equals(id, courses.id) &&
                Objects.equals(title, courses.title) &&
                Objects.equals(createdAt, courses.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, createdAt);
    }

    @Override
    public String toString() {
        return "Courses{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", isPublished=" + isPublished +
                ", createdAt=" + createdAt +
                ", categoryId=" + (category != null ? category.getId() : null) +
                ", levelsCount=" + levels.size() +
                ", ratingsCount=" + ratings.size() +
                ", averageRating=" + getAverageRating() +
                '}';
    }
}