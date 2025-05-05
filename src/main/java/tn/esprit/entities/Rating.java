package tn.esprit.entities;

import java.util.Objects;

public class Rating {
    private Long id;
    private Courses course;
    private User user;
    private Integer rating;

    public Rating() {
    }

    public Rating(Courses course, User user, Integer rating) {
        this.course = course;
        this.user = user;
        this.rating = rating;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Courses getCourse() {
        return course;
    }

    public void setCourse(Courses course) {
        this.course = course;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

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