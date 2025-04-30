package tn.esprit.entities;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "file")
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;

    @Column(name = "is_viewed", nullable = false)
    private Boolean isViewed = false;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "level_id")
    private Level level;

    // Constructors
    public File() {
    }

    public File(String fileName) {
        this.fileName = fileName;
    }

    public File(String fileName, Level level) {
        this(fileName);
        this.level = level;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Boolean isViewed() {
        return isViewed;
    }

    public void setViewed(Boolean viewed) {
        isViewed = viewed;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    // Business method
    public String getFilePath() {
        return "/uploads/images/" + this.fileName;
    }

    // toString() method
    @Override
    public String toString() {
        return "File{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", isViewed=" + isViewed +
                ", levelId=" + (level != null ? level.getId() : null) +
                '}';
    }

    // equals() method
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        File file = (File) o;
        return Objects.equals(id, file.id) &&
                Objects.equals(fileName, file.fileName);
    }

    // hashCode() method
    @Override
    public int hashCode() {
        return Objects.hash(id, fileName);
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String fileName;
        private Level level;
        private Boolean isViewed = false;

        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder level(Level level) {
            this.level = level;
            return this;
        }

        public Builder isViewed(Boolean isViewed) {
            this.isViewed = isViewed;
            return this;
        }

        public File build() {
            File file = new File(fileName, level);
            file.setViewed(isViewed);
            return file;
        }
    }
}