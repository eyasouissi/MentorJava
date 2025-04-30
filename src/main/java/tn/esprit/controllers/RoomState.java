package tn.esprit.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomState {
    private Map<String, WidgetState> widgets = new HashMap<>();
    private List<PhotoState> photos = new ArrayList<>();
    private List<TodoItemState> todoItems = new ArrayList<>();
    private TimerState timerState;
    // Getters and setters
}

class WidgetState {
    private boolean visible;
    private double x;
    private double y;
    private double width;
    private double height;
    // Getters and setters
}

class PhotoState {
    private String imageUri;
    private double x;
    private double y;
    private double width;
    private double height;
    // Getters and setters
}

class TodoItemState {
    private String text;
    private boolean checked;
    // Getters and setters
}

class TimerState {
    private int secondsRemaining;
    private boolean isRunning;
    // Getters and setters
}