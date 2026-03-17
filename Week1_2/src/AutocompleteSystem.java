import java.util.*;

public class AutocompleteSystem {

    // Trie Node
    static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        Map<String, Integer> frequencyMap = new HashMap<>();
    }

    private TrieNode root;
    private Map<String, Integer> globalFrequency;

    public AutocompleteSystem() {
        root = new TrieNode();
        globalFrequency = new HashMap<>();
    }

    // Insert query
    public void insert(String query) {
        globalFrequency.put(query,
                globalFrequency.getOrDefault(query, 0) + 1);

        TrieNode node = root;

        for (char c : query.toCharArray()) {
            node.children.putIfAbsent(c, new TrieNode());
            node = node.children.get(c);

            // store query frequency at each prefix node
            node.frequencyMap.put(query, globalFrequency.get(query));
        }
    }

    // Search top 10 suggestions
    public List<String> search(String prefix) {
        TrieNode node = root;

        // traverse prefix
        for (char c : prefix.toCharArray()) {
            if (!node.children.containsKey(c)) {
                return new ArrayList<>();
            }
            node = node.children.get(c);
        }

        // Min heap for top 10
        PriorityQueue<Map.Entry<String, Integer>> pq =
                new PriorityQueue<>(Map.Entry.comparingByValue());

        for (Map.Entry<String, Integer> entry : node.frequencyMap.entrySet()) {
            pq.add(entry);
            if (pq.size() > 10) {
                pq.poll();
            }
        }

        List<String> result = new ArrayList<>();
        while (!pq.isEmpty()) {
            result.add(pq.poll().getKey());
        }

        Collections.reverse(result); // highest first
        return result;
    }

    // MAIN METHOD
    public static void main(String[] args) {
        AutocompleteSystem system = new AutocompleteSystem();

        system.insert("java tutorial");
        system.insert("javascript");
        system.insert("java download");
        system.insert("java tutorial");
        system.insert("java tutorial");
        system.insert("java 21 features");

        System.out.println("Search 'jav':");
        List<String> results = system.search("jav");

        int rank = 1;
        for (String res : results) {
            System.out.println(rank++ + ". " + res);
        }
    }
}