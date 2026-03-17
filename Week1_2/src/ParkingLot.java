import java.util.*;

public class ParkingLot {

    enum Status {
        EMPTY, OCCUPIED, DELETED
    }

    static class ParkingSpot {
        String licensePlate;
        long entryTime;
        Status status;

        ParkingSpot() {
            this.status = Status.EMPTY;
        }
    }

    private ParkingSpot[] table;
    private int capacity;
    private int size;

    // stats
    private int totalProbes = 0;
    private int totalOperations = 0;

    public ParkingLot(int capacity) {
        this.capacity = capacity;
        this.table = new ParkingSpot[capacity];
        this.size = 0;

        for (int i = 0; i < capacity; i++) {
            table[i] = new ParkingSpot();
        }
    }

    // Hash function
    private int hash(String licensePlate) {
        return Math.abs(licensePlate.hashCode()) % capacity;
    }

    // Park vehicle
    public void parkVehicle(String licensePlate) {
        int index = hash(licensePlate);
        int probes = 0;

        while (table[index].status == Status.OCCUPIED) {
            index = (index + 1) % capacity; // linear probing
            probes++;
        }

        table[index].licensePlate = licensePlate;
        table[index].entryTime = System.currentTimeMillis();
        table[index].status = Status.OCCUPIED;

        size++;
        totalProbes += probes;
        totalOperations++;

        System.out.println("Vehicle " + licensePlate +
                " parked at spot #" + index +
                " (" + probes + " probes)");
    }

    // Exit vehicle
    public void exitVehicle(String licensePlate) {
        int index = hash(licensePlate);
        int probes = 0;

        while (table[index].status != Status.EMPTY) {
            if (table[index].status == Status.OCCUPIED &&
                    table[index].licensePlate.equals(licensePlate)) {

                long durationMillis = System.currentTimeMillis() - table[index].entryTime;
                double hours = durationMillis / (1000.0 * 60 * 60);

                double fee = hours * 5; // $5 per hour

                table[index].status = Status.DELETED;
                size--;

                System.out.println("Vehicle " + licensePlate + " exited from spot #" + index);
                System.out.println("Duration: " + String.format("%.2f", hours) + " hrs");
                System.out.println("Fee: $" + String.format("%.2f", fee));
                return;
            }

            index = (index + 1) % capacity;
            probes++;
        }

        System.out.println("Vehicle not found!");
    }

    // Find nearest available spot
    public int findNearestSpot() {
        for (int i = 0; i < capacity; i++) {
            if (table[i].status != Status.OCCUPIED) {
                return i;
            }
        }
        return -1;
    }

    // Statistics
    public void getStatistics() {
        double occupancy = (size * 100.0) / capacity;
        double avgProbes = totalOperations == 0 ? 0 :
                (totalProbes * 1.0) / totalOperations;

        System.out.println("\n===== Parking Stats =====");
        System.out.println("Occupancy: " + String.format("%.2f", occupancy) + "%");
        System.out.println("Average Probes: " + String.format("%.2f", avgProbes));
    }

    // MAIN METHOD
    public static void main(String[] args) throws InterruptedException {
        ParkingLot lot = new ParkingLot(10);

        lot.parkVehicle("ABC-1234");
        lot.parkVehicle("ABC-1235");
        lot.parkVehicle("XYZ-9999");

        Thread.sleep(2000);

        lot.exitVehicle("ABC-1234");

        lot.getStatistics();

        System.out.println("Nearest Available Spot: " + lot.findNearestSpot());
    }
}