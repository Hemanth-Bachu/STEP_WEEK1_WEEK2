import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FlashSaleInventoryManager {

    // productId -> stock
    private Map<String, Integer> stockMap;

    // productId -> waiting list (FIFO)
    private Map<String, Queue<Integer>> waitingList;

    public FlashSaleInventoryManager() {
        stockMap = new ConcurrentHashMap<>();
        waitingList = new ConcurrentHashMap<>();
    }

    // Initialize product
    public void addProduct(String productId, int stock) {
        stockMap.put(productId, stock);
        waitingList.put(productId, new LinkedList<>());
    }

    // Check stock (O(1))
    public int checkStock(String productId) {
        return stockMap.getOrDefault(productId, 0);
    }

    // Thread-safe purchase
    public String purchaseItem(String productId, int userId) {
        synchronized (productId.intern()) {  // lock per product

            int stock = stockMap.getOrDefault(productId, 0);

            if (stock > 0) {
                stockMap.put(productId, stock - 1);
                return "Success! Remaining stock: " + (stock - 1);
            } else {
                Queue<Integer> queue = waitingList.get(productId);
                queue.add(userId);
                return "Out of stock. Added to waiting list. Position: " + queue.size();
            }
        }
    }

    // Get waiting list
    public Queue<Integer> getWaitingList(String productId) {
        return waitingList.get(productId);
    }

    // MAIN METHOD (Simulation)
    public static void main(String[] args) {
        FlashSaleInventoryManager manager = new FlashSaleInventoryManager();

        String product = "IPHONE15_256GB";
        manager.addProduct(product, 5); // small number for testing

        // Simulate multiple users
        for (int i = 1; i <= 10; i++) {
            int userId = i;
            System.out.println("User " + userId + ": " +
                    manager.purchaseItem(product, userId));
        }

        System.out.println("Final Stock: " + manager.checkStock(product));
        System.out.println("Waiting List: " + manager.getWaitingList(product));
    }
}