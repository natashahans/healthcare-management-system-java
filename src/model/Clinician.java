package model;

public abstract class Clinician extends Staff {
    protected String specialty;

    public Clinician(String id, String f, String l, String dob, String u, String p, String s) {
        // Passes all 6 required fields to the Staff constructor
        super(id, f, l, dob, u, p);
        this.specialty = s;
    }

    public String getSpecialty() { return specialty; }
}