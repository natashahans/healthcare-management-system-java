package model;

public class Patient extends Person {
    private final String nhsNumber;
    private final String gender;
    private final String phone;
    private final String email;
    private final String address;
    private final String postcode;
    private final String nextOfKin;
    private final String nokPhone;
    private final String regDate;
    private final String gpSurgeryId;

    public Patient(String id, String f, String l, String dob, String nhs,
                   String gender, String phone, String email, String address, String postcode,
                   String nextOfKin, String nokPhone, String regDate, String gpSurgeryId) {
        super(id, f, l, dob);
        this.nhsNumber = nhs;
        this.gender = gender;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.postcode = postcode;
        this.nextOfKin = nextOfKin;
        this.nokPhone = nokPhone;
        this.regDate = regDate;
        this.gpSurgeryId = gpSurgeryId;
    }

    // Getters for all 14 fields
    public String getNhsNumber() { return nhsNumber; }
    public String getGender() { return gender; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getPostcode() { return postcode; }
    public String getNextOfKin() { return nextOfKin; }
    public String getNokPhone() { return nokPhone; }
    public String getRegDate() { return regDate; }
    public String getGpSurgeryId() { return gpSurgeryId; }

    @Override public String getDetails() { return "NHS: " + nhsNumber; }
}