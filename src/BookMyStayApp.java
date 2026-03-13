import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class BookMyStayApp {

    public static void main(String[] args) {

        System.out.println("=================================");
        System.out.println(" Book My Stay Application v5.1 ");
        System.out.println(" Booking Request Queue ");
        System.out.println("=================================");

        // Room domain objects
        Room single = new SingleRoom();
        Room doubleRoom = new DoubleRoom();
        Room suite = new SuiteRoom();

        // Inventory (UC3)
        RoomInventory inventory = new RoomInventory();

        // Search service (UC4)
        RoomSearchService search = new RoomSearchService(inventory);

        search.searchAvailableRooms(single, doubleRoom, suite);

        // Booking Queue (UC5)
        BookingRequestQueue bookingQueue = new BookingRequestQueue();

        System.out.println("\n--- Booking Requests ---");

        bookingQueue.addRequest(new Reservation("Alice", "Single Room"));
        bookingQueue.addRequest(new Reservation("Bob", "Double Room"));
        bookingQueue.addRequest(new Reservation("Charlie", "Suite Room"));

        bookingQueue.displayRequests();
    }
}

/////////////////////////////////////////////////////////
// ABSTRACT ROOM
/////////////////////////////////////////////////////////

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

/////////////////////////////////////////////////////////
// ROOM TYPES
/////////////////////////////////////////////////////////

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

/////////////////////////////////////////////////////////
// UC3 INVENTORY
/////////////////////////////////////////////////////////

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
}

/////////////////////////////////////////////////////////
// UC4 SEARCH SERVICE
/////////////////////////////////////////////////////////

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

/////////////////////////////////////////////////////////
// UC5 RESERVATION
/////////////////////////////////////////////////////////

class Reservation {

    String guestName;
    String roomType;

    Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    void display() {
        System.out.println("Guest: " + guestName + " | Requested Room: " + roomType);
    }
}

/////////////////////////////////////////////////////////
// UC5 BOOKING QUEUE
/////////////////////////////////////////////////////////

class BookingRequestQueue {

    private Queue<Reservation> queue;

    BookingRequestQueue() {
        queue = new LinkedList<>();
    }

    void addRequest(Reservation reservation) {

        queue.add(reservation);

        System.out.println("Booking request added for " + reservation.guestName);
    }

    void displayRequests() {

        System.out.println("\nRequests in Queue (FIFO Order):\n");

        for (Reservation r : queue) {
            r.display();
        }
    }
}