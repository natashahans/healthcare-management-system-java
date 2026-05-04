package model;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors; // FIXED: THIS IMPORT IS NOW PRESENT

public class DataAccessLayer {
    private static final String DATA_DIR = "data/";

    private void handleIOError(Exception e, String context) {
        System.err.println("CRITICAL DATA ERROR [" + context + "]: " + e.getMessage());
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(null,
                        "Database System Error: Could not " + context + ".\n" +
                                "Please ensure the CSV files are not open in Excel.",
                        "System Persistence Error",
                        JOptionPane.ERROR_MESSAGE)
        );
    }

    public List<String[]> loadRawData(String fileName) {
        List<String[]> rows = new ArrayList<>();
        File file = new File(DATA_DIR + fileName);
        if (!file.exists()) return rows;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // Skip Header
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    // Regex handles commas inside quotes (addresses)
                    rows.add(line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1));
                }
            }
        } catch (IOException e) {
            handleIOError(e, "read " + fileName);
        }
        return rows;
    }

    public String generateNextId(String prefix, String fileName) {
        int count = loadRawData(fileName).size() + 1;
        return String.format("%s%03d", prefix, count);
    }

    public List<Patient> loadPatients() {
        List<Patient> list = new ArrayList<>();
        for (String[] r : loadRawData("patients.csv")) {
            if (r.length >= 14) {
                list.add(new Patient(
                        r[0].replace("\"", ""), r[1], r[2], r[3], r[4],
                        r[5], r[6], r[7], r[8].replace("\"", ""), r[9],
                        r[10], r[11], r[12], r[13]
                ));
            }
        }
        return list;
    }

    public String getPatientName(String id) {
        for (Patient p : loadPatients()) {
            if (p.getId().equals(id) || p.getNhsNumber().equals(id)) {
                return p.getFullName();
            }
        }
        return "Unknown Patient (" + id + ")";
    }

    public void saveNewPatient(Patient p) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(DATA_DIR + "patients.csv", true)))) {
            out.println(String.format("%s,%s,%s,%s,%s,%s,%s,%s,\"%s\",%s,%s,%s,%s,%s",
                    p.getId(), p.getFirstName(), p.getLastName(), p.getDateOfBirth(), p.getNhsNumber(),
                    p.getGender(), p.getPhone(), p.getEmail(), p.getAddress().replace("\"", ""), p.getPostcode(),
                    p.getNextOfKin(), p.getNokPhone(), p.getRegDate(), p.getGpSurgeryId()));
        } catch (IOException e) {
            handleIOError(e, "save new patient record");
        }
    }

    public List<Appointment> loadAppointments() {
        List<Appointment> list = new ArrayList<>();
        for (String[] r : loadRawData("appointments.csv")) {
            if (r.length >= 13) {
                list.add(new Appointment(r[0], r[1], r[2], r[3], r[4], r[5], r[6], r[7], r[8], r[9], r[10], r[11], r[12]));
            }
        }
        return list;
    }

    public void saveNewAppointment(Appointment a) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(DATA_DIR + "appointments.csv", true)))) {
            out.println(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,\"%s\",\"%s\",%s,%s",
                    a.getId(), a.getPatientId(), a.getClinicianId(), a.getFacilityId(),
                    a.getDate(), a.getTime(), a.getDuration(), a.getType(), a.getStatus(),
                    a.getReason(), a.getNotes(), a.getCreated(), a.getLastModified()));
        } catch (IOException e) {
            handleIOError(e, "book new appointment");
        }
    }

    public void saveNewReferral(Referral r, String patientId, String clinicianId, String summary) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(DATA_DIR + "referrals.csv", true)))) {
            String cleanSummary = (summary == null) ? "" : summary.replace(",", ";");
            out.println(String.format("%s,%s,%s,C005,S001,H001,%s,%s,Referral,%s,None,Pending,,,2026-01-13,2026-01-13",
                    r.getReferralId(), patientId, clinicianId, r.getReferralDate(), r.getUrgency(), cleanSummary));
        } catch (IOException e) {
            handleIOError(e, "generate clinical referral");
        }
    }

    public void updateAppointment(String id, String newDateTime, String newStatus) {
        List<String[]> rows = loadRawData("appointments.csv");
        String[] dt = newDateTime.split(" ");
        for (String[] r : rows) {
            if (r.length > 0 && r[0].equals(id)) {
                if (newDateTime.contains("-")) {
                    r[4] = dt[0];
                    r[5] = (dt.length > 1) ? dt[1] : "09:00";
                }
                r[8] = newStatus;
                r[12] = "2026-01-13";
            }
        }
        rewriteFile("appointments.csv", "appointment_id,patient_id,clinician_id,facility_id,appointment_date,appointment_time,duration_minutes,appointment_type,status,reason_for_visit,notes,created_date,last_modified", rows);
    }

    public void cancelAppointment(String id) {
        updateAppointment(id, "STAY_SAME", "Cancelled");
    }

    public void deletePatient(String nhs) {
        List<String[]> rows = loadRawData("patients.csv");
        rows.removeIf(r -> r.length > 4 && r[4].equals(nhs));
        rewriteFile("patients.csv", "patient_id,first_name,last_name,date_of_birth,nhs_number,gender,phone_number,email,address,postcode,emergency_contact_name,emergency_contact_phone,registration_date,gp_surgery_id", rows);
    }

    public void deleteAppointment(String id) {
        List<String[]> rows = loadRawData("appointments.csv");
        rows.removeIf(r -> r.length > 0 && r[0].equals(id));
        rewriteFile("appointments.csv", "appointment_id,patient_id,clinician_id,facility_id,appointment_date,appointment_time,duration_minutes,appointment_type,status,reason_for_visit,notes,created_date,last_modified", rows);
    }

    public String getFacilityName(String id) {
        for (String[] r : loadRawData("facilities.csv")) {
            if (r.length > 1 && r[0].equals(id)) return r[1];
        }
        return "General Practice Centre (" + id + ")";
    }

    /**
     * Passes 6 arguments (id, f, l, dob, email, pass) to AdministrativeStaff
     */
    public List<Staff> loadAllStaff() {
        List<Staff> allStaff = new ArrayList<>();
        for (String[] r : loadRawData("staff.csv")) {
            if (r.length > 7) {
                // Constructor: String id, String f, String l, String dob, String u, String p
                allStaff.add(new AdministrativeStaff(r[0], r[1], r[2], "1980-01-01", r[7], "pass"));
            }
        }
        allStaff.addAll(loadClinicians());
        return allStaff;
    }

    public List<Clinician> loadClinicians() {
        List<Clinician> list = new ArrayList<>();
        for (String[] r : loadRawData("clinicians.csv")) {
            if (r.length > 7) {
                String id = r[0], fName = r[1], lName = r[2], email = r[7], title = r[3], spec = r[4];
                // Subclass constructors: id, f, l, dob, username, password
                if (title.contains("GP"))
                    list.add(new GeneralPractitioner(id, fName, lName, "1975-01-01", email, "pass"));
                else if (title.contains("Nurse"))
                    list.add(new Nurse(id, fName, lName, "1985-01-01", email, "pass"));
                else
                    list.add(new SpecialistDoctor(id, fName, lName, "1970-01-01", email, "pass", spec));
            }
        }
        return list;
    }

    public String generateSystemReport(int pCount, int cCount, int rCount) {
        List<Appointment> allAppts = loadAppointments();
        long scheduled = allAppts.stream().filter(a -> a.getStatus().equalsIgnoreCase("Scheduled")).count();
        long cancelled = allAppts.stream().filter(a -> a.getStatus().equalsIgnoreCase("Cancelled")).count();

        String report = "=== HMS GLOBAL SYSTEM AUDIT REPORT ===\n" +
                "Generated: " + new Date() + "\n" +
                "--------------------------------------\n" +
                "REGISTRY TOTALS:\n" +
                "Total Patients:     " + pCount + "\n" +
                "Total Staff:        " + cCount + "\n" +
                "Active Referrals:   " + rCount + "\n\n" +
                "APPOINTMENT BREAKDOWN:\n" +
                "Total Recorded:     " + allAppts.size() + "\n" +
                "Current Scheduled:  " + scheduled + "\n" +
                "Total Cancelled:    " + cancelled + "\n" +
                "--------------------------------------\n" +
                "System Status:      OPTIMAL\n";

        try (PrintWriter out = new PrintWriter(new FileWriter(DATA_DIR + "admin_report.txt"))) {
            out.println(report);
        } catch (IOException e) {
            handleIOError(e, "update reports");
        }
        return report;
    }

    private void rewriteFile(String fileName, String header, List<String[]> rows) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(DATA_DIR + fileName, false))) {
            pw.println(header);
            for (String[] r : rows) pw.println(String.join(",", r));
        } catch (IOException e) {
            handleIOError(e, "update " + fileName);
        }
    }

    public void saveVitals(String patientId, String bp, String hr, String temp, String weight) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(DATA_DIR + "vitals.txt", true)))) {
            out.println(String.format("DATE: %s | PATIENT: %s | BP: %s | HR: %s | TEMP: %s | WT: %s",
                    new Date(), patientId, bp, hr, temp, weight));
        } catch (IOException e) {
            handleIOError(e, "save patient vitals");
        }
    }

    // Prescription CSV persistence
    public void saveNewPrescription(Prescription rx, String pId, String cId) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(DATA_DIR + "prescriptions.csv", true)))) {
            out.println(String.format("%s,%s,%s,,%s,%s,%s,%s,%s,28 tablets,%s,Boots Pharmacy,Issued,%s,",
                    rx.getId(), pId, cId, rx.getDate(), rx.getMedication(), rx.getDosage(),
                    rx.getFrequency(), rx.getDuration(), rx.getInstructions(), rx.getDate()));
        } catch (IOException e) { handleIOError(e, "save prescription"); }
    }

    // 2. Validation: Check for duplicates
    public boolean isDuplicatePatient(String id, String nhs) {
        for (Patient p : loadPatients()) {
            if (p.getId().equals(id) || p.getNhsNumber().equals(nhs)) return true;
        }
        return false;
    }

    /**
     * ROLE DERIVATION
     * check for "nurs" which covers both "Nurse" and "Nursing".
     */
    public String deriveRole(String specialty) {
        String s = specialty.toLowerCase();
        if (s.contains("nurs")) return "Nurse"; // FIXED: Matches "General Nursing"
        if (s.contains("gp") || s.contains("general practice")) return "GP";
        return "Specialist";
    }


    // Medical History Lookups
    public List<String[]> getPatientPrescriptions(String nhs) {
        return loadRawData("prescriptions.csv").stream().filter(r -> r.length > 1 && r[1].equals(nhs)).collect(Collectors.toList());
    }

    public List<String[]> getPatientReferrals(String nhs) {
        return loadRawData("referrals.csv").stream().filter(r -> r.length > 1 && r[1].equals(nhs)).collect(Collectors.toList());
    }
}