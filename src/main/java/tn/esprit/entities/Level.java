package tn.esprit.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Level {
    private int id;
    private String name;
    private Courses course;
    private List<File> files = new ArrayList<>();
    private Level previousLevel;
    private boolean isComplete = false;

    public Level() {}

    public Level(String name, Courses course) {
        this.name = name;
        this.course = course;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Courses getCourse() { return course; }
    public void setCourse(Courses course) { this.course = course; }
    public List<File> getFiles() { return files; }
    public void setFiles(List<File> files) { this.files = files; }
    public Level getPreviousLevel() { return previousLevel; }
    public void setPreviousLevel(Level previousLevel) { this.previousLevel = previousLevel; }
    public boolean isComplete() { return isComplete; }
    public void setComplete(boolean complete) { this.isComplete = complete; }

    public boolean isUnlocked(User user) {
        if (this.previousLevel == null) {
            return true;
        }
        return user.hasCompletedLevel(this.previousLevel);
    }

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

    @Override
    public String toString() {
        return "Level{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", isComplete=" + isComplete +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Level level = (Level) o;
        return Objects.equals(id, level.id) &&
                Objects.equals(name, level.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private Courses course;
        private Level previousLevel;
        private boolean isComplete = false;

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

        public Builder isComplete(boolean isComplete) {
            this.isComplete = isComplete;
            return this;
        }

        public Level build() {
            Level level = new Level(name, course);
            level.setPreviousLevel(previousLevel);
            level.setComplete(isComplete);
            return level;
        }
    }

}
