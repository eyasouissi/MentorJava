package tn.esprit.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Category {
    private int id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private boolean isActive = true;
    private String icon;
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Courses> courses = new ArrayList<>();


    public Category() {
        this.createdAt = LocalDateTime.now();
    }

    public Category(String name) {
        this();
        this.name = name;
    }

    public Category(String name, String description, LocalDateTime createdAt, boolean isActive, String icon) {
        this.name = name;
        this.description = description;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.isActive = isActive;
        this.icon = icon;
    }


    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Changed return type to primitive boolean
    public boolean getIsActive() { return isActive; }

    public void setIsActive(boolean active) { isActive = active; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public List<Courses> getCourses() { return courses; }
    public void setCourses(List<Courses> courses) { this.courses = courses; }

    public void addCourse(Courses course) {
        if (!courses.contains(course)) {
            courses.add(course);
            course.setCategory(this);
        }
    }

    public void removeCourse(Courses course) {
        if (courses.remove(course)) {
            course.setCategory(null);
        }
    }

    public int getCourseCount() {
        return courses.size();
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", isActive=" + isActive +
                ", courseCount=" + getCourseCount() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(id, category.id) &&
                Objects.equals(name, category.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
