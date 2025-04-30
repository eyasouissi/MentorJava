package tn.esprit.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255, nullable = false)
    @NotBlank(message = "Name cannot be blank")
    @Size(min = 3, max = 255, message = "Name must be between {min} and {max} characters")
    private String name;

    @Column(columnDefinition = "TEXT")
    @Size(max = 500, message = "Description cannot exceed {max} characters")
    private String description;

    @Column(name = "created_at", nullable = false)
    @NotNull(message = "Creation date is required")
    private LocalDateTime createdAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(length = 100, nullable = true)
    @Size(max = 100, message = "Icon cannot exceed {max} characters")
    private String icon;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Courses> courses = new ArrayList<>();

    // Constructors
    public Category() {
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
    }

    public Category(String name) {
        this();
        this.name = name;
    }

    public Category(String name, String description, Boolean isActive, String icon) {
        this();
        this.name = name;
        this.description = description;
        this.isActive = isActive;
        this.icon = icon;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<Courses> getCourses() {
        return courses;
    }

    public void setCourses(List<Courses> courses) {
        this.courses = courses;
    }

    // Relationship management
    public void addCourse(Courses course) {
        courses.add(course);
        course.setCategory(this);
    }

    public void removeCourse(Courses course) {
        courses.remove(course);
        course.setCategory(null);
    }

    // Business methods
    public int getCourseCount() {
        return courses.size();
    }

    // toString()
    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + (description != null ? description.substring(0, Math.min(description.length(), 30)) + "..." : "null") + '\'' +
                ", createdAt=" + createdAt +
                ", isActive=" + isActive +
                ", icon='" + icon + '\'' +
                ", courseCount=" + getCourseCount() +
                '}';
    }

    // equals() and hashCode()
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(id, category.id) &&
                Objects.equals(name, category.name) &&
                Objects.equals(createdAt, category.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, createdAt);
    }
}