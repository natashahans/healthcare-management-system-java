package model.services;

import model.*;
import java.util.List;

public class AuthenticationService {
    private final DataAccessLayer dal;

    public AuthenticationService(DataAccessLayer dal) {
        this.dal = dal;
    }

    /**
     * The Heart of the Three-Tier System:
     * Validates credentials and returns a Person (the common parent).
     */
    public Person authenticate(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) return null;

        // 1. Check Staff Hierarchy (Admins, GPs, Nurses, Specialists)
        // These use the Email from staff.csv or clinicians.csv as their username
        List<Staff> staffList = dal.loadAllStaff();
        for (Staff s : staffList) {
            if (s.getUsername().equalsIgnoreCase(username) && s.getPassword().equals(password)) {
                return s;
            }
        }

        // 2. Check Patient Hierarchy
        // Allows Sarah Brown to login via "sarah.brown@email.com" OR "4567890123"
        // Password for patients is set to "pass" for assessment simplicity
        List<Patient> patients = dal.loadPatients();
        for (Patient p : patients) {
            boolean isEmailMatch = p.getEmail().equalsIgnoreCase(username);
            boolean isNhsMatch = p.getNhsNumber().equals(username);

            if ((isEmailMatch || isNhsMatch) && password.equals("pass")) {
                return p;
            }
        }

        return null; // No match found
    }
}