public class BookMyStayApp {

    public static void main(String[] args) {

        // UC1 - Welcome Message
        System.out.println("=================================");
        System.out.println(" Welcome to BookMyStay ");
        System.out.println(" Hotel Booking System v1.0 ");
        System.out.println("=================================");

        // UC2 - Room Initialization

        Room single = new SingleRoom();
        Room doubleRoom = new DoubleRoom();
        Room suite = new SuiteRoom();

        int singleAvailable = 5;
        int doubleAvailable = 3;
        int suiteAvailable = 2;

        System.out.println("\nSingle Room Details");
        single.display();
        System.out.println("Available: " + singleAvailable);

        System.out.println("\nDouble Room Details");
        doubleRoom.display();
        System.out.println("Available: " + doubleAvailable);

        System.out.println("\nSuite Room Details");
        suite.display();
        System.out.println("Available: " + suiteAvailable);
    }
}

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
        super("Single Room",1,100);
    }
}

class DoubleRoom extends Room {
    DoubleRoom() {
        super("Double Room",2,180);
    }
}

class SuiteRoom extends Room {
    SuiteRoom() {
        super("Suite Room",3,350);
    }
}