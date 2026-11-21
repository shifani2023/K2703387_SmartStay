import java.util.Objects;

/** Abstract parent class for all four SmartStay room packages. */
public abstract class K2703387_Room {
    protected final String roomId;  // Accessible to derived room subtypes
    protected final String roomType;
    protected K2703387_BedType bedType;
    protected double dailyRate;
    protected K2703387_RoomStatus status;
    protected final int maxCapacity;

    protected K2703387_Room(String roomId, String roomType,
                            K2703387_BedType bedType, double dailyRate, int maxCapacity) {
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("Room ID cannot be empty.");
        }
        this.roomId = roomId.trim();
        this.roomType = Objects.requireNonNull(roomType);
        this.bedType = Objects.requireNonNull(bedType);
        setDailyRate(dailyRate);
        if (maxCapacity < 1) throw new IllegalArgumentException("Capacity must be positive.");
        this.maxCapacity = maxCapacity;
        this.status = K2703387_RoomStatus.AVAILABLE;
    }

    public String getRoomId() { return roomId; }
    public String getRoomType() { return roomType; }
    public K2703387_BedType getBedType() { return bedType; }
    public double getDailyRate() { return dailyRate; }
    public K2703387_RoomStatus getStatus() { return status; }
    public int getMaxCapacity() { return maxCapacity; }

    public void setBedType(K2703387_BedType bed) {
        this.bedType = Objects.requireNonNull(bed);
    }

    public void setDailyRate(double rate) {
        if (!Double.isFinite(rate) || rate <= 0) {
            throw new IllegalArgumentException("Daily rate must be positive.");
        }
        this.dailyRate = rate;
    }

    public void setStatus(K2703387_RoomStatus status) {
        this.status = Objects.requireNonNull(status);
    }
// Abstract methods defining contractual obligations for concrete rooms
    public abstract boolean hasComplimentaryBreakfast();
    public abstract double getExtraGuestChargeRate();
    public abstract double getTaxRate();

    @Override
    public String toString() {
        return String.format("%-6s | %-12s | %-6s | LKR %,.2f | %-17s | capacity %d | breakfast %s",
                roomId, roomType, bedType, dailyRate, status, maxCapacity,
                hasComplimentaryBreakfast() ? "Yes" : "No");
    }
}
