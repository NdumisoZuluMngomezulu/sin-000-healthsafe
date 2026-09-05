package co.wethinkcode.healthsafe.model;

import java.util.List;

public class Ward {
    private String ward_id;
    private String wing;
    private String department;
    private List<Doctor> assignedDoctors;
    private int alertLevel;

    public Ward(){}

    public Ward(String id, String wing, String department){
        this.ward_id = id;
        this.wing = wing;
        this.department = department;
    }

    public String getId(){return this.ward_id;}
    public String getWing(){return this.wing;}
    public String department(){return this.department;}
    public void setId(String id){this.ward_id = id;}
    public void setWing(String wing){this.wing = wing;}
    public void setDept(String dep){this.department = dep;}
    @Override
    public String toString(){return "This is ward is for " + department() + " on the " + getWing() + " wing.";}
}
