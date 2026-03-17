import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {

    // Token Bucket class
    static class TokenBucket {
        int tokens;
        final int maxTokens;
        final double refillRate; // tokens per second
        long lastRefillTime;

        public TokenBucket(int maxTokens, double refillRate) {
            this.maxTokens = maxTokens;
            this.refillRate = refillRate;
            this.tokens = maxTokens;
            this.lastRefillTime = System.currentTimeMillis();
        }

        // Refill tokens based on time passed
        private void refill() {
            long now = System.currentTimeMillis();
            double seconds = (now - lastRefillTime) / 1000.0;

            int tokensToAdd = (int) (seconds * refillRate);

            if (tokensToAdd > 0) {
                tokens = Math.min(maxTokens, tokens + tokensToAdd);
                lastRefillTime = now;
            }
        }

        // Try to consume a token
        public synchronized boolean allowRequest() {
            refill();

            if (tokens > 0) {
                tokens--;
                return true;
            }
            return false;
        }

        public synchronized int getRemainingTokens() {
            refill();
            return tokens;
        }
    }

    // clientId -> TokenBucket
    private ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    private final int MAX_REQUESTS = 1000;
    private final double REFILL_RATE = 1000.0 / 3600; // per second

    // Check rate limit
    public String checkRateLimit(String clientId) {
        buckets.putIfAbsent(clientId,
                new TokenBucket(MAX_REQUESTS, REFILL_RATE));

        TokenBucket bucket = buckets.get(clientId);

        if (bucket.allowRequest()) {
            return "Allowed (" + bucket.getRemainingTokens() + " requests remaining)";
        } else {
            return "Denied (0 requests remaining, retry later)";
        }
    }

    // Get status
    public String getRateLimitStatus(String clientId) {
        TokenBucket bucket = buckets.get(clientId);

        if (bucket == null) {
            return "No data for client";
        }

        int remaining = bucket.getRemainingTokens();
        int used = MAX_REQUESTS - remaining;

        return "{used: " + used + ", limit: " + MAX_REQUESTS + "}";
    }

    // MAIN METHOD
    public static void main(String[] args) {
        RateLimiter limiter = new RateLimiter();

        String client = "abc123";

        for (int i = 0; i < 5; i++) {
            System.out.println(limiter.checkRateLimit(client));
        }

        System.out.println(limiter.getRateLimitStatus(client));
    }
}
