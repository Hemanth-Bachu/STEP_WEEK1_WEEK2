import java.util.*;

public class DNSCache {

    // Entry class
    static class DNSEntry {
        String domain;
        String ipAddress;
        long expiryTime;

        public DNSEntry(String domain, String ipAddress, long ttlSeconds) {
            this.domain = domain;
            this.ipAddress = ipAddress;
            this.expiryTime = System.currentTimeMillis() + (ttlSeconds * 1000);
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    // LRU Cache using LinkedHashMap
    private final int capacity;
    private LinkedHashMap<String, DNSEntry> cache;

    // Metrics
    private int hits = 0;
    private int misses = 0;

    public DNSCache(int capacity) {
        this.capacity = capacity;

        this.cache = new LinkedHashMap<String, DNSEntry>(capacity, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                return size() > capacity; // LRU eviction
            }
        };

        startCleanupThread();
    }

    // Resolve domain
    public synchronized String resolve(String domain) {
        long start = System.nanoTime();

        DNSEntry entry = cache.get(domain);

        if (entry != null && !entry.isExpired()) {
            hits++;
            long time = (System.nanoTime() - start) / 1_000_000;
            return "Cache HIT → " + entry.ipAddress + " (" + time + " ms)";
        }

        // MISS or EXPIRED
        misses++;
        String ip = queryUpstreamDNS(domain);

        cache.put(domain, new DNSEntry(domain, ip, 5)); // TTL = 5 sec (demo)

        return "Cache MISS → " + ip;
    }

    // Simulated upstream DNS
    private String queryUpstreamDNS(String domain) {
        try {
            Thread.sleep(100); // simulate 100ms latency
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return "192.168.1." + new Random().nextInt(255);
    }

    // Background cleanup thread
    private void startCleanupThread() {
        Thread cleaner = new Thread(() -> {
            while (true) {
                synchronized (this) {
                    Iterator<Map.Entry<String, DNSEntry>> it = cache.entrySet().iterator();

                    while (it.hasNext()) {
                        Map.Entry<String, DNSEntry> entry = it.next();
                        if (entry.getValue().isExpired()) {
                            it.remove();
                        }
                    }
                }

                try {
                    Thread.sleep(2000); // cleanup every 2 sec
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        cleaner.setDaemon(true);
        cleaner.start();
    }

    // Stats
    public void getCacheStats() {
        int total = hits + misses;
        double hitRate = total == 0 ? 0 : (hits * 100.0) / total;

        System.out.println("Cache Hits: " + hits);
        System.out.println("Cache Misses: " + misses);
        System.out.println("Hit Rate: " + hitRate + "%");
    }

    // MAIN METHOD
    public static void main(String[] args) throws InterruptedException {
        DNSCache dnsCache = new DNSCache(3);

        System.out.println(dnsCache.resolve("google.com"));
        System.out.println(dnsCache.resolve("google.com")); // HIT

        Thread.sleep(6000); // wait for TTL expiry

        System.out.println(dnsCache.resolve("google.com")); // EXPIRED

        dnsCache.getCacheStats();
    }
}