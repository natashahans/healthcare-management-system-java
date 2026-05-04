package model;

/**
 * Appointment Model - The Global Standard version.
 * Fully synchronized with the 13-column appointments.csv schema.
 * Supports simplified patient booking, multi-step admin workflows, and full detail views.
 */
public class Appointment {
    // Basic Identifiers
    private final String id;
    private final String patientId;
    private final String clinicianId;
    private final String facilityId;

    // Schedule Details
    private final String date;
    private final String time;
    private final String duration;
    private final String type;

    // Clinical Context
    private String status; // Non-final to allow cancellation and rescheduling
    private final String reason;
    private final String notes;

    // Audit Metadata
    private final String created;
    private String lastModified; // Non-final to track updates

    /**
     * Full Constructor for absolute data integrity.
     * Required for mapping raw CSV rows and generating professional detail views.
     */
    public Appointment(String id, String patientId, String clinicianId, String facilityId,
                       String date, String time, String duration, String type,
                       String status, String reason, String notes,
                       String created, String lastModified) {
        this.id = id;
        this.patientId = patientId;
        this.clinicianId = clinicianId;
        this.facilityId = facilityId;
        this.date = date;
        this.time = time;
        this.duration = duration;
        this.type = type;
        this.status = status;
        this.reason = reason;
        this.notes = notes;
        this.created = created;
        this.lastModified = lastModified;
    }

    // ==========================================================
    // ===        GETTERS (For Dashboard & Detail Views)      ===
    // ==========================================================

    public String getId() { return id; }
    public String getPatientId() { return patientId; }
    public String getClinicianId() { return clinicianId; }
    public String getFacilityId() { return facilityId; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getDuration() { return duration; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public String getReason() { return reason; }
    public String getNotes() { return notes; }
    public String getCreated() { return created; }
    public String getLastModified() { return lastModified; }

    /**
     * Requirement Helper: Combines date and time for clean display in JTables.
     */
    public String getDateTime() {
        return date + " " + time;
    }

    // ==========================================================
    // ===       SETTERS (For Rescheduling/Cancellation)      ===
    // ==========================================================

    public void setStatus(String status) {
        this.status = status;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }
}