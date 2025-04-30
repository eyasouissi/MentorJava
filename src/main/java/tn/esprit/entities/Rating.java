package tn.esprit.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.Objects;

@Entity
@Table(name = "rating")
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, foreignKey = @ForeignKey(name = "fk_rating_course"))
    private Courses course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, foreignKey = @ForeignKey(name = "fk_rating_user"))
    private User user;

    @Column(nullable = false)
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    public Rating() {
    }

    public Rating(Courses course, User user, Integer rating) {
        this.course = course;
        this.user = user;
        this.rating = rating;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Courses getCourse() { return course; }
    public void setCourse(Courses course) { this.course = course; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rating rating1 = (Rating) o;
        return Objects.equals(id, rating1.id) &&
                Objects.equals(course, rating1.course) &&
                Objects.equals(user, rating1.user) &&
                Objects.equals(rating, rating1.rating);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, course, user, rating);
    }

    @Override
    public String toString() {
        return "Rating{" +
                "id=" + id +
                ", courseId=" + (course != null ? course.getId() : null) +
                ", userId=" + (user != null ? user.getId() : null) +
                ", rating=" + rating +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Courses course;
        private User user;
        private Integer rating;

        public Builder course(Courses course) {
            this.course = course;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder rating(Integer rating) {
            this.rating = rating;
            return this;
        }

        public Rating build() {
            return new Rating(course, user, rating);
        }
    }
}