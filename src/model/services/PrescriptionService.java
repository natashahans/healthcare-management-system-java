package model.services;

import model.Prescription;
import model.DataAccessLayer;
import java.io.*;

public class PrescriptionService {
    private final DataAccessLayer dal;

    public PrescriptionService(DataAccessLayer dal) {
        this.dal = dal;
    }

    public void issuePrescription(Prescription rx, String patientId, String gpId) {
        dal.saveNewPrescription(rx, patientId, gpId);

        // 2. Generate Output Text File
        String fileName = "data/prescription_" + rx.getId() + ".txt";
        try (PrintWriter pw = new PrintWriter(new FileWriter(fileName))) {
            pw.println("=== OFFICIAL MEDICAL PRESCRIPTION ===");
            pw.println("Prescription ID: " + rx.getId());
            pw.println("Date Issued:     " + rx.getDate());
            pw.println("Medication:      " + rx.getMedication());
            pw.println("Dosage/Freq:     " + rx.getDosage() + " (" + rx.getFrequency() + ")");
            pw.println("Duration:        " + rx.getDuration() + " days");
            pw.println("Instructions:    " + rx.getInstructions());
            pw.println("Pharmacy:        " + rx.getPharmacy());
            pw.println("Authorized By:   " + gpId);
            pw.println("=====================================");
        } catch (IOException e) { e.printStackTrace(); }
    }
}