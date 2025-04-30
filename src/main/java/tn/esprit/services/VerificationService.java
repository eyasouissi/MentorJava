package tn.esprit.services;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class VerificationService {
    public static String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }

    public static LocalDateTime calculateExpiryDate() {
        return LocalDateTime.now().plus(24, ChronoUnit.HOURS); // 24-hour expiry
    }
}