import java.util.*;

public class PlagiarismDetector {

    // n-gram size
    private static final int N = 5;

    // n-gram -> set of document IDs
    private Map<String, Set<String>> index;

    // documentId -> list of n-grams
    private Map<String, List<String>> documentNGrams;

    public PlagiarismDetector() {
        index = new HashMap<>();
        documentNGrams = new HashMap<>();
    }

    // Add document to system
    public void addDocument(String docId, String content) {
        List<String> ngrams = generateNGrams(content);
        documentNGrams.put(docId, ngrams);

        for (String gram : ngrams) {
            index.putIfAbsent(gram, new HashSet<>());
            index.get(gram).add(docId);
        }
    }

    // Generate n-grams
    private List<String> generateNGrams(String text) {
        List<String> result = new ArrayList<>();

        String[] words = text.toLowerCase().split("\\s+");

        for (int i = 0; i <= words.length - N; i++) {
            StringBuilder gram = new StringBuilder();
            for (int j = 0; j < N; j++) {
                gram.append(words[i + j]).append(" ");
            }
            result.add(gram.toString().trim());
        }

        return result;
    }

    // Analyze document for plagiarism
    public void analyzeDocument(String docId) {
        List<String> targetNGrams = documentNGrams.get(docId);

        Map<String, Integer> matchCount = new HashMap<>();

        for (String gram : targetNGrams) {
            if (index.containsKey(gram)) {
                for (String otherDoc : index.get(gram)) {
                    if (!otherDoc.equals(docId)) {
                        matchCount.put(otherDoc,
                                matchCount.getOrDefault(otherDoc, 0) + 1);
                    }
                }
            }
        }

        System.out.println("Analyzing: " + docId);
        System.out.println("Total n-grams: " + targetNGrams.size());

        for (Map.Entry<String, Integer> entry : matchCount.entrySet()) {
            String otherDoc = entry.getKey();
            int matches = entry.getValue();

            double similarity = (matches * 100.0) / targetNGrams.size();

            System.out.println("Matched with: " + otherDoc);
            System.out.println("Matching n-grams: " + matches);
            System.out.println("Similarity: " + String.format("%.2f", similarity) + "%");

            if (similarity > 50) {
                System.out.println("⚠️ PLAGIARISM DETECTED!");
            } else if (similarity > 15) {
                System.out.println("⚠️ Suspicious content");
            }

            System.out.println("----------------------");
        }
    }

    // MAIN METHOD
    public static void main(String[] args) {
        PlagiarismDetector detector = new PlagiarismDetector();

        String doc1 = "This is a simple example of plagiarism detection system using n grams for checking similarity";
        String doc2 = "This is a simple example of plagiarism detection system using n grams to check similarity";
        String doc3 = "Completely different content with no similarity at all in this document";

        detector.addDocument("essay_089", doc1);
        detector.addDocument("essay_092", doc2);
        detector.addDocument("essay_123", doc3);

        detector.analyzeDocument("essay_092");
    }
}