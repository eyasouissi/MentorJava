package tn.esprit.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "project")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255, nullable = false)
    @NotBlank(message = "Title cannot be empty!")
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    @NotBlank(message = "Description cannot be empty!")
    @Size(min = 5, message = "Description must contain at least 5 characters!")
    private String description;

    @Column(name = "fichier_pdf", nullable = true)
    private String pdfFile;

    @Column(name = "date_creation_project", nullable = false)
    private LocalDateTime creationDate;

    @Column(nullable = false)
    @NotBlank(message = "Select one of these choices!")
    private String difficulty;

    @Column(name = "date_limite", nullable = true)
    @FutureOrPresent(message = "Please enter a valid date!")
    private LocalDate deadline;

    @Column(nullable = true)
    private String image;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = true)
    private GroupStudent group;

    @Transient
    private MultipartFile pdfUpload;

    @Transient
    private MultipartFile imageUpload;

    // Constructors
    public Project() {
        this.creationDate = LocalDateTime.now();
    }

    public Project(String title, String description, String difficulty) {
        this();
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPdfFile() {
        return pdfFile;
    }

    public void setPdfFile(String pdfFile) {
        this.pdfFile = pdfFile;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public GroupStudent getGroup() {
        return group;
    }

    public void setGroup(GroupStudent group) {
        this.group = group;
    }

    public MultipartFile getPdfUpload() {
        return pdfUpload;
    }

    public void setPdfUpload(MultipartFile pdfUpload) {
        this.pdfUpload = pdfUpload;
    }

    public MultipartFile getImageUpload() {
        return imageUpload;
    }

    public void setImageUpload(MultipartFile imageUpload) {
        this.imageUpload = imageUpload;
    }

    // Business methods
    public boolean isOverdue() {
        return deadline != null && LocalDate.now().isAfter(deadline);
    }

    // toString() method
    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + (description != null ? description.substring(0, Math.min(description.length(), 30)) + "..." : "null") + '\'' +
                ", pdfFile='" + pdfFile + '\'' +
                ", creationDate=" + creationDate +
                ", difficulty='" + difficulty + '\'' +
                ", deadline=" + deadline +
                ", image='" + image + '\'' +
                ", groupId=" + (group != null ? group.getId() : null) +
                '}';
    }

    // equals() method
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return Objects.equals(id, project.id) &&
                Objects.equals(title, project.title) &&
                Objects.equals(creationDate, project.creationDate);
    }

    // hashCode() method
    @Override
    public int hashCode() {
        return Objects.hash(id, title, creationDate);
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String title;
        private String description;
        private String difficulty;
        private LocalDate deadline;
        private GroupStudent group;

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder difficulty(String difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        public Builder deadline(LocalDate deadline) {
            this.deadline = deadline;
            return this;
        }

        public Builder group(GroupStudent group) {
            this.group = group;
            return this;
        }

        public Project build() {
            Project project = new Project(title, description, difficulty);
            project.setDeadline(deadline);
            project.setGroup(group);
            return project;
        }
    }
}