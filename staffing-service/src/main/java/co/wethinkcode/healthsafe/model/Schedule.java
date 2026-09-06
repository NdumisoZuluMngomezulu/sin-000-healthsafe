package co.wethinkcode.healthsafe.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Schedule {
    private Ward ward;
    private String wing;
    private String department;
    private List<Doctor> assignedDoctors;
    private int alertLevel;

    public Schedule(Ward ward){
        this.department = ward.department();
        this.assignedDoctors = new ArrayList<>();
    }

    public Ward ward(){return this.ward;}
    public String wing(){return this.wing;}
    public String department(){return department;}
    public List<Doctor> doctors(){return List.copyOf(assignedDoctors);}
    public void assignDoctors(List<Doctor> doctors) {
        int i = 0;
        while(this.assignedDoctors.size() != alertLevel) {
            if (doctors.get(i).getSpecialty().equals(this.department)){
                assignedDoctors.add(doctors.get(i));
            }
            i++;
        }
    }
}
