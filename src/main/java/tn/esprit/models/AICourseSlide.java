package tn.esprit.models;

public record AICourseSlide(String title, String content) {
    @Override
    public String toString() {
        return title + "\n" + content;
    }
}