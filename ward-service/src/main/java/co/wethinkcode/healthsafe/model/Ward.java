package co.wethinkcode.healthsafe.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class Ward {
    private String ward_id;
    private String wing;
    private String department;
    private List<Map<String, Object>> assignedDoctors;
    private int alertLevel;
    private List<Equipment> equipment_list;

    public Ward(){}

    public Ward(String id, String wing, String department){
        this.ward_id = id;
        this.wing = wing;
        this.department = department;
        this.assignedDoctors = new ArrayList<>();
        this.equipment_list = new ArrayList<>();
    }

    public String getId(){return this.ward_id;}
    public String getWing(){return this.wing;}
    public String department(){return this.department;}
    public void setId(String id){this.ward_id = id;}
    public void setWing(String wing){this.wing = wing;}
    public void equip(Equipment equipment){this.equipment_list.add(equipment);}
    public void setDept(String dep){this.department = dep;}
    public void setAlert(int level){this.alertLevel = level;}
    public void setDoctors(List<Map<String, Object>> doctors){this.assignedDoctors = doctors;}
    @Override
    public String toString(){return "This is ward is for " + department() + " on the " + getWing() + " wing.";}
}
