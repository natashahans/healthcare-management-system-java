package model;

public abstract class Person {
    protected String id;
    protected String firstName;
    protected String lastName;
    protected String dateOfBirth;

    public Person(String id, String firstName, String lastName, String dateOfBirth) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
    }

    public String getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getFullName() { return firstName + " " + lastName; }
    public abstract String getDetails();
}