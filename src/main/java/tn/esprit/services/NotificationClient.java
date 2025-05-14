package tn.esprit.services;

import com.google.gson.Gson;
import javafx.application.Platform;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import tn.esprit.entities.Notification;

import java.net.URI;
import java.net.URISyntaxException;

public class NotificationClient {
    private WebSocketClient client;
    private final Gson gson = new Gson();
    private final NotificationHandler handler;

    public interface NotificationHandler {
        void handleNotification(Notification notification);
    }

    public NotificationClient(Long userId, NotificationHandler handler) {
        this.handler = handler;
        try {
            client = new WebSocketClient(new URI("ws://localhost:8081/notifications/" + userId)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("Connected to notification server");
                }

                @Override
                public void onMessage(String message) {
                    try {
                        Notification notification = gson.fromJson(message, Notification.class);
                        Platform.runLater(() -> handler.handleNotification(notification));
                    } catch (Exception e) {
                        System.err.println("Error parsing notification: " + e.getMessage());
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("Disconnected from notification server: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    System.err.println("WebSocket error: " + ex.getMessage());
                    ex.printStackTrace();
                }
            };
            
            // Connect with retry mechanism
            connectWithRetry();
            
        } catch (URISyntaxException e) {
            System.err.println("Invalid WebSocket URI: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void connectWithRetry() {
        Thread connectThread = new Thread(() -> {
            int maxRetries = 5;
            int retryCount = 0;
            boolean connected = false;

            while (!connected && retryCount < maxRetries) {
                try {
                    connected = client.connectBlocking();
                    if (connected) {
                        System.out.println("Successfully connected to WebSocket server");
                    } else {
                        retryCount++;
                        Thread.sleep(2000); // Wait 2 seconds before retrying
                    }
                } catch (InterruptedException e) {
                    System.err.println("Connection attempt interrupted: " + e.getMessage());
                    break;
                }
            }

            if (!connected) {
                System.err.println("Failed to connect to WebSocket server after " + maxRetries + " attempts");
            }
        });
        
        connectThread.setDaemon(true);
        connectThread.start();
    }

    public boolean isOpen() {
        return client != null && client.isOpen();
    }

    public void send(String message) {
        if (isOpen()) {
            client.send(message);
        } else {
            System.err.println("Cannot send message: WebSocket is not open");
        }
    }

    public void close() {
        if (client != null) {
            try {
                client.closeBlocking();
            } catch (InterruptedException e) {
                System.err.println("Error closing WebSocket: " + e.getMessage());
            }
        }
    }
} 