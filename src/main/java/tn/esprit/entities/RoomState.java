package tn.esprit.entities;

import com.google.gson.annotations.SerializedName;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomState {
    @SerializedName("widgetPositions")
    private Map<String, WidgetPosition> widgetPositions = new HashMap<>();

    @SerializedName("chatHistory")
    private List<String> chatHistory = new ArrayList<>();

    @SerializedName("whiteboardData")
    private List<WhiteboardStroke> whiteboardData = new ArrayList<>();

    @SerializedName("todoItems")
    private List<TodoItem> todoItems = new ArrayList<>();

    @SerializedName("notes")
    private List<Note> notes = new ArrayList<>();

    @SerializedName("workspaceImages")
    private List<WorkspaceImage> workspaceImages = new ArrayList<>();

    @SerializedName("youtubeUrl")
    private String youtubeUrl = "";

    @SerializedName("timerState")
    private TimerState timerState;

    @SerializedName("activeTool")
    private String activeTool;

    @SerializedName("lastUsedColor")
    private String lastUsedColor = "#000000";

    @SerializedName("lastEncouragement")
    private String lastEncouragement = "";

    // Getters and setters
    public Map<String, WidgetPosition> getWidgetPositions() { return widgetPositions; }
    public void setWidgetPositions(Map<String, WidgetPosition> positions) { this.widgetPositions = positions; }

    public List<String> getChatHistory() { return chatHistory; }
    public void setChatHistory(List<String> history) { this.chatHistory = history; }

    public List<WhiteboardStroke> getWhiteboardData() { return whiteboardData; }
    public void setWhiteboardData(List<WhiteboardStroke> data) { this.whiteboardData = data; }

    public List<TodoItem> getTodoItems() { return todoItems; }
    public void setTodoItems(List<TodoItem> items) { this.todoItems = items; }

    public List<Note> getNotes() { return notes; }
    public void setNotes(List<Note> notes) { this.notes = notes; }

    public List<WorkspaceImage> getWorkspaceImages() { return workspaceImages; }
    public void setWorkspaceImages(List<WorkspaceImage> images) { this.workspaceImages = images; }

    public String getYoutubeUrl() { return youtubeUrl; }
    public void setYoutubeUrl(String url) { this.youtubeUrl = url; }

    public TimerState getTimerState() { return timerState; }
    public void setTimerState(TimerState state) { this.timerState = state; }

    public String getActiveTool() { return activeTool; }
    public void setActiveTool(String tool) { this.activeTool = tool; }

    public String getLastUsedColor() { return lastUsedColor; }
    public void setLastUsedColor(String color) { this.lastUsedColor = color; }

    public String getLastEncouragement() { return lastEncouragement; }
    public void setLastEncouragement(String message) { this.lastEncouragement = message; }

    public static class WhiteboardStroke {
        @SerializedName("color")
        private String color;
        @SerializedName("points")
        private List<Double> points = new ArrayList<>();

        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        public List<Double> getPoints() { return points; }
        public void setPoints(List<Double> points) { this.points = points; }
    }

    public static class WidgetPosition {
        @SerializedName("x")
        private double x;
        @SerializedName("y")
        private double y;
        @SerializedName("visible")
        private boolean visible;
        @SerializedName("type")
        private String type;

        public double getX() { return x; }
        public void setX(double x) { this.x = x; }
        public double getY() { return y; }
        public void setY(double y) { this.y = y; }
        public boolean isVisible() { return visible; }
        public void setVisible(boolean visible) { this.visible = visible; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    public static class TodoItem {
        @SerializedName("text")
        private String text;
        @SerializedName("completed")
        private boolean completed;

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
    }

    public static class Note {
        @SerializedName("content")
        private String content;
        @SerializedName("x")
        private double x;
        @SerializedName("y")
        private double y;

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public double getX() { return x; }
        public void setX(double x) { this.x = x; }
        public double getY() { return y; }
        public void setY(double y) { this.y = y; }
    }

    public static class TimerState {
        @SerializedName("secondsRemaining")
        private int secondsRemaining;
        @SerializedName("running")
        private boolean running;

        public int getSecondsRemaining() { return secondsRemaining; }
        public void setSecondsRemaining(int seconds) { this.secondsRemaining = seconds; }
        public boolean isRunning() { return running; }
        public void setRunning(boolean running) { this.running = running; }
    }

    public static class WorkspaceImage {
        @SerializedName("url")
        private String url;
        @SerializedName("x")
        private double x;
        @SerializedName("y")
        private double y;
        @SerializedName("width")
        private double width;

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public double getX() { return x; }
        public void setX(double x) { this.x = x; }
        public double getY() { return y; }
        public void setY(double y) { this.y = y; }
        public double getWidth() { return width; }
        public void setWidth(double width) { this.width = width; }
    }
}