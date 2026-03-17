import java.util.*;

public class UsernameChecker {

    // username -> userId
    private Map<String, Integer> userMap;

    // username -> attempt count
    private Map<String, Integer> attemptCount;

    public UsernameChecker() {
        userMap = new HashMap<>();
        attemptCount = new HashMap<>();
    }

    // Add user (simulate registered users)
    public void registerUser(String username, int userId) {
        userMap.put(username, userId);
    }

    // Check availability (O(1))
    public boolean checkAvailability(String username) {
        // track attempts
        attemptCount.put(username, attemptCount.getOrDefault(username, 0) + 1);

        return !userMap.containsKey(username);
    }

    // Suggest alternatives
    public List<String> suggestAlternatives(String username) {
        List<String> suggestions = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            String suggestion = username + i;
            if (!userMap.containsKey(suggestion)) {
                suggestions.add(suggestion);
            }
        }

        // Try replacing underscore with dot
        if (username.contains("_")) {
            String alt = username.replace("_", ".");
            if (!userMap.containsKey(alt)) {
                suggestions.add(alt);
            }
        }

        return suggestions;
    }

    // Get most attempted username
    public String getMostAttempted() {
        String maxUser = null;
        int maxCount = 0;

        for (Map.Entry<String, Integer> entry : attemptCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                maxUser = entry.getKey();
            }
        }

        return maxUser + " (" + maxCount + " attempts)";
    }

    // MAIN METHOD FOR TESTING
    public static void main(String[] args) {
        UsernameChecker checker = new UsernameChecker();

        // Preload some users
        checker.registerUser("john_doe", 1);
        checker.registerUser("admin", 2);

        System.out.println("john_doe available? " + checker.checkAvailability("john_doe"));
        System.out.println("jane_smith available? " + checker.checkAvailability("jane_smith"));

        System.out.println("Suggestions for john_doe: " + checker.suggestAlternatives("john_doe"));

        // simulate multiple attempts
        checker.checkAvailability("admin");
        checker.checkAvailability("admin");
        checker.checkAvailability("admin");

        System.out.println("Most attempted: " + checker.getMostAttempted());
    }
}