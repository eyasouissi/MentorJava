package tn.esprit.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "level")
public class Level {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255, nullable = false)
    @NotBlank(message = "Level name cannot be blank")
    @Size(min = 3, max = 255, message = "Level name must be between {min} and {max} characters")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "course_id", foreignKey = @ForeignKey(name = "fk_level_course"))
    @NotNull(message = "Course is required")
    private Courses course;

    @OneToMany(mappedBy = "level", cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    private List<File> files = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_level_id")
    private Level previousLevel;

    @Column(nullable = false)
    private Boolean isComplete = false;

    // Constructors
    public Level() {
    }

    public Level(String name, Courses course) {
        this.name = name;
        this.course = course;
    }

    // Business method
    public boolean isUnlocked(User user) {
        // Level 1 is always unlocked
        if (this.previousLevel == null) {
            return true;
        }

        // Check if user has completed previous level
        return user.hasCompletedLevel(this.previousLevel);
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

    public Courses getCourse() {
        return course;
    }

    public void setCourse(Courses course) {
        this.course = course;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public Level getPreviousLevel() {
        return previousLevel;
    }

    public void setPreviousLevel(Level previousLevel) {
        this.previousLevel = previousLevel;
    }

    public Boolean getIsComplete() {
        return isComplete;
    }

    public void setIsComplete(Boolean complete) {
        isComplete = complete;
    }

    // Relationship management methods
    public void addFile(File file) {
        if (!files.contains(file)) {
            files.add(file);
            file.setLevel(this);
        }
    }

    public void removeFile(File file) {
        if (files.remove(file)) {
            file.setLevel(null);
        }
    }

    // toString() method
    @Override
    public String toString() {
        return "Level{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", courseId=" + (course != null ? course.getId() : null) +
                ", filesCount=" + files.size() +
                ", previousLevelId=" + (previousLevel != null ? previousLevel.getId() : null) +
                ", isComplete=" + isComplete +
                '}';
    }

    // equals() method
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Level level = (Level) o;
        return Objects.equals(id, level.id) &&
                Objects.equals(name, level.name);
    }

    // hashCode() method
    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private Courses course;
        private Level previousLevel;
        private Boolean isComplete = false;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder course(Courses course) {
            this.course = course;
            return this;
        }

        public Builder previousLevel(Level previousLevel) {
            this.previousLevel = previousLevel;
            return this;
        }

        public Builder isComplete(Boolean isComplete) {
            this.isComplete = isComplete;
            return this;
        }

        public Level build() {
            Level level = new Level(name, course);
            level.setPreviousLevel(previousLevel);
            level.setIsComplete(isComplete);
            return level;
        }
    }
}
