/** Guest ID and name are immutable; contact information may be updated. */
public class K2703387_Guest {
    private final String nicOrPassport;
    private final String fullName;
    private String contactNumber;
    private String email;

    public K2703387_Guest(String nicOrPassport, String fullName,
                          String contactNumber, String email) {
        if (nicOrPassport == null || nicOrPassport.isBlank() ||
                fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("NIC/passport and name are required.");
        }
        this.nicOrPassport = nicOrPassport.trim();
        this.fullName = fullName.trim();
        setContactNumber(contactNumber);
        setEmail(email);
    }

    public String getNicOrPassport() { return nicOrPassport; }
    public String getFullName() { return fullName; }
    public String getContactNumber() { return contactNumber; }
    public String getEmail() { return email; }

    public void setContactNumber(String contact) {
        if (contact == null || !contact.matches("\\+?[0-9]{7,15}")) {
            throw new IllegalArgumentException("Contact number must contain 7-15 digits (optional +).");
        }
        this.contactNumber = contact;
    }

    public void setEmail(String email) {
        if (email == null || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }
        this.email = email.trim();
    }

    @Override
    public String toString() {
        return nicOrPassport + " | " + fullName + " | " + contactNumber + " | " + email;
    }
}
