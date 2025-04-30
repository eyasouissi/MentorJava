package tn.esprit.services;

import java.util.*;

public class LocalSentimentAnalyzer {
    private static final Set<String> NEGATIVE_WORDS = Set.of(
            "hate", "awful", "terrible", "worst", "disgusting", "horrible",
            "bad", "angry", "upset", "sad", "unhappy", "negative",
            "annoying", "disappointed", "failure", "tragic", "sucks",
            "painful", "hateful", "depressing", "gross", "nasty", "ugly",
            "insult", "rude", "dumb", "stupid", "worthless", "mean",
            "poor", "unacceptable", "offensive", "abuse", "ashamed",
            "cry", "jealous", "regret", "disgust", "pathetic"
    );

    private static final Set<String> POSITIVE_WORDS = Set.of(
            "love", "great", "excellent", "awesome", "best", "wonderful",
            "good", "happy", "joy", "pleased", "positive", "perfect",
            "amazing", "beautiful", "fantastic", "brilliant", "impressive",
            "delightful", "lovely", "superb", "outstanding", "cool", "nice",
            "peaceful", "smart", "friendly", "helpful", "kind", "blessed",
            "incredible", "spectacular", "marvelous", "encouraging", "adorable",
            "inspiring", "grateful", "fun", "sweet", "talented", "creative"
    );

    public SentimentResult analyze(String text) {
        List<String> words = Arrays.asList(text.toLowerCase().split("\\s+"));

        long positiveCount = words.stream()
                .filter(POSITIVE_WORDS::contains)
                .count();

        long negativeCount = words.stream()
                .filter(NEGATIVE_WORDS::contains)
                .count();

        double total = words.size();
        double score = (positiveCount - negativeCount) / total;

        return new SentimentResult(
                score,
                negativeCount / total,
                (total - positiveCount - negativeCount) / total,
                positiveCount / total
        );
    }

    public static class SentimentResult {
        private final double score;
        private final double negative;
        private final double neutral;
        private final double positive;

        public SentimentResult(double score, double negative,
                               double neutral, double positive) {
            this.score = score;
            this.negative = negative;
            this.neutral = neutral;
            this.positive = positive;
        }

        public double getScore() { return score; }
        public double getNegative() { return negative; }
        public double getNeutral() { return neutral; }
        public double getPositive() { return positive; }
    }
}
