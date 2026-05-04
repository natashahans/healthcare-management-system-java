package model;

public class Referral {
    private final String referralId;
    private final String referralDate;
    private final String urgency;
    private final String status;

    public Referral(String referralId, String referralDate, String urgency, String status) {
        this.referralId = referralId;
        this.referralDate = referralDate;
        this.urgency = urgency;
        this.status = status;
    }

    public String getReferralId() { return referralId; }
    public String getReferralDate() { return referralDate; }
    public String getUrgency() { return urgency; }
    public String getStatus() { return status; }
}