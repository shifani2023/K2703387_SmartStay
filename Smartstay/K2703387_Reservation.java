import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/** Association to one guest and one room; owns exactly one invoice when issued. */
public class K2703387_Reservation {
    public static final double BOOKING_ADVANCE = 10000.0;
    private static final int ASSUMED_CHECK_IN_HOUR = 14;

    private final String bookingId;
    private final K2703387_Guest guest;
    private final K2703387_Room room;
    private final LocalDate bookingDate;
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;
    private final int numberOfGuests;
    private final double bookingAdvance = BOOKING_ADVANCE;
    private boolean isCancelled;
    private K2703387_Invoice invoice;

    public K2703387_Reservation(String bookingId, K2703387_Guest guest,
                                K2703387_Room room, LocalDate checkIn,
                                LocalDate checkOut, int guests) {
        if (bookingId == null || bookingId.isBlank()) {
            throw new IllegalArgumentException("Booking ID is required.");
        }
        this.bookingId = bookingId.trim();
        this.guest = Objects.requireNonNull(guest);
        this.room = Objects.requireNonNull(room);
        this.checkInDate = Objects.requireNonNull(checkIn);
        this.checkOutDate = Objects.requireNonNull(checkOut);
        this.bookingDate = LocalDate.now();
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Check-out must be after check-in.");
        }
        if (guests <= 0) throw new IllegalArgumentException("Number of guests must be positive.");
        this.numberOfGuests = guests;
        this.isCancelled = false;
    }

    public long calculateNumberOfNights() {
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    public boolean isValidAdvanceBooking() {
        return !checkInDate.isBefore(bookingDate.plusDays(2));
    }

    /** Date-only conservative approximation of the 24-hour cutoff. */
    // Overload 1: Date-only cancellation eligibility check
    public boolean isEligibleForCancellation(LocalDate currentDate) {
        return !isCancelled && currentDate.isBefore(checkInDate.minusDays(1));
    }

    /** Precise cutoff using the stated implementation assumption: 2 p.m. check-in. */
    // Overload 2: Time-specific cancellation check with check-in cutoff calculation
    public boolean isEligibleForCancellation(LocalDateTime currentDateTime) {
        LocalDateTime cutoff = checkInDate.atTime(ASSUMED_CHECK_IN_HOUR, 0).minusHours(24);
        return !isCancelled && currentDateTime.isBefore(cutoff);
    }

    /** Interval intersection: checkout is exclusive, so adjacent stays do not overlap. */
    public boolean isOverlapping(LocalDate startDate, LocalDate endDate) {
        return !isCancelled && startDate.isBefore(checkOutDate) && endDate.isAfter(checkInDate);
    }

    public K2703387_Invoice generateInvoice() {
        if (invoice == null) {
            invoice = new K2703387_Invoice("INV-" + bookingId, this);
        }
        return invoice;
    }

    public String getBookingId() { return bookingId; }
    public K2703387_Guest getGuest() { return guest; }
    public K2703387_Room getRoom() { return room; }
    public LocalDate getBookingDate() { return bookingDate; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public int getNumberOfGuests() { return numberOfGuests; }
    public double getBookingAdvance() { return bookingAdvance; }
    public boolean isCancelled() { return isCancelled; }
    public void setCancelled(boolean status) { isCancelled = status; }

    @Override
    public String toString() {
        return String.format("%s | %-20s | Room %s | %s to %s | guests %d | %s",
                bookingId, guest.getFullName(), room.getRoomId(), checkInDate,
                checkOutDate, numberOfGuests, isCancelled ? "CANCELLED" : "ACTIVE");
    }
}
