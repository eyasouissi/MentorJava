// tn/esprit/services/PrivacyService.java
package tn.esprit.services;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrivacyService {
    private static final Pattern[] PATTERNS = {
            // Email pattern
            Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b"),
            // Phone number pattern (international)
            Pattern.compile("\\+?\\d{1,3}[\\s-]?\\(?\\d{1,3}\\)?[\\s-]?\\d{3,4}[\\s-]?\\d{3,4}\\b"),
            // Credit card pattern
            Pattern.compile("\\b(?:\\d{4}[ -]?){3}\\d{4}\\b"),
            // Social security number pattern (US)
            Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b")
    };

    public List<String> findSensitiveInfo(String text) {
        List<String> matches = new ArrayList<>();
        for (Pattern pattern : PATTERNS) {
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                matches.add(matcher.group());
            }
        }
        return matches;
    }
}