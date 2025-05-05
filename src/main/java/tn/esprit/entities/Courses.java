package tn.esprit.entities;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Courses {
    private int id;
    private String title;
    private String description;
    private boolean isPublished = true; // using primitive boolean
    private int progressPointsRequired = 0; // using primitive int
    private Instant createdAt = Instant.now();
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Category category;

    private List<Level> levels = new ArrayList<>();
    private List<Rating> ratings = new ArrayList<>();
    private boolean isPremium = false; // using primitive boolean
    private String tutorName;

    public Courses() {}

    public Courses(String title, Category category) {
        this.title = title;
        this.category = category;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // Changed return type to primitive boolean
    public boolean getIsPublished() { return isPublished; }

    // Set method for isPublished is kept as is, since you're using primitive boolean for the field
    public void setIsPublished(boolean published) { isPublished = published; }

    // Changed to return primitive int
    public int getProgressPointsRequired() { return progressPointsRequired; }

    // Set method for progressPointsRequired
    public void setProgressPointsRequired(int points) { this.progressPointsRequired = points; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public List<Level> getLevels() { return levels; }
    public void setLevels(List<Level> levels) { this.levels = levels; }
    public List<Rating> getRatings() { return ratings; }
    public void setRatings(List<Rating> ratings) { this.ratings = ratings; }

    // Changed return type to primitive boolean
    public boolean getIsPremium() { return isPremium; }

    // Set method for isPremium is kept as is, since you're using primitive boolean for the field
    public void setIsPremium(boolean premium) { isPremium = premium; }

    public String getTutorName() { return tutorName; }
    public void setTutorName(String tutorName) { this.tutorName = tutorName; }

    public double getAverageRating() {
        if (ratings.isEmpty()) {
            return 0.0;  // Return 0.0 as Double if no ratings
        }
        return ratings.stream()
                .mapToDouble(Rating::getRating)
                .average()
                .orElse(0.0);  // Return 0.0 as Double if no average rating is found
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
                Objects.equals(title, courses.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title);
    }

    @Override
    public String toString() {
        return "Courses{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", averageRating=" + getAverageRating() +
                '}';
    }
}
