import java.util.*;

public class BookMyStayApp {

    public static void main(String[] args) {

        System.out.println("=================================");
        System.out.println(" Book My Stay Application v6.1 ");
        System.out.println(" Reservation Confirmation ");
        System.out.println("=================================");

        // Room objects
        Room single = new SingleRoom();
        Room doubleRoom = new DoubleRoom();
        Room suite = new SuiteRoom();

        // Inventory
        RoomInventory inventory = new RoomInventory();

        // Search
        RoomSearchService search = new RoomSearchService(inventory);
        search.searchAvailableRooms(single, doubleRoom, suite);

        // Booking Queue
        BookingRequestQueue queue = new BookingRequestQueue();

        queue.addRequest(new Reservation("Alice", "Single Room"));
        queue.addRequest(new Reservation("Bob", "Double Room"));
        queue.addRequest(new Reservation("Charlie", "Suite Room"));

        // Allocation Service
        BookingService bookingService = new BookingService(inventory);

        bookingService.processBookings(queue);
    }
}

//////////////////////////////////////////////////////
// ROOM DOMAIN
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

    Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
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
// BOOKING SERVICE (UC6)
//////////////////////////////////////////////////////

class BookingService {

    private RoomInventory inventory;

    // Track allocated rooms
    private HashMap<String, Set<String>> allocatedRooms;

    BookingService(RoomInventory inventory) {

        this.inventory = inventory;
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