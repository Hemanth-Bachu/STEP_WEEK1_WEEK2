import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RealTimeAnalytics {

    // page -> total visits
    private Map<String, Integer> pageViews;

    // page -> unique users
    private Map<String, Set<String>> uniqueVisitors;

    // source -> count
    private Map<String, Integer> trafficSources;

    public RealTimeAnalytics() {
        pageViews = new ConcurrentHashMap<>();
        uniqueVisitors = new ConcurrentHashMap<>();
        trafficSources = new ConcurrentHashMap<>();

        startDashboardUpdater();
    }

    // Event structure
    static class Event {
        String url;
        String userId;
        String source;

        public Event(String url, String userId, String source) {
            this.url = url;
            this.userId = userId;
            this.source = source;
        }
    }

    // Process incoming event (O(1))
    public void processEvent(Event event) {

        // update page views
        pageViews.put(event.url,
                pageViews.getOrDefault(event.url, 0) + 1);

        // update unique visitors
        uniqueVisitors.putIfAbsent(event.url, new HashSet<>());
        uniqueVisitors.get(event.url).add(event.userId);

        // update traffic source
        trafficSources.put(event.source,
                trafficSources.getOrDefault(event.source, 0) + 1);
    }

    // Get Top 10 pages
    public List<Map.Entry<String, Integer>> getTopPages() {
        PriorityQueue<Map.Entry<String, Integer>> pq =
                new PriorityQueue<>(Map.Entry.comparingByValue());

        for (Map.Entry<String, Integer> entry : pageViews.entrySet()) {
            pq.add(entry);
            if (pq.size() > 10) {
                pq.poll(); // remove smallest
            }
        }

        List<Map.Entry<String, Integer>> result = new ArrayList<>(pq);
        result.sort((a, b) -> b.getValue() - a.getValue());

        return result;
    }

    // Dashboard output
    public void printDashboard() {
        System.out.println("\n===== DASHBOARD =====");

        System.out.println("Top Pages:");
        for (Map.Entry<String, Integer> entry : getTopPages()) {
            String url = entry.getKey();
            int views = entry.getValue();
            int unique = uniqueVisitors.get(url).size();

            System.out.println(url + " - " + views + " views (" + unique + " unique)");
        }

        System.out.println("\nTraffic Sources:");
        int total = trafficSources.values().stream().mapToInt(i -> i).sum();

        for (Map.Entry<String, Integer> entry : trafficSources.entrySet()) {
            double percent = (entry.getValue() * 100.0) / total;
            System.out.println(entry.getKey() + ": " + String.format("%.1f", percent) + "%");
        }
    }

    // Auto-refresh every 5 seconds
    private void startDashboardUpdater() {
        Thread t = new Thread(() -> {
            while (true) {
                printDashboard();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        t.setDaemon(true);
        t.start();
    }

    // MAIN METHOD
    public static void main(String[] args) {
        RealTimeAnalytics analytics = new RealTimeAnalytics();

        String[] urls = {
                "/article/breaking-news",
                "/sports/championship",
                "/tech/ai"
        };

        String[] sources = {"google", "facebook", "direct"};

        Random rand = new Random();

        // simulate streaming events
        for (int i = 0; i < 100; i++) {
            String url = urls[rand.nextInt(urls.length)];
            String user = "user_" + rand.nextInt(50);
            String source = sources[rand.nextInt(sources.length)];

            analytics.processEvent(new Event(url, user, source));

            try {
                Thread.sleep(100); // simulate real-time flow
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}