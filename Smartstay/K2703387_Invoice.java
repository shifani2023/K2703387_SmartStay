import java.util.Locale;
import java.util.Objects;

/** An invoice belongs to one reservation and keeps its calculated amounts. */
public class K2703387_Invoice {
    private final String invoiceId;
    private final K2703387_Reservation reservation;
    private double baseRoomCharge;
    private double extraGuestCharge;
    private double discountAmount;
    private double taxAmount;
    private final double advanceDeducted;
    private double finalPayableAmount;

    public K2703387_Invoice(String invoiceId, K2703387_Reservation reservation) {
        this.invoiceId = Objects.requireNonNull(invoiceId);
        this.reservation = Objects.requireNonNull(reservation);
        this.advanceDeducted = reservation.getBookingAdvance();

        // Save the amounts ONCE. A room rate change must not alter an old invoice.
        K2703387_Room room = reservation.getRoom();
        long nights = reservation.calculateNumberOfNights();
        int extraGuests = Math.max(0, reservation.getNumberOfGuests() - room.getMaxCapacity());
        baseRoomCharge = room.getDailyRate() * nights;
        extraGuestCharge = extraGuests * room.getExtraGuestChargeRate() * nights;
        discountAmount = nights >= 5 ? baseRoomCharge * 0.12 : 0;
        // Executes subclass-specific tax rate dynamically without checking concrete type
        taxAmount = (baseRoomCharge - discountAmount + extraGuestCharge) * room.getTaxRate();
        finalPayableAmount = baseRoomCharge + extraGuestCharge - discountAmount
                + taxAmount - advanceDeducted;
    }

    // These methods return the values saved on invoice creation, not live room prices.
    public double calculateBaseCharge() { return baseRoomCharge; }
    public double calculateExtraGuestCharge() { return extraGuestCharge; }
    public double calculateDiscount() { return discountAmount; }
    public double calculateTax() { return taxAmount; }
    public double calculateFinalBill() { return finalPayableAmount; }

    public String printDetailedInvoice() {
        K2703387_Room room = reservation.getRoom();
        K2703387_Guest guest = reservation.getGuest();
        long nights = reservation.calculateNumberOfNights();
        double rateAtBooking = baseRoomCharge / nights;
        double taxableSubtotal = baseRoomCharge - discountAmount + extraGuestCharge;
        double taxPercent = taxableSubtotal > 0 ? taxAmount * 100 / taxableSubtotal : 0;
        double payable = Math.max(0, finalPayableAmount);
        double refund = Math.max(0, -finalPayableAmount);

        return String.format(Locale.US,
            "\n========== SMARTSTAY INVOICE ==========\n" +
            "Invoice ID          : %s\nBooking ID          : %s\n" +
            "Guest name          : %s\nNIC / Passport      : %s\n" +
            "Contact             : %s\nEmail               : %s\n" +
            "Room ID             : %s\nRoom type           : %s\n" +
            "Bed type            : %s\nDaily rate (booked) : LKR %,.2f\n" +
            "Check-in            : %s\nCheck-out           : %s\n" +
            "Nights              : %d\nNumber of guests    : %d\n" +
            "Breakfast included  : %s\n" +
            "---------------------------------------\n" +
            "Base room charge    : LKR %,.2f\n" +
            "Extra guest charge  : LKR %,.2f\n" +
            "Discount (12%%)      : LKR %,.2f\n" +
            "Tax (%.0f%%)          : LKR %,.2f\n" +
            "Advance deducted    : LKR %,.2f\n" +
            "---------------------------------------\n" +
            "Net balance         : LKR %,.2f\n" +
            "PAYABLE BY GUEST    : LKR %,.2f\n" +
            "REFUND TO GUEST     : LKR %,.2f\n" +
            "=======================================\n",
            invoiceId, reservation.getBookingId(), guest.getFullName(),
            guest.getNicOrPassport(), guest.getContactNumber(), guest.getEmail(),
            room.getRoomId(), room.getRoomType(), room.getBedType(), rateAtBooking,
            reservation.getCheckInDate(), reservation.getCheckOutDate(),
            nights, reservation.getNumberOfGuests(),
            room.hasComplimentaryBreakfast() ? "Yes" : "No", baseRoomCharge,
            extraGuestCharge, discountAmount, taxPercent, taxAmount,
            advanceDeducted, finalPayableAmount, payable, refund);
    }

    public String getInvoiceId() { return invoiceId; }
    public double getFinalPayableAmount() { return finalPayableAmount; }
}
