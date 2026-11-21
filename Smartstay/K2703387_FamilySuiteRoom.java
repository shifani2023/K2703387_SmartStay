public class K2703387_FamilySuiteRoom extends K2703387_Room {
    private static final double EXTRA_GUEST_FEE = 2500;
    private static final double TAX_RATE = 0.15;

    public K2703387_FamilySuiteRoom(String roomId, K2703387_BedType bedType) {
        super(roomId, "Family Suite", bedType, 18000, 4);
    }

    @Override public boolean hasComplimentaryBreakfast() { return true; }
    @Override public double getExtraGuestChargeRate() { return EXTRA_GUEST_FEE; }
    @Override public double getTaxRate() { return TAX_RATE; }
}
