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
            System.out.println("ℹ Verification server already running");
            return;
        }

        try {
            startWithPort(8085);
            isRunning = true;
        } catch (IOException e) {
            System.out.println("⚠ Port 8085 busy, trying random port");
            startWithPort(0); // Let system choose available port
            isRunning = true;
        }
    }

    private static void startWithPort(int port) throws IOException {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            ACTUAL_PORT = server.getAddress().getPort();

            server.createContext("/verify", VerificationServer::handleVerificationRequest);
            server.setExecutor(null); // creates a default executor
            server.start();

            System.out.println("✅ Verification server running on port " + ACTUAL_PORT);
        } catch (IOException e) {
            System.err.println("❌ Failed to start verification server: " + e.getMessage());
            throw e;
        }
    }
    private static void handleVerificationRequest(HttpExchange exchange) throws IOException {
        try {
            String query = exchange.getRequestURI().getQuery();
            String response;
            int statusCode;

            if (query != null && query.startsWith("token=")) {
                String token = query.substring(6);
                System.out.println("🔑 Received verification token: " + token);

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
            System.err.println("❌ Error handling verification request:");
            e.printStackTrace();
            exchange.sendResponseHeaders(500, 0);
        }
    }

    public static String getVerificationUrl(String token) {
        if (!isRunning) {
            try {
                start();
            } catch (IOException e) {
                System.err.println("⚠ Failed to start verification server: " + e.getMessage());
                return "http://localhost:8085/verify?token=" + token + " (Server not running)";
            }
        }

        String baseUrl = "http://localhost:" + ACTUAL_PORT;
        System.out.println("🔗 Verification URL: " + baseUrl + "/verify?token=" + token);
        return baseUrl + "/verify?token=" + token;
    }

    public static synchronized void stop() {
        if (server != null) {
            server.stop(0);
            isRunning = false;
            System.out.println("🛑 Verification server stopped");
        }
    }
}