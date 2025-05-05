package tn.esprit.entities;

import java.util.Objects;

public class File {
    private int id;
    private String fileName;
    private String file_Path; // Chemin complet du fichier
    private boolean isViewed = false;
    private Level level;

    public File() {}

    public File(String fileName) {
        this.fileName = fileName;
    }

    public File(String fileName, String filePath) {
        this.fileName = fileName;
        this.file_Path = filePath;
    }

    public File(String fileName, String filePath, Level level) {
        this(fileName, filePath);
        this.level = level;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return file_Path;
    }

    public void setFilePath(String filePath) {
        this.file_Path = filePath;
    }

    public boolean isViewed() {
        return isViewed;
    }

    public void setViewed(boolean viewed) {
        this.isViewed = viewed;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return "File{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", filePath='" + file_Path + '\'' +
                ", isViewed=" + isViewed +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        File file = (File) o;
        return id == file.id &&
                isViewed == file.isViewed &&
                Objects.equals(fileName, file.fileName) &&
                Objects.equals(file_Path, file.file_Path) &&
                Objects.equals(level, file.level);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fileName, file_Path, isViewed, level);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int id;
        private String fileName;
        private String filePath;
        private boolean isViewed = false;
        private Level level;

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder filePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder isViewed(boolean isViewed) {
            this.isViewed = isViewed;
            return this;
        }

        public Builder level(Level level) {
            this.level = level;
            return this;
        }

        public File build() {
            File file = new File();
            file.setId(id);
            file.setFileName(fileName);
            file.setFilePath(filePath);
            file.setViewed(isViewed);
            file.setLevel(level);
            return file;
        }
    }
}