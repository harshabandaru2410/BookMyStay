import java.util.HashMap;

public class BookMyStayApp {

    public static void main(String[] args) {

        // UC1 - Welcome Message
        System.out.println("=================================");
        System.out.println(" Welcome to BookMyStay ");
        System.out.println(" Hotel Booking System v3.1 ");
        System.out.println("=================================");

        // UC2 - Room Objects
        Room single = new SingleRoom();
        Room doubleRoom = new DoubleRoom();
        Room suite = new SuiteRoom();

        // UC3 - Centralized Inventory
        RoomInventory inventory = new RoomInventory();

        // Display Room Details with Inventory
        System.out.println("\nSingle Room Details");
        single.display();
        System.out.println("Available: " + inventory.getAvailability("Single Room"));

        System.out.println("\nDouble Room Details");
        doubleRoom.display();
        System.out.println("Available: " + inventory.getAvailability("Double Room"));

        System.out.println("\nSuite Room Details");
        suite.display();
        System.out.println("Available: " + inventory.getAvailability("Suite Room"));

        // Display full inventory
        inventory.displayInventory();
    }
}


// ---------------------------
// Abstract Room Class (UC2)
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
// UC3 - Inventory Management
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

    void updateAvailability(String roomType, int count) {
        inventory.put(roomType, count);
    }

    void displayInventory() {

        System.out.println("\nCurrent Room Inventory:");

        for (String room : inventory.keySet()) {
            System.out.println(room + " : " + inventory.get(room));
        }
    }
}