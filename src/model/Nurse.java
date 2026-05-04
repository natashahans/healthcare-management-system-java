package model;

public class Nurse extends Clinician {
    public Nurse(String id, String f, String l, String dob, String u, String p) {
        super(id, f, l, dob, u, p, "Nursing");
    }
}