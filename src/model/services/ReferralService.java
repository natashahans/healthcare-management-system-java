package model.services;

import model.Referral;
import java.io.*;
import java.util.*;

public class ReferralService {
    private static ReferralService instance;
    private final List<Referral> referralQueue = new ArrayList<>();
    private ReferralService() {}

    public static synchronized ReferralService getInstance() {
        if (instance == null) instance = new ReferralService();
        return instance;
    }

    public void createReferral(Referral r, String summary, String patientName) {
        referralQueue.add(r);
        // Filename includes patient name for clarity
        String fileName = "data/REF_" + r.getReferralId() + "_" + patientName.replace(" ", "_") + ".txt";
        try (PrintWriter pw = new PrintWriter(new FileWriter(fileName))) {
            pw.println("=== CLINICAL REFERRAL LETTER ===");
            pw.println("Referral ID: " + r.getReferralId());
            pw.println("Patient:     " + patientName);
            pw.println("Urgency:     " + r.getUrgency());
            pw.println("Summary:     " + summary);
            pw.println("Generated:   " + new Date());
            pw.println("================================");
        } catch (IOException e) { e.printStackTrace(); }
    }

    public List<Referral> getReferralQueue() { return referralQueue; }

    /**
     * Appends a medical history entry to a patient-specific text file.
     */
    public void updateElectronicHealthRecord(String nhsNumber, Referral r, String gpName) {
        String fileName = "data/EHR_" + nhsNumber + ".txt";
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)))) {
            out.println("=== ELECTRONIC HEALTH RECORD UPDATE ===");
            out.println("Timestamp:   " + new java.util.Date());
            out.println("Event:       SPECIALIST REFERRAL CREATED");
            out.println("Referral ID: " + r.getReferralId());
            out.println("Urgency:     " + r.getUrgency());
            out.println("Authorized:  " + gpName);
            out.println("----------------------------------------\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}