package co.wethinkcode.healthsafe.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * ward-service's own view of a ward: the cleaned fields from ingestion-service,
 * plus the doctors/alert-level/equipment info layered on by staffing-service
 * (stage 3, via the staffing-events-topic) and by equipment tracking (stage 4).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Ward {
    private String wardId;
    private String wing;
    private String department;
    private Integer bedsAvailable;
    private String notes;

    private int alertLevel;
    private List<String> assignedDoctors = new ArrayList<>();
    private List<Equipment> equipmentList = new ArrayList<>();

    public Ward() {
    }

    // Used when populating from ingestion-service's cleaned records.
    public Ward(String wardId, String wing, String department, Integer bedsAvailable, String notes) {
        this.wardId = wardId;
        this.wing = wing;
        this.department = department;
        this.bedsAvailable = bedsAvailable;
        this.notes = notes;
    }

    public String getWardId() { return wardId; }
    public void setWardId(String wardId) { this.wardId = wardId; }

    public String getWing() { return wing; }
    public void setWing(String wing) { this.wing = wing; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public Integer getBedsAvailable() { return bedsAvailable; }
    public void setBedsAvailable(Integer bedsAvailable) { this.bedsAvailable = bedsAvailable; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public int getAlertLevel() { return alertLevel; }
    public void setAlertLevel(int alertLevel) { this.alertLevel = alertLevel; }

    public List<String> getAssignedDoctors() { return assignedDoctors; }
    public void setAssignedDoctors(List<String> assignedDoctors) {
        this.assignedDoctors = assignedDoctors != null ? assignedDoctors : new ArrayList<>();
    }

    public List<Equipment> getEquipmentList() { return equipmentList; }
    public void setEquipmentList(List<Equipment> equipmentList) {
        this.equipmentList = equipmentList != null ? equipmentList : new ArrayList<>();
    }

    public void addEquipment(Equipment equipment) {
        this.equipmentList.add(equipment);
    }

    /** Equipment on this ward currently flagged as damaged. */
    public List<Equipment> faultyEquipment() {
        List<Equipment> faulty = new ArrayList<>();
        for (Equipment e : equipmentList) {
            if (e.isDamaged()) {
                faulty.add(e);
            }
        }
        return faulty;
    }

    @Override
    public String toString() {
        return "Ward{" + wardId + ", " + department + ", " + wing + "}";
    }
}
