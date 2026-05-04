package model;

public class Prescription {
    private final String id, date, medication, dosage, frequency, duration, instructions, pharmacy;

    public Prescription(String id, String date, String medication, String dosage,
                        String frequency, String duration, String instructions, String pharmacy) {
        this.id = id;
        this.date = date;
        this.medication = medication;
        this.dosage = dosage;
        this.frequency = frequency;
        this.duration = duration;
        this.instructions = instructions;
        this.pharmacy = pharmacy;
    }

    public String getId() { return id; }
    public String getDate() { return date; }
    public String getMedication() { return medication; }
    public String getDosage() { return dosage; }
    public String getFrequency() { return frequency; }
    public String getDuration() { return duration; }
    public String getInstructions() { return instructions; }
    public String getPharmacy() { return pharmacy; }
}