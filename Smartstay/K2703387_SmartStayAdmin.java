
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Central controller: validates room, guest and reservation operations. */
public class K2703387_SmartStayAdmin {

    private final String adminId;
    private final List<K2703387_Room> rooms = new ArrayList<>();
    private final List<K2703387_Guest> guests = new ArrayList<>();
    private final List<K2703387_Reservation> reservations = new ArrayList<>();
    private int nextBookingNumber = 1;

    public K2703387_SmartStayAdmin(String adminId) {
        if (adminId == null || adminId.isBlank())
            throw new IllegalArgumentException("Administrator ID required.");
        this.adminId = adminId;
    }

    public void addRoom(K2703387_Room room) {
        Objects.requireNonNull(room);
        if (findRoomById(room.getRoomId()) != null)
            throw new IllegalArgumentException("Room ID already exists.");
        rooms.add(room);
    }

    private boolean hasFutureBooking(K2703387_Room room) {
        for (K2703387_Reservation booking : reservations) {
            if (booking.getRoom() == room && !booking.isCancelled()
                    && booking.getCheckOutDate().isAfter(LocalDate.now())) return true;
        }
        return false;
    }

    public boolean updateRoom(String roomId, K2703387_BedType bed, double rate) {
        K2703387_Room room = findRoomById(roomId);
        // Avoid retroactively changing pricing details used by active reservations.
        if (room == null || hasFutureBooking(room)) return false;
        if (bed == null || !Double.isFinite(rate) || rate <= 0) return false;
        room.setBedType(bed);
        room.setDailyRate(rate);
        return true;
    }

    public boolean removeRoom(String roomId) {
        K2703387_Room room = findRoomById(roomId);
        if (room == null || hasFutureBooking(room)) return false;
        return rooms.remove(room);
    }

    public boolean setAvailability(String roomId, K2703387_RoomStatus status) {
        K2703387_Room room = findRoomById(roomId);
        if (room == null || status == null) return false;
        room.setStatus(status);
        return true;
    }

    public List<K2703387_Room> viewRooms() {
        return List.copyOf(rooms);
    }

    public void addGuest(K2703387_Guest guest) {
        Objects.requireNonNull(guest);
        if (findGuestByNic(guest.getNicOrPassport()) != null)
            throw new IllegalArgumentException("NIC/passport already registered.");
        guests.add(guest);
    }

    // Overload 1: Filter by room type string only
    public List<K2703387_Room> searchRooms(String type) {
        List<K2703387_Room> results = new ArrayList<>();
        for (K2703387_Room room : rooms) {
            if (room.getStatus() == K2703387_RoomStatus.AVAILABLE
                    && room.getRoomType().equalsIgnoreCase(type.trim())) results.add(room);
        }
        return results;
    }

    /** Second overload: room type AND absence of overlapping active reservations. */
    public List<K2703387_Room> searchRooms(String type, LocalDate in, LocalDate out) {
        validateDateRange(in, out);
        List<K2703387_Room> results = new ArrayList<>();
        for (K2703387_Room room : searchRooms(type)) {
            if (isFreeOnDates(room, in, out)) results.add(room);
        }
        return results;
    }

    /** Third overload: bed type AND date range. */
    public List<K2703387_Room> searchRooms(LocalDate in, LocalDate out,
                                            K2703387_BedType bedType) {
        validateDateRange(in, out);
        Objects.requireNonNull(bedType);
        List<K2703387_Room> results = new ArrayList<>();
        for (K2703387_Room room : rooms) {
            if (room.getStatus() == K2703387_RoomStatus.AVAILABLE
                    && room.getBedType() == bedType && isFreeOnDates(room, in, out)) {
                results.add(room);
            }
        }
        return results;
    }

    private void validateDateRange(LocalDate in, LocalDate out) {
        if (in == null || out == null || !out.isAfter(in))
            throw new IllegalArgumentException("Check-out must be later than check-in.");
        if (in.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Check-in cannot be in the past.");
    }

    private boolean isFreeOnDates(K2703387_Room room, LocalDate in, LocalDate out) {
        for (K2703387_Reservation booking : reservations) {
            if (booking.getRoom() == room && booking.isOverlapping(in, out)) return false;
        }
        return true;
    }

    public K2703387_Reservation bookRoom(K2703387_Guest guest, K2703387_Room room,
                                         LocalDate in, LocalDate out, int numberOfGuests) {
        validateDateRange(in, out);

        if (guest == null || findGuestByNic(guest.getNicOrPassport()) != guest)
            throw new IllegalArgumentException("Guest must be registered first.");

        if (room == null || findRoomById(room.getRoomId()) != room)
            throw new IllegalArgumentException("Room does not exist.");

        if (numberOfGuests < 1)
            throw new IllegalArgumentException("Guests must be at least 1.");

        // Allow only ONE extra guest beyond normal room capacity.
        // Prevents customers from booking more guests than the room can accommodate.
        if (numberOfGuests > room.getMaxCapacity() + 1) {
            throw new IllegalArgumentException(
                "Booking rejected. This room allows a maximum of "
                + (room.getMaxCapacity() + 1)
                + " guests, including one extra guest." 
            );
        }

        if (room.getStatus() != K2703387_RoomStatus.AVAILABLE)
            throw new IllegalArgumentException("Room is not currently available.");

        if (!isFreeOnDates(room, in, out))
            throw new IllegalArgumentException("Room already booked during these dates.");

        String bookingId = String.format(Locale.US, "B%04d", nextBookingNumber);

        K2703387_Reservation booking = new K2703387_Reservation(
                bookingId, guest, room, in, out, numberOfGuests);

        if (!booking.isValidAdvanceBooking())
            throw new IllegalArgumentException("Book at least two days before check-in.");

        // Prepare the single invoice at booking time to keep the booked room rate.
        // generateInvoice() later returns this same invoice for the reservation.
        booking.generateInvoice();

        nextBookingNumber++;
        reservations.add(booking);

        // A future booking does NOT change operational status to RESERVED:
        // date-overlap checking allows the same room to be sold on other dates.
        return booking;
    }

    public List<K2703387_Reservation> findReservation(String query) {
        if (query == null || query.isBlank()) return List.of();

        String q = query.trim().toLowerCase(Locale.ROOT);
        List<K2703387_Reservation> results = new ArrayList<>();

        for (K2703387_Reservation r : reservations) {
            if (r.getBookingId().toLowerCase(Locale.ROOT).equals(q)
                    || r.getGuest().getFullName().toLowerCase(Locale.ROOT).contains(q)) {
                results.add(r);
            }
        }
        return results;
    }

    public List<K2703387_Reservation> viewCheckIns(LocalDate date) {
        List<K2703387_Reservation> results = new ArrayList<>();

        for (K2703387_Reservation r : reservations) {
            if (!r.isCancelled() && r.getCheckInDate().equals(date)) results.add(r);
        }
        return results;
    }

    public boolean cancelBooking(String id) {
        K2703387_Reservation booking = findByBookingId(id);

        if (booking == null ||
                !booking.isEligibleForCancellation(LocalDateTime.now())) return false;

        booking.setCancelled(true);

        // Retain history and release the reserved dates.
        // Do not override an administrator-set RESERVED or UNDER_MAINTENANCE status.
        // The room can be booked again on those dates once its status is AVAILABLE.
        return true;
    }

    public K2703387_Invoice generateInvoice(String bookingId) {
        K2703387_Reservation booking = findByBookingId(bookingId);

        if (booking == null || booking.isCancelled())
            throw new IllegalArgumentException("No active booking exists with this ID.");

        return booking.generateInvoice();
    }

    private K2703387_Reservation findByBookingId(String id) {
        if (id == null) return null;

        for (K2703387_Reservation booking : reservations) {
            if (booking.getBookingId().equalsIgnoreCase(id.trim())) return booking;
        }
        return null;
    }

    public K2703387_Guest findGuestByNic(String nicOrPassport) {
        if (nicOrPassport == null) return null;

        for (K2703387_Guest guest : guests) {
            if (guest.getNicOrPassport().equalsIgnoreCase(nicOrPassport.trim())) return guest;
        }
        return null;
    }

    public K2703387_Room findRoomById(String roomId) {
        if (roomId == null) return null;

        for (K2703387_Room room : rooms) {
            if (room.getRoomId().equalsIgnoreCase(  roomId.trim())) return room;
        }
        return null;
    }
}
