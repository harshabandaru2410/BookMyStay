import java.util.*;

public class BookMyStayApp {

    public static void main(String[] args) {

        System.out.println("=================================");
        System.out.println(" Book My Stay Application v10.0 ");
        System.out.println(" Full System (UC1 - UC10) ");
        System.out.println("=================================");

        // UC1 & UC2: Rooms
        Room single = new SingleRoom();
        Room doubleRoom = new DoubleRoom();
        Room suite = new SuiteRoom();

        // UC3: Inventory
        RoomInventory inventory = new RoomInventory();

        // UC4: Search
        RoomSearchService search = new RoomSearchService(inventory);
        search.searchAvailableRooms(single, doubleRoom, suite);

        // UC5: Booking Queue
        BookingRequestQueue queue = new BookingRequestQueue();
        queue.addRequest(new Reservation("Alice", "Single Room"));
        queue.addRequest(new Reservation("Bob", "Double Room"));
        queue.addRequest(new Reservation("", "Single Room")); // invalid
        queue.addRequest(new Reservation("David", "Luxury Room")); // invalid

        // UC8: History
        BookingHistory history = new BookingHistory();

        // UC9: Validator
        BookingValidator validator = new BookingValidator();

        // UC6 + UC9: Booking Service
        BookingService bookingService = new BookingService(inventory, history, validator);
        bookingService.processBookings(queue);

        // UC7: Add-On Services
        System.out.println("\n=== Add-On Services ===");

        String reservationId = "RES101";
        AddOnServiceManager manager = new AddOnServiceManager();

        manager.addService(reservationId, new AddOnService("Breakfast", 20));
        manager.addService(reservationId, new AddOnService("Spa", 50));

        manager.displayServices(reservationId);
        System.out.println("Total Add-On Cost: $" + manager.calculateTotalCost(reservationId));

        // UC10: Cancellation
        CancellationService cancellationService = new CancellationService(inventory, history);

        if (!history.getAllReservations().isEmpty()) {
            Reservation toCancel = history.getAllReservations().get(0);
            cancellationService.cancelReservation(toCancel);
        }

        // UC8: Report
        BookingReportService reportService = new BookingReportService();
        reportService.generateReport(history.getAllReservations());

        System.out.println("\nSystem completed with full lifecycle handling.");
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
        System.out.println(roomType + " | Beds: " + beds + " | Price: $" + price);
    }
}

class SingleRoom extends Room {
    SingleRoom() { super("Single Room", 1, 100); }
}

class DoubleRoom extends Room {
    DoubleRoom() { super("Double Room", 2, 180); }
}

class SuiteRoom extends Room {
    SuiteRoom() { super("Suite Room", 3, 350); }
}

//////////////////////////////////////////////////////
// INVENTORY (UC3)
//////////////////////////////////////////////////////

class RoomInventory {

    private Map<String, Integer> inventory = new HashMap<>();

    RoomInventory() {
        inventory.put("Single Room", 2);
        inventory.put("Double Room", 2);
        inventory.put("Suite Room", 1);
    }

    int getAvailability(String type) {
        return inventory.getOrDefault(type, 0);
    }

    void decreaseAvailability(String type) throws InvalidBookingException {
        if (getAvailability(type) <= 0)
            throw new InvalidBookingException("Inventory underflow");
        inventory.put(type, getAvailability(type) - 1);
    }

    void increaseAvailability(String type) {
        inventory.put(type, getAvailability(type) + 1);
    }
}

//////////////////////////////////////////////////////
// SEARCH (UC4)
//////////////////////////////////////////////////////

class RoomSearchService {

    private RoomInventory inventory;

    RoomSearchService(RoomInventory inventory) {
        this.inventory = inventory;
    }

    void searchAvailableRooms(Room... rooms) {
        System.out.println("\nAvailable Rooms:");
        for (Room r : rooms) {
            int avail = inventory.getAvailability(r.roomType);
            if (avail > 0) {
                r.display();
                System.out.println("Available: " + avail);
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
        return guestName + " | " + roomType + " | " + roomId;
    }
}

//////////////////////////////////////////////////////
// QUEUE (UC5)
//////////////////////////////////////////////////////

class BookingRequestQueue {

    Queue<Reservation> queue = new LinkedList<>();

    void addRequest(Reservation r) {
        queue.add(r);
    }

    boolean hasRequests() {
        return !queue.isEmpty();
    }

    Reservation getNextRequest() {
        return queue.poll();
    }
}

//////////////////////////////////////////////////////
// HISTORY (UC8)
//////////////////////////////////////////////////////

class BookingHistory {

    private List<Reservation> confirmed = new ArrayList<>();

    void addReservation(Reservation r) {
        confirmed.add(r);
    }

    void removeReservation(Reservation r) {
        confirmed.remove(r);
    }

    List<Reservation> getAllReservations() {
        return confirmed;
    }
}

//////////////////////////////////////////////////////
// VALIDATION (UC9)
//////////////////////////////////////////////////////

class InvalidBookingException extends Exception {
    InvalidBookingException(String msg) { super(msg); }
}

class BookingValidator {

    Set<String> validTypes = new HashSet<>(
            Arrays.asList("Single Room", "Double Room", "Suite Room"));

    void validate(Reservation r, int availability) throws InvalidBookingException {

        if (r.guestName == null || r.guestName.trim().isEmpty())
            throw new InvalidBookingException("Invalid guest name");

        if (!validTypes.contains(r.roomType))
            throw new InvalidBookingException("Invalid room type");

        if (availability <= 0)
            throw new InvalidBookingException("No availability");
    }
}

//////////////////////////////////////////////////////
// BOOKING SERVICE (UC6 + UC9)
//////////////////////////////////////////////////////

class BookingService {

    private RoomInventory inventory;
    private BookingHistory history;
    private BookingValidator validator;

    BookingService(RoomInventory inventory, BookingHistory history, BookingValidator validator) {
        this.inventory = inventory;
        this.history = history;
        this.validator = validator;
    }

    void processBookings(BookingRequestQueue queue) {

        System.out.println("\nProcessing Bookings:");

        while (queue.hasRequests()) {

            Reservation r = queue.getNextRequest();

            try {
                int avail = inventory.getAvailability(r.roomType);
                validator.validate(r, avail);

                String roomId = generateRoomId(r.roomType);

                inventory.decreaseAvailability(r.roomType);

                Reservation confirmed = new Reservation(r.guestName, r.roomType, roomId);
                history.addReservation(confirmed);

                System.out.println("Confirmed: " + confirmed);

            } catch (InvalidBookingException e) {
                System.out.println("Failed: " + e.getMessage());
            }
        }
    }

    private String generateRoomId(String type) {
        return type.substring(0, 2).toUpperCase() + (new Random().nextInt(900) + 100);
    }
}

//////////////////////////////////////////////////////
// ADD-ON (UC7)
//////////////////////////////////////////////////////

class AddOnService {
    String name;
    double cost;

    AddOnService(String name, double cost) {
        this.name = name;
        this.cost = cost;
    }

    public String toString() {
        return name + " $" + cost;
    }
}

class AddOnServiceManager {

    Map<String, List<AddOnService>> map = new HashMap<>();

    void addService(String id, AddOnService s) {
        map.putIfAbsent(id, new ArrayList<>());
        map.get(id).add(s);
    }

    void displayServices(String id) {
        System.out.println("Services:");
        for (AddOnService s : map.getOrDefault(id, new ArrayList<>()))
            System.out.println("- " + s);
    }

    double calculateTotalCost(String id) {
        return map.getOrDefault(id, new ArrayList<>())
                .stream().mapToDouble(s -> s.cost).sum();
    }
}

//////////////////////////////////////////////////////
// REPORT (UC8)
//////////////////////////////////////////////////////

class BookingReportService {

    void generateReport(List<Reservation> list) {

        System.out.println("\n=== REPORT ===");

        for (Reservation r : list)
            System.out.println(r);

        System.out.println("Total: " + list.size());
    }
}

//////////////////////////////////////////////////////
// CANCELLATION (UC10)
//////////////////////////////////////////////////////

class CancellationService {

    private RoomInventory inventory;
    private BookingHistory history;
    private Stack<String> rollbackStack = new Stack<>();

    CancellationService(RoomInventory inventory, BookingHistory history) {
        this.inventory = inventory;
        this.history = history;
    }

    void cancelReservation(Reservation r) {

        if (!history.getAllReservations().contains(r)) {
            System.out.println("Cancellation failed.");
            return;
        }

        rollbackStack.push(r.roomId);

        history.removeReservation(r);
        inventory.increaseAvailability(r.roomType);

        System.out.println("Cancelled: " + r);
    }
}