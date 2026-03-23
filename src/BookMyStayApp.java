import java.util.*;

public class BookMyStayApp {

    public static void main(String[] args) {

        System.out.println("=================================");
        System.out.println(" Book My Stay Application v11.0 ");
        System.out.println(" Full System (UC1 - UC11) ");
        System.out.println("=================================");

        // -------------------------------
        // UC1–UC3: Setup
        // -------------------------------
        RoomInventory inventory = new RoomInventory();

        // -------------------------------
        // UC4: Search
        // -------------------------------
        new RoomSearchService(inventory)
                .searchAvailableRooms(new SingleRoom(), new DoubleRoom(), new SuiteRoom());

        // -------------------------------
        // UC5: Queue
        // -------------------------------
        BookingRequestQueue queue = new BookingRequestQueue();
        queue.addRequest(new Reservation("Alice", "Single Room"));
        queue.addRequest(new Reservation("Bob", "Double Room"));
        queue.addRequest(new Reservation("", "Single Room")); // invalid
        queue.addRequest(new Reservation("Eve", "Suite Room"));

        // -------------------------------
        // UC8 + UC9
        // -------------------------------
        BookingHistory history = new BookingHistory();
        BookingValidator validator = new BookingValidator();

        // -------------------------------
        // UC6: Booking
        // -------------------------------
        BookingService bookingService =
                new BookingService(inventory, history, validator);
        bookingService.processBookings(queue);

        // -------------------------------
        // UC7: Add-ons
        // -------------------------------
        AddOnServiceManager addOnManager = new AddOnServiceManager();
        String resId = "RES101";

        addOnManager.addService(resId, new AddOnService("Breakfast", 20));
        addOnManager.addService(resId, new AddOnService("Spa", 50));

        System.out.println("\nAdd-On Services:");
        addOnManager.displayServices(resId);

        // -------------------------------
        // UC10: Cancellation
        // -------------------------------
        CancellationService cancelService =
                new CancellationService(inventory, history);

        if (!history.getAllReservations().isEmpty()) {
            cancelService.cancelReservation(history.getAllReservations().get(0));
        }

        // -------------------------------
        // UC8: Report
        // -------------------------------
        new BookingReportService()
                .generateReport(history.getAllReservations());

        // -------------------------------
        // UC11: Concurrency Simulation
        // -------------------------------
        System.out.println("\n=== Concurrent Booking Simulation ===");

        BookingQueue sharedQueue = new BookingQueue();

        sharedQueue.addRequest(new Reservation("User1", "Single Room"));
        sharedQueue.addRequest(new Reservation("User2", "Single Room"));
        sharedQueue.addRequest(new Reservation("User3", "Single Room"));

        BookingProcessor t1 = new BookingProcessor(sharedQueue, inventory, "T1");
        BookingProcessor t2 = new BookingProcessor(sharedQueue, inventory, "T2");

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {}

        System.out.println("\nSystem execution completed.");
    }
}

//////////////////////////////////////////////////////
// ROOMS (UC1, UC2)
//////////////////////////////////////////////////////

abstract class Room {
    String roomType;
    int beds;
    double price;

    Room(String type, int beds, double price) {
        this.roomType = type;
        this.beds = beds;
        this.price = price;
    }

    void display() {
        System.out.println(roomType + " | Beds: " + beds + " | $" + price);
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

    private Map<String, Integer> map = new HashMap<>();

    RoomInventory() {
        map.put("Single Room", 2);
        map.put("Double Room", 2);
        map.put("Suite Room", 1);
    }

    synchronized int getAvailability(String type) {
        return map.getOrDefault(type, 0);
    }

    synchronized void decreaseAvailability(String type) throws Exception {
        if (getAvailability(type) <= 0)
            throw new Exception("No availability");
        map.put(type, getAvailability(type) - 1);
    }

    synchronized void increaseAvailability(String type) {
        map.put(type, getAvailability(type) + 1);
    }
}

//////////////////////////////////////////////////////
// SEARCH (UC4)
//////////////////////////////////////////////////////

class RoomSearchService {
    private RoomInventory inventory;

    RoomSearchService(RoomInventory inv) { this.inventory = inv; }

    void searchAvailableRooms(Room... rooms) {
        System.out.println("\nAvailable Rooms:");
        for (Room r : rooms) {
            int a = inventory.getAvailability(r.roomType);
            if (a > 0) {
                r.display();
                System.out.println("Available: " + a);
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

    Reservation(String g, String t) {
        guestName = g; roomType = t;
    }

    Reservation(String g, String t, String id) {
        guestName = g; roomType = t; roomId = id;
    }

    public String toString() {
        return guestName + " | " + roomType + " | " + roomId;
    }
}

//////////////////////////////////////////////////////
// QUEUE (UC5)
//////////////////////////////////////////////////////

class BookingRequestQueue {
    Queue<Reservation> q = new LinkedList<>();

    void addRequest(Reservation r) { q.add(r); }
    boolean hasRequests() { return !q.isEmpty(); }
    Reservation getNextRequest() { return q.poll(); }
}

//////////////////////////////////////////////////////
// HISTORY (UC8)
//////////////////////////////////////////////////////

class BookingHistory {
    List<Reservation> list = new ArrayList<>();

    void addReservation(Reservation r) { list.add(r); }
    void removeReservation(Reservation r) { list.remove(r); }
    List<Reservation> getAllReservations() { return list; }
}

//////////////////////////////////////////////////////
// VALIDATION (UC9)
//////////////////////////////////////////////////////

class BookingValidator {

    Set<String> valid = new HashSet<>(
            Arrays.asList("Single Room","Double Room","Suite Room"));

    void validate(Reservation r, int a) throws Exception {
        if (r.guestName == null || r.guestName.isEmpty())
            throw new Exception("Invalid name");
        if (!valid.contains(r.roomType))
            throw new Exception("Invalid room");
        if (a <= 0)
            throw new Exception("No availability");
    }
}

//////////////////////////////////////////////////////
// BOOKING (UC6)
//////////////////////////////////////////////////////

class BookingService {

    RoomInventory inv;
    BookingHistory hist;
    BookingValidator val;

    BookingService(RoomInventory i, BookingHistory h, BookingValidator v) {
        inv=i; hist=h; val=v;
    }

    void processBookings(BookingRequestQueue q) {

        while (q.hasRequests()) {

            Reservation r = q.getNextRequest();

            try {
                val.validate(r, inv.getAvailability(r.roomType));

                String id = genId(r.roomType);
                inv.decreaseAvailability(r.roomType);

                Reservation c = new Reservation(r.guestName, r.roomType, id);
                hist.addReservation(c);

                System.out.println("Booked: " + c);

            } catch (Exception e) {
                System.out.println("Failed: " + e.getMessage());
            }
        }
    }

    String genId(String t) {
        return t.substring(0,2).toUpperCase()
                + (new Random().nextInt(900)+100);
    }
}

//////////////////////////////////////////////////////
// ADD-ON (UC7)
//////////////////////////////////////////////////////

class AddOnService {
    String name; double cost;
    AddOnService(String n,double c){name=n;cost=c;}
    public String toString(){return name+" $"+cost;}
}

class AddOnServiceManager {
    Map<String,List<AddOnService>> map=new HashMap<>();

    void addService(String id,AddOnService s){
        map.putIfAbsent(id,new ArrayList<>());
        map.get(id).add(s);
    }

    void displayServices(String id){
        for(AddOnService s: map.getOrDefault(id,new ArrayList<>()))
            System.out.println(s);
    }
}

//////////////////////////////////////////////////////
// REPORT (UC8)
//////////////////////////////////////////////////////

class BookingReportService {
    void generateReport(List<Reservation> list){
        System.out.println("\nREPORT:");
        for(Reservation r:list) System.out.println(r);
        System.out.println("Total: "+list.size());
    }
}

//////////////////////////////////////////////////////
// CANCELLATION (UC10)
//////////////////////////////////////////////////////

class CancellationService {

    RoomInventory inv;
    BookingHistory hist;
    Stack<String> stack = new Stack<>();

    CancellationService(RoomInventory i, BookingHistory h){
        inv=i; hist=h;
    }

    void cancelReservation(Reservation r){

        if(!hist.getAllReservations().contains(r)){
            System.out.println("Cancel failed");
            return;
        }

        stack.push(r.roomId);
        hist.removeReservation(r);
        inv.increaseAvailability(r.roomType);

        System.out.println("Cancelled: "+r);
    }
}

//////////////////////////////////////////////////////
// CONCURRENCY (UC11)
//////////////////////////////////////////////////////

class BookingQueue {
    Queue<Reservation> q=new LinkedList<>();

    synchronized void addRequest(Reservation r){ q.add(r); }
    synchronized Reservation get(){ return q.poll(); }
    synchronized boolean has(){ return !q.isEmpty(); }
}

class BookingProcessor extends Thread {

    BookingQueue q;
    RoomInventory inv;

    BookingProcessor(BookingQueue q, RoomInventory i,String name){
        super(name);
        this.q=q; this.inv=i;
    }

    public void run(){

        while(true){

            Reservation r;

            synchronized(q){
                if(!q.has()) break;
                r=q.get();
            }

            if(r!=null){
                try{
                    inv.decreaseAvailability(r.roomType);
                    System.out.println(getName()+" booked "+r.roomType);
                }catch(Exception e){
                    System.out.println(getName()+" failed");
                }
            }
        }
    }
}