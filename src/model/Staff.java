package model;

public abstract class Staff extends Person {
    protected String username;
    protected String password;

    public Staff(String id, String f, String l, String dob, String u, String p) {
        super(id, f, l, dob);
        this.username = u;
        this.password = p;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    @Override public String getDetails() { return "Staff ID: " + id; }
}