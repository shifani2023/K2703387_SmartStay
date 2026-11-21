public class K2703387_SuiteRoom extends K2703387_Room {
    private static final double EXTRA_GUEST_FEE = 2000;
    private static final double TAX_RATE = 0.15;

    public K2703387_SuiteRoom(String roomId, K2703387_BedType bedType) {
        super(roomId, "Suite", bedType, 14000, 3);
    }

    @Override public boolean hasComplimentaryBreakfast() { return true; }
    @Override public double getExtraGuestChargeRate() { return EXTRA_GUEST_FEE; }
    @Override public double getTaxRate() { return TAX_RATE; }
}
