import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/** Console user interface. Every menu action delegates to the OOP controller. */
public class K2703387_Main {
    private static final Scanner INPUT = new Scanner(System.in);
    private static final K2703387_SmartStayAdmin ADMIN =
            new K2703387_SmartStayAdmin("ADMIN-01");

    public static void main(String[] args) {
        seedDemoRooms();
        System.out.println("All records are held in memory until the program closes.");
        System.out.println("-----------Welcome to SmartStay.------------");

        while (true) {
            showMainMenu();
            int choice = readInt("Select an option: ");
            switch (choice) {
                case 1 -> runAdminMenu();
                case 2 -> runCustomerMenu();
                case 0 -> {
                    System.out.println("Thank you for using SmartStay. Goodbye!");
                    INPUT.close();
                    return;
                }
                default -> System.out.println("Please select 0, 1, or 2.");
            }
        }
    }

    // Administrator options //
    private static void runAdminMenu() {
        while (true) {
            showAdminMenu();
            int choice = readInt("Administrator option: ");
            if (choice == 0) return;

            try {
                switch (choice) {
                    case 1 -> addRoom();
                    case 2 -> updateRoom();
                    case 3 -> removeRoom();
                    case 4 -> setAvailability();
                    case 5 -> showRooms(ADMIN.viewRooms());
                    default -> System.out.println("Please select 0 to 5.");
                }
            } catch (IllegalArgumentException ex) {
                System.out.println("ERROR: " + ex.getMessage());
            }
        }
    }

    // Customer options delegate to the same controller defined in UML.
    private static void runCustomerMenu() {
        while (true) {
            showCustomerMenu();
            int choice = readInt("Customer option: ");
            if (choice == 0) return;

            try {
                switch (choice) {
                    case 1 -> registerGuest();
                    case 2 -> searchRooms();
                    case 3 -> bookRoom();
                    case 4 -> searchReservation();
                    case 5 -> viewCheckIns();
                    case 6 -> cancelBooking();
                    case 7 -> printInvoice();
                    default -> System.out.println("Please select 0 to 7.");
                }
            } catch (IllegalArgumentException ex) {
                System.out.println("ERROR: " + ex.getMessage());
            }
        }
    }

    private static void seedDemoRooms() {
        ADMIN.addRoom(new K2703387_StandardRoom("R101", K2703387_BedType.SINGLE));
        ADMIN.addRoom(new K2703387_DeluxeRoom("R201", K2703387_BedType.DOUBLE));
        ADMIN.addRoom(new K2703387_SuiteRoom("R301", K2703387_BedType.QUEEN));
        ADMIN.addRoom(new K2703387_FamilySuiteRoom("R401", K2703387_BedType.KING));
    }

    private static void showMainMenu() {
        System.out.println("\n-------- SMARTSTAY HOTEL------------");
        System.out.println("1. Administrator");
        System.out.println("2. Customer");
        System.out.println("0. Exit");
        System.out.println("****************************************");
    }

    private static void showAdminMenu() {
        System.out.println("\n-------- ADMINISTRATOR MENU --------");
        System.out.println("1. Add room");
        System.out.println("2. Update room bed type / daily rate");
        System.out.println("3. Remove room");
        System.out.println("4. Change room availability status");
        System.out.println("5. View all rooms");
        System.out.println("0. Back to main menu");
        System.out.println("****************************************");
    }

    private static void showCustomerMenu() {
        System.out.println("\n-------- CUSTOMER MENU --------");
        System.out.println("1. Register customer");
        System.out.println("2. Browse available rooms");
        System.out.println("3. Make reservation");
        System.out.println("4. Search reservations (name or booking ID)");
        System.out.println("5. View check-ins by date");
        System.out.println("6. Cancel reservation");
        System.out.println("7. Generate detailed invoice");
        System.out.println("0. Back to main menu");
        System.out.println("****************************************");
    }

    private static void addRoom() {
        String id = readText("New room ID: ");
        int type = chooseRoomType();
        K2703387_BedType bed = chooseBedType();
        K2703387_Room room = switch (type) {
            case 1 -> new K2703387_StandardRoom(id, bed);
            case 2 -> new K2703387_DeluxeRoom(id, bed);
            case 3 -> new K2703387_SuiteRoom(id, bed);
            case 4 -> new K2703387_FamilySuiteRoom(id, bed);
            default -> throw new IllegalArgumentException("Invalid room type.");
        };
        ADMIN.addRoom(room);
        System.out.println("Room saved: " + room);
    }

    private static void updateRoom() {
        String id = readText("Room ID: ");
        K2703387_BedType bed = chooseBedType();
        double rate = readDouble("New daily rate (LKR): ");
        System.out.println(ADMIN.updateRoom(id, bed, rate)
                ? "Room successfully updated."
                : "Update failed: ID missing, invalid rate, or future bookings exist.");
    }

    private static void removeRoom() {
        String id = readText("Room ID to remove: ");
        System.out.println(ADMIN.removeRoom(id) ? "Room removed." :
                "Cannot remove: room missing or has an active future/current booking.");
    }

    private static void setAvailability() {
        String id = readText("Room ID: ");
        System.out.println("1. AVAILABLE  2. RESERVED  3. UNDER_MAINTENANCE");
        int option = readInt("Status: ");
        if (option < 1 || option > 3) throw new IllegalArgumentException("Invalid status.");
        K2703387_RoomStatus status = K2703387_RoomStatus.values()[option - 1];
        System.out.println(ADMIN.setAvailability(id, status)
                ? "Status updated." : "Room was not found.");
    }

    private static void registerGuest() {
        String nic = readText("NIC/passport: ");
        String name = readText("Full name: ");
        String phone = readText("Phone (digits, optional +): ");
        String email = readText("Email: ");
        K2703387_Guest guest = new K2703387_Guest(nic, name, phone, email);
        ADMIN.addGuest(guest);
        System.out.println("Guest registered: " + guest);
    }

    private static void searchRooms() {
        System.out.println("1. By room type only");
        System.out.println("2. By room type and dates");
        System.out.println("3. By bed type and dates");
        int mode = readInt("Search option: ");
        List<K2703387_Room> results;
        if (mode == 1 || mode == 2) {
            String type = roomTypeName(chooseRoomType());
            if (mode == 1) results = ADMIN.searchRooms(type);
            else {
                LocalDate in = readDate("Check-in (YYYY-MM-DD): ");
                LocalDate out = readDate("Check-out (YYYY-MM-DD): ");
                results = ADMIN.searchRooms(type, in, out);
            }
        } else if (mode == 3) {
            K2703387_BedType bed = chooseBedType();
            LocalDate in = readDate("Check-in (YYYY-MM-DD): ");
            LocalDate out = readDate("Check-out (YYYY-MM-DD): ");
            results = ADMIN.searchRooms(in, out, bed);
        } else throw new IllegalArgumentException("Invalid search option.");
        showRooms(results);
    }

    private static void bookRoom() {
        String nic = readText("Registered guest NIC/passport: ");
        String id = readText("Room ID: ");
        LocalDate in = readDate("Check-in (YYYY-MM-DD): ");
        LocalDate out = readDate("Check-out (YYYY-MM-DD): ");
        int numberOfGuests = readInt("Total guests: ");
        System.out.println("A refundable LKR 10,000 advance is required.");
        // Simple CLI confirmation only; this is not an online payment service.
        if (!readYesNo("Have you received the LKR 10,000 advance? (Y/N): ")) {
            System.out.println("Booking not created: advance was not confirmed.");
            return;
        }
        K2703387_Reservation r = ADMIN.bookRoom(ADMIN.findGuestByNic(nic),
                ADMIN.findRoomById(id), in, out, numberOfGuests);
        System.out.println("BOOKING SUCCESSFUL: " + r);
        System.out.printf("Advance confirmed (manual receipt): LKR %,.2f%n",
                r.getBookingAdvance());
        System.out.println("Use Customer option 7 and booking ID " + r.getBookingId() + " to print the invoice.");
    }

    private static void searchReservation() {
        String q = readText("Booking ID or part of guest name: ");
        showReservations(ADMIN.findReservation(q));
    }

    private static void viewCheckIns() {
        showReservations(ADMIN.viewCheckIns(readDate("Check-in date (YYYY-MM-DD): ")));
    }

    private static void cancelBooking() {
        String id = readText("Booking ID: ");
        System.out.println(ADMIN.cancelBooking(id)
                ? "Booking cancelled. Reservation dates have been released; advance refundable."
                : "Unable to cancel: booking missing, already cancelled, or within 24 hours of check-in.");
    }

    private static void printInvoice() {
        String id = readText("Booking ID: ");
        System.out.println(ADMIN.generateInvoice(id).printDetailedInvoice());
    }

    private static void showRooms(List<K2703387_Room> rooms) {
        if (rooms.isEmpty()) { System.out.println("No matching rooms found."); return; }
        System.out.println("\nROOM ID | TYPE         | BED    | RATE        | STATUS            | DETAILS");
        for (K2703387_Room r : rooms) System.out.println(r);
    }

    private static void showReservations(List<K2703387_Reservation> results) {
        if (results.isEmpty()) { System.out.println("No reservations found."); return; }
        for (K2703387_Reservation r : results) System.out.println(r);
    }

    private static int chooseRoomType() {
        System.out.println("1. Standard  2. Deluxe  3. Suite  4. Family Suite");
        int type = readInt("Room type: ");
        if (type < 1 || type > 4) throw new IllegalArgumentException("Choose room type 1-4.");
        return type;
    }

    private static String roomTypeName(int option) {
        return switch (option) {
            case 1 -> "Standard";
            case 2 -> "Deluxe";
            case 3 -> "Suite";
            case 4 -> "Family Suite";
            default -> throw new IllegalArgumentException("Invalid room type.");
        };
    }

    private static K2703387_BedType chooseBedType() {
        System.out.println("1. SINGLE  2. DOUBLE  3. QUEEN  4. KING");
        int option = readInt("Bed type: ");
        if (option < 1 || option > 4) throw new IllegalArgumentException("Choose bed 1-4.");
        return K2703387_BedType.values()[option - 1];
    }

    private static String readText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String text = INPUT.nextLine().trim();
            if (!text.isEmpty()) return text;
            System.out.println("Please enter a non-empty value.");
        }
    }

    /** Read yes/no safely without silently accepting other answers. */
    private static boolean readYesNo(String prompt) {
        while (true) {
            String answer = readText(prompt);
            if (answer.equalsIgnoreCase("Y") || answer.equalsIgnoreCase("YES")) return true;
            if (answer.equalsIgnoreCase("N") || answer.equalsIgnoreCase("NO")) return false;
            System.out.println("Please type Y or N.");
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            try { return Integer.parseInt(readText(prompt)); }
            catch (NumberFormatException e) { System.out.println("Enter a whole number."); }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            try { return Double.parseDouble(readText(prompt)); }
            catch (NumberFormatException e) { System.out.println("Enter a valid numeric amount."); }
        }
    }

    private static LocalDate readDate(String prompt) {
        while (true) {
            try { return LocalDate.parse(readText(prompt)); }
            catch (DateTimeParseException e) {
                System.out.println("Date format must be YYYY-MM-DD, e.g. 2026-12-05.");
            }
        }
    }
}