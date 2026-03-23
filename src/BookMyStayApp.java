import java.util.*;

public class BookMyStayApp {

    public static void main(String[] args) {

        System.out.println("=================================");
        System.out.println(" Book My Stay Application v8.0 ");
        System.out.println(" Full System (UC1 - UC8) ");
        System.out.println("=================================");

        // -------------------------------
        // UC1 & UC2: Room Types
        // -------------------------------
        Room single = new SingleRoom();
        Room doubleRoom = new DoubleRoom();
        Room suite = new SuiteRoom();

        // -------------------------------
        // UC3: Inventory
        // -------------------------------
        RoomInventory inventory = new RoomInventory();

        // -------------------------------
        // UC4: Search
        // -------------------------------
        RoomSearchService search = new RoomSearchService(inventory);
        search.searchAvailableRooms(single, doubleRoom, suite);

        // -------------------------------
        // UC5: Booking Queue
        // -------------------------------
        BookingRequestQueue queue = new BookingRequestQueue();

        queue.addRequest(new Reservation("Alice", "Single Room"));
        queue.addRequest(new Reservation("Bob", "Double Room"));
        queue.addRequest(new Reservation("Charlie", "Suite Room"));

        // -------------------------------
        // UC8: Booking History
        // -------------------------------
        BookingHistory history = new BookingHistory();

        // -------------------------------
        // UC6: Booking Service
        // -------------------------------
        BookingService bookingService = new BookingService(inventory, history);
        bookingService.processBookings(queue);

        // -------------------------------
        // UC7: Add-On Services
        // -------------------------------
        System.out.println("\n=== Add-On Services ===");

        String reservationId = "RES101";

        AddOnServiceManager manager = new AddOnServiceManager();

        manager.addService(reservationId, new AddOnService("Breakfast", 20));
        manager.addService(reservationId, new AddOnService("Spa", 50));
        manager.addService(reservationId, new AddOnService("Airport Pickup", 30));

        manager.displayServices(reservationId);

        double total = manager.calculateTotalCost(reservationId);
        System.out.println("Total Add-On Cost: $" + total);

        // -------------------------------
        // UC8: Reporting
        // -------------------------------
        BookingReportService reportService = new BookingReportService();
        reportService.generateReport(history.getAllReservations());
    }
}

//////////////////////////////////////////////////////
// ROOM DOMAIN (UC1, UC2)
//////////////////////////////////////////////////////

abstract class Room {

    String roomType;
    int beds;
    double price;

    Room(String roomType, int beds, double price) {
        this.roomType = roomType;
        this.beds = beds;
        this.price = price;
    }

    void display() {
        System.out.println("Room Type: " + roomType);
        System.out.println("Beds: " + beds);
        System.out.println("Price: $" + price);
    }
}

class SingleRoom extends Room {
    SingleRoom() {
        super("Single Room", 1, 100);
    }
}

class DoubleRoom extends Room {
    DoubleRoom() {
        super("Double Room", 2, 180);
    }
}

class SuiteRoom extends Room {
    SuiteRoom() {
        super("Suite Room", 3, 350);
    }
}

//////////////////////////////////////////////////////
// INVENTORY (UC3)
//////////////////////////////////////////////////////

class RoomInventory {

    private HashMap<String, Integer> inventory;

    RoomInventory() {
        inventory = new HashMap<>();
        inventory.put("Single Room", 5);
        inventory.put("Double Room", 3);
        inventory.put("Suite Room", 2);
    }

    int getAvailability(String roomType) {
        return inventory.get(roomType);
    }

    void decreaseAvailability(String roomType) {
        inventory.put(roomType, inventory.get(roomType) - 1);
    }
}

//////////////////////////////////////////////////////
// SEARCH SERVICE (UC4)
//////////////////////////////////////////////////////

class RoomSearchService {

    private RoomInventory inventory;

    RoomSearchService(RoomInventory inventory) {
        this.inventory = inventory;
    }

    void searchAvailableRooms(Room... rooms) {

        System.out.println("\nAvailable Rooms:\n");

        for (Room room : rooms) {

            int availability = inventory.getAvailability(room.roomType);

            if (availability > 0) {

                room.display();
                System.out.println("Available: " + availability);
                System.out.println("---------------------");
            }
        }
    }
}

//////////////////////////////////////////////////////
// RESERVATION (UC5)
//////////////////////////////////////////////////////

class Reservation {

    String guestName;
    String roomType;
    String roomId;

    Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    Reservation(String guestName, String roomType, String roomId) {
        this.guestName = guestName;
        this.roomType = roomType;
        this.roomId = roomId;
    }

    public String toString() {
        return "Guest: " + guestName +
                ", Room Type: " + roomType +
                ", Room ID: " + roomId;
    }
}

//////////////////////////////////////////////////////
// BOOKING QUEUE (UC5)
//////////////////////////////////////////////////////

class BookingRequestQueue {

    Queue<Reservation> queue = new LinkedList<>();

    void addRequest(Reservation r) {
        queue.add(r);
        System.out.println("Booking request added for " + r.guestName);
    }

    Reservation getNextRequest() {
        return queue.poll();
    }

    boolean hasRequests() {
        return !queue.isEmpty();
    }
}

//////////////////////////////////////////////////////
// BOOKING HISTORY (UC8)
//////////////////////////////////////////////////////

class BookingHistory {

    private List<Reservation> history = new ArrayList<>();

    void addReservation(Reservation r) {
        history.add(r);
    }

    List<Reservation> getAllReservations() {
        return history;
    }
}

//////////////////////////////////////////////////////
// BOOKING SERVICE (UC6)
//////////////////////////////////////////////////////

class BookingService {

    private RoomInventory inventory;
    private BookingHistory history;

    private HashMap<String, Set<String>> allocatedRooms;

    BookingService(RoomInventory inventory, BookingHistory history) {
        this.inventory = inventory;
        this.history = history;
        allocatedRooms = new HashMap<>();
    }

    void processBookings(BookingRequestQueue queue) {

        System.out.println("\n--- Processing Booking Requests ---\n");

        while (queue.hasRequests()) {

            Reservation r = queue.getNextRequest();

            int availability = inventory.getAvailability(r.roomType);

            if (availability > 0) {

                String roomId = generateRoomId(r.roomType);

                allocatedRooms.putIfAbsent(r.roomType, new HashSet<>());
                allocatedRooms.get(r.roomType).add(roomId);

                inventory.decreaseAvailability(r.roomType);

                // Store confirmed booking (UC8)
                history.addReservation(new Reservation(r.guestName, r.roomType, roomId));

                System.out.println("Reservation Confirmed");
                System.out.println("Guest: " + r.guestName);
                System.out.println("Room Type: " + r.roomType);
                System.out.println("Assigned Room ID: " + roomId);
                System.out.println("---------------------");

            } else {
                System.out.println("No rooms available for " + r.guestName);
            }
        }
    }

    private String generateRoomId(String roomType) {

        int number = new Random().nextInt(900) + 100;

        String prefix = roomType.replace(" ", "").substring(0, 2).toUpperCase();

        return prefix + number;
    }
}

//////////////////////////////////////////////////////
// ADD-ON SERVICES (UC7)
//////////////////////////////////////////////////////

class AddOnService {

    String name;
    double cost;

    AddOnService(String name, double cost) {
        this.name = name;
        this.cost = cost;
    }

    public String toString() {
        return name + " ($" + cost + ")";
    }
}

class AddOnServiceManager {

    private HashMap<String, List<AddOnService>> serviceMap = new HashMap<>();

    void addService(String reservationId, AddOnService service) {
        serviceMap.putIfAbsent(reservationId, new ArrayList<>());
        serviceMap.get(reservationId).add(service);
    }

    void displayServices(String reservationId) {

        List<AddOnService> services = serviceMap.get(reservationId);

        if (services == null || services.isEmpty()) {
            System.out.println("No add-on services.");
            return;
        }

        for (AddOnService s : services) {
            System.out.println("- " + s);
        }
    }

    double calculateTotalCost(String reservationId) {

        double total = 0;

        List<AddOnService> services = serviceMap.get(reservationId);

        if (services != null) {
            for (AddOnService s : services) {
                total += s.cost;
            }
        }

        return total;
    }
}

//////////////////////////////////////////////////////
// REPORT SERVICE (UC8)
//////////////////////////////////////////////////////

class BookingReportService {

    void generateReport(List<Reservation> reservations) {

        System.out.println("\n===== Booking Report =====");

        if (reservations.isEmpty()) {
            System.out.println("No bookings found.");
            return;
        }

        for (Reservation r : reservations) {
            System.out.println(r);
        }

        System.out.println("\nTotal Bookings: " + reservations.size());
    }
}