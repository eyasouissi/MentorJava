package tn.esprit.utils;

import com.stripe.Stripe;

public class StripeConfig {
    // Clé API secrète pour l'environnement de test
    // Remplacez par votre clé secrète réelle de Stripe
    public static final String STRIPE_SECRET_KEY = "sk_test_51Qr4STH5Ru1zkbIax3ydCqrG2RutA88to3Xz5bg4L8gh1gf07xjhHTXgbK9NOzIAifAIIMFjG1u3ZCBXcFUMqKVA00bTjKj2hw";
    // Initialiser Stripe une seule fois dans votre application
    public static void initializeStripe() {
        Stripe.apiKey = STRIPE_SECRET_KEY;
        System.out.println("Stripe initialisé avec succès avec la clé: " + STRIPE_SECRET_KEY.substring(0, 8) + "...");
    }
}

