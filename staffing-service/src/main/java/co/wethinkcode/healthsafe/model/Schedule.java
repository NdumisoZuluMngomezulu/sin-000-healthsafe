package co.wethinkcode.healthsafe.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An on-call schedule for a single ward: how many/which doctors are on call,
 * sized by the hospital's current Emergency Status (alertLevel, 0-8).
 */
public class Schedule {
    private String wardId;
    private String wing;
    private String department;
    private int alertLevel;
    private List<Doctor> assignedDoctors = new ArrayList<>();

    public Schedule() {
    }

    public Schedule(Ward ward, int alertLevel, List<Doctor> availableDoctors) {
        this.wardId = ward.getWardId();
        this.wing = ward.getWing();
        this.department = ward.getDepartment();
        this.alertLevel = alertLevel;
        this.assignedDoctors = assignDoctors(availableDoctors, department, alertLevel);
    }

    /** At least one doctor on call regardless of status; more as alertLevel climbs. */
    private static List<Doctor> assignDoctors(List<Doctor> doctors, String department, int alertLevel) {
        int required = Math.max(1, alertLevel);
        if (doctors == null || department == null) {
            return new ArrayList<>();
        }
        return doctors.stream()
                .filter(d -> department.equalsIgnoreCase(d.getSpecialty()))
                .limit(required)
                .collect(Collectors.toList());
    }

    public String getWardId() { return wardId; }
    public void setWardId(String wardId) { this.wardId = wardId; }

    public String getWing() { return wing; }
    public void setWing(String wing) { this.wing = wing; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public int getAlertLevel() { return alertLevel; }
    public void setAlertLevel(int alertLevel) { this.alertLevel = alertLevel; }

    public List<Doctor> getAssignedDoctors() { return assignedDoctors; }
    public void setAssignedDoctors(List<Doctor> assignedDoctors) { this.assignedDoctors = assignedDoctors; }
}
