package co.wethinkcode.healthsafe.model;

public class Doctor {
    private int id;
    private String name;
    private String specialty;

    public Doctor(int id, String name, String spec){
        this.id = id;
        this.name = name;
        this.specialty = spec;
    }

    public String getName(){return this.name;}
    public int getId(){return this.id;}
    public String getSpecialty(){return this.specialty;}
}
