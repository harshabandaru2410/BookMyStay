import java.util.HashMap;

public class UseCase4RoomSearch {

    public static void main(String[] args) {

        System.out.println("=================================");
        System.out.println(" Book My Stay Application v4.1 ");
        System.out.println(" Room Search & Availability ");
        System.out.println("=================================");

        // Room domain objects
        Room single = new SingleRoom();
        Room doubleRoom = new DoubleRoom();
        Room suite = new SuiteRoom();

        // Centralized inventory (UC3)
        RoomInventory inventory = new RoomInventory();

        // Search Service
        RoomSearchService search = new RoomSearchService(inventory);

        // Perform search (UC4)
        search.searchAvailableRooms(single, doubleRoom, suite);
    }
}


// ---------------------------
// Abstract Room Class
// ---------------------------
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


// ---------------------------
// Room Implementations
// ---------------------------
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


// ---------------------------
// UC3 Inventory Management
// ---------------------------
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


// ---------------------------
// UC4 Search Service
// ---------------------------
class RoomSearchService {

    private RoomInventory inventory;

    RoomSearchService(RoomInventory inventory) {
        this.inventory = inventory;
    }

    void searchAvailableRooms(Room... rooms) {

        System.out.println("\nAvailable Rooms:\n");

        for (Room room : rooms) {

            int availability = inventory.getAvailability(room.roomType);

            // Defensive check
            if (availability > 0) {

                room.display();
                System.out.println("Available: " + availability);
                System.out.println("---------------------");
            }
        }
    }
}