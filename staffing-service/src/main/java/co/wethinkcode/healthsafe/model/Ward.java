package co.wethinkcode.healthsafe.model;

public class Ward {
    private String ward_id;
    private String wing;
    private String department;
    private String available_beds;

    public Ward(){}

    public Ward(String id, String wing, String department, String beds){
        this.ward_id = id;
        this.wing = wing;
        this.department = department;
        this.available_beds = beds;
    }

    public String getId(){return this.ward_id;}
    public String getWing(){return this.wing;}
    public String department(){return this.department;}
    public String available_beds(){return this.available_beds;}
    public void setId(String id){this.ward_id = id;}
    public void setWing(String wing){this.wing = wing;}
    public void setDept(String dep){this.department = dep;}
    public void setBed(String bed){this.available_beds = bed;}
}
