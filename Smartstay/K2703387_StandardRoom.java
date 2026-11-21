// K2703387_StandardRoom derives common fields and behaviors from K2703387_Room
public class K2703387_StandardRoom extends K2703387_Room {
    private static final double EXTRA_GUEST_FEE = 1000;
    private static final double TAX_RATE = 0.10;

    public K2703387_StandardRoom(String roomId, K2703387_BedType bedType) {
        super(roomId, "Standard", bedType, 6000, 1);
    }

    @Override public boolean hasComplimentaryBreakfast() { return false; }
    @Override public double getExtraGuestChargeRate() { return EXTRA_GUEST_FEE; }
    @Override public double getTaxRate() { return TAX_RATE; }
}
