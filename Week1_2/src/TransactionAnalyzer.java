import java.util.*;

public class TransactionAnalyzer {

    static class Transaction {
        int id;
        int amount;
        String merchant;
        String account;
        long time; // epoch seconds

        public Transaction(int id, int amount, String merchant, String account, long time) {
            this.id = id;
            this.amount = amount;
            this.merchant = merchant;
            this.account = account;
            this.time = time;
        }
    }

    // 1. Classic Two-Sum
    public static List<int[]> findTwoSum(List<Transaction> transactions, int target) {
        Map<Integer, Transaction> map = new HashMap<>();
        List<int[]> result = new ArrayList<>();

        for (Transaction t : transactions) {
            int complement = target - t.amount;

            if (map.containsKey(complement)) {
                result.add(new int[]{map.get(complement).id, t.id});
            }

            map.put(t.amount, t);
        }

        return result;
    }

    // 2. Two-Sum with Time Window (1 hour)
    public static List<int[]> findTwoSumWithTime(List<Transaction> transactions, int target) {
        List<int[]> result = new ArrayList<>();

        for (int i = 0; i < transactions.size(); i++) {
            Map<Integer, Transaction> map = new HashMap<>();

            for (int j = i + 1; j < transactions.size(); j++) {
                Transaction t1 = transactions.get(i);
                Transaction t2 = transactions.get(j);

                // check time difference <= 1 hour (3600 sec)
                if (Math.abs(t1.time - t2.time) <= 3600) {
                    int complement = target - t2.amount;

                    if (map.containsKey(complement)) {
                        result.add(new int[]{map.get(complement).id, t2.id});
                    }

                    map.put(t2.amount, t2);
                }
            }
        }

        return result;
    }

    // 3. K-Sum (generalized)
    public static void kSumHelper(List<Transaction> transactions, int target, int k,
                                  int start, List<Integer> path, List<List<Integer>> result) {

        if (k == 0 && target == 0) {
            result.add(new ArrayList<>(path));
            return;
        }

        if (k == 0 || start >= transactions.size()) return;

        for (int i = start; i < transactions.size(); i++) {
            path.add(transactions.get(i).id);

            kSumHelper(transactions,
                    target - transactions.get(i).amount,
                    k - 1,
                    i + 1,
                    path,
                    result);

            path.remove(path.size() - 1);
        }
    }

    public static List<List<Integer>> findKSum(List<Transaction> transactions, int k, int target) {
        List<List<Integer>> result = new ArrayList<>();
        kSumHelper(transactions, target, k, 0, new ArrayList<>(), result);
        return result;
    }

    // 4. Duplicate Detection
    public static void detectDuplicates(List<Transaction> transactions) {
        Map<String, Set<String>> map = new HashMap<>();

        for (Transaction t : transactions) {
            String key = t.amount + "_" + t.merchant;

            map.putIfAbsent(key, new HashSet<>());
            map.get(key).add(t.account);
        }

        for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
            if (entry.getValue().size() > 1) {
                System.out.println("Duplicate detected: " + entry.getKey() +
                        " accounts: " + entry.getValue());
            }
        }
    }

    // MAIN METHOD
    public static void main(String[] args) {

        List<Transaction> transactions = Arrays.asList(
                new Transaction(1, 500, "StoreA", "acc1", 1000),
                new Transaction(2, 300, "StoreB", "acc2", 1100),
                new Transaction(3, 200, "StoreC", "acc3", 1200),
                new Transaction(4, 500, "StoreA", "acc4", 1300)
        );

        System.out.println("Two Sum:");
        for (int[] pair : findTwoSum(transactions, 500)) {
            System.out.println(pair[0] + ", " + pair[1]);
        }

        System.out.println("\nK Sum:");
        System.out.println(findKSum(transactions, 3, 1000));

        System.out.println("\nDuplicates:");
        detectDuplicates(transactions);
    }
}