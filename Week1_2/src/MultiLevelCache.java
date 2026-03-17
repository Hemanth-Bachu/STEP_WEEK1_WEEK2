import java.util.*;

public class MultiLevelCache {

    // LRU Cache using LinkedHashMap
    static class LRUCache<K, V> extends LinkedHashMap<K, V> {
        private int capacity;

        public LRUCache(int capacity) {
            super(capacity, 0.75f, true);
            this.capacity = capacity;
        }

        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > capacity;
        }
    }

    // Simulated video data
    static class Video {
        String id;
        String content;

        public Video(String id) {
            this.id = id;
            this.content = "VideoData_" + id;
        }
    }

    private LRUCache<String, Video> L1;
    private LRUCache<String, Video> L2;
    private Map<String, Video> L3; // database

    private Map<String, Integer> accessCount;

    // stats
    private int l1Hits = 0, l2Hits = 0, l3Hits = 0;

    public MultiLevelCache() {
        L1 = new LRUCache<>(3);   // small for demo
        L2 = new LRUCache<>(5);
        L3 = new HashMap<>();
        accessCount = new HashMap<>();

        // preload DB
        for (int i = 1; i <= 10; i++) {
            L3.put("video_" + i, new Video("video_" + i));
        }
    }

    public Video getVideo(String videoId) {

        long start = System.nanoTime();

        // L1
        if (L1.containsKey(videoId)) {
            l1Hits++;
            System.out.println("L1 HIT");
            return L1.get(videoId);
        }

        // L2
        if (L2.containsKey(videoId)) {
            l2Hits++;
            System.out.println("L2 HIT → Promoting to L1");

            Video v = L2.get(videoId);
            L1.put(videoId, v);

            return v;
        }

        // L3
        if (L3.containsKey(videoId)) {
            l3Hits++;
            System.out.println("L3 HIT → Adding to L2");

            Video v = L3.get(videoId);
            L2.put(videoId, v);

            return v;
        }

        return null;
    }

    // Track access count for promotion logic
    public void recordAccess(String videoId) {
        accessCount.put(videoId,
                accessCount.getOrDefault(videoId, 0) + 1);

        // promote if frequently accessed
        if (accessCount.get(videoId) > 2 && L2.containsKey(videoId)) {
            System.out.println("Promoting " + videoId + " to L1 (hot content)");
            L1.put(videoId, L2.get(videoId));
        }
    }

    // Cache invalidation
    public void invalidate(String videoId) {
        L1.remove(videoId);
        L2.remove(videoId);
        L3.remove(videoId);

        System.out.println("Invalidated " + videoId + " from all levels");
    }

    // Stats
    public void getStats() {
        int total = l1Hits + l2Hits + l3Hits;

        System.out.println("\n===== Cache Stats =====");
        System.out.println("L1 Hit Rate: " + percent(l1Hits, total));
        System.out.println("L2 Hit Rate: " + percent(l2Hits, total));
        System.out.println("L3 Hit Rate: " + percent(l3Hits, total));
    }

    private String percent(int part, int total) {
        return total == 0 ? "0%" :
                String.format("%.2f%%", (part * 100.0) / total);
    }

    // MAIN METHOD
    public static void main(String[] args) {
        MultiLevelCache cache = new MultiLevelCache();

        cache.getVideo("video_1"); // L3
        cache.recordAccess("video_1");

        cache.getVideo("video_1"); // L2
        cache.recordAccess("video_1");

        cache.getVideo("video_1"); // L1

        cache.getVideo("video_2"); // L3
        cache.getVideo("video_2"); // L2

        cache.invalidate("video_1");

        cache.getStats();
    }
}