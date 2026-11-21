// K2703387_DeluxeRoom derives common fields and behaviors from K2703387_Room
public class K2703387_DeluxeRoom extends K2703387_Room {
    private static final double EXTRA_GUEST_FEE = 1500;
    private static final double TAX_RATE = 0.12;

    public K2703387_DeluxeRoom(String roomId, K2703387_BedType bedType) {
        super(roomId, "Deluxe", bedType, 9000, 2);
    }

    @Override public boolean hasComplimentaryBreakfast() { return true; }
    @Override public double getExtraGuestChargeRate() { return EXTRA_GUEST_FEE; }
    @Override public double getTaxRate() { return TAX_RATE; }
}
