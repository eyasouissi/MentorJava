package tn.esprit.services;

import com.sun.net.httpserver.*;
import tn.esprit.controllers.auth.VerificationController;
import java.io.*;
import java.net.InetSocketAddress;

public class VerificationServer {
    private static HttpServer server;
    private static int ACTUAL_PORT;
    private static boolean isRunning = false;

    public static synchronized void start() throws IOException {
        if (isRunning) {
            return;
        }

        try {
            startWithPort(8085);
            isRunning = true;
        } catch (IOException e) {
            System.out.println("⚠ Port 8085 busy, using random port");
            startWithPort(0);
            isRunning = true;
        }
    }

    private static void startWithPort(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        ACTUAL_PORT = server.getAddress().getPort();

        server.createContext("/verify", exchange -> {
            try {
                String query = exchange.getRequestURI().getQuery();
                String response;
                int statusCode;

                if (query != null && query.startsWith("token=")) {
                    String token = query.substring(6);
                    javafx.application.Platform.runLater(() -> {
                        VerificationController.showVerificationWindow(token);
                    });
                    response = "Verification process started. Please check the application window.";
                    statusCode = 200;
                } else {
                    response = "Invalid verification link. Missing token.";
                    statusCode = 400;
                }

                exchange.sendResponseHeaders(statusCode, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } catch (Exception e) {
                System.err.println("Error handling verification request:");
                e.printStackTrace();
            }
        });

        server.start();
        System.out.println("✅ Verification server running on port " + ACTUAL_PORT);
    }

    public static String getVerificationUrl(String token) {
        if (!isRunning) {
            throw new IllegalStateException("Verification server not running");
        }
        return "http://localhost:" + ACTUAL_PORT + "/verify?token=" + token;
    }

    public static synchronized void stop() {
        if (server != null) {
            server.stop(0);
            isRunning = false;
            System.out.println("🛑 Verification server stopped");
        }
    }
}