package co.wethinkcode.healthsafe.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * staffing-service's view of a ward, as returned by ward-service's
 * GET /wards/{id} (see Integration contracts in the root README). Ignores
 * the extra fields ward-service's own Ward carries (alertLevel,
 * assignedDoctors, equipmentList) - staffing-service only needs the
 * ingestion-derived fields to size a schedule.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Ward {
    private String wardId;
    private String wing;
    private String department;
    private Integer bedsAvailable;
    private String notes;

    public Ward(){}

    public Ward(String wardId, String wing, String department){
        this.wardId = wardId;
        this.wing = wing;
        this.department = department;
    }

    public String getWardId(){return this.wardId;}
    public void setWardId(String wardId){this.wardId = wardId;}
    public String getWing(){return this.wing;}
    public void setWing(String wing){this.wing = wing;}
    public String getDepartment(){return this.department;}
    public void setDepartment(String department){this.department = department;}
    public Integer getBedsAvailable(){return this.bedsAvailable;}
    public void setBedsAvailable(Integer bedsAvailable){this.bedsAvailable = bedsAvailable;}
    public String getNotes(){return this.notes;}
    public void setNotes(String notes){this.notes = notes;}

    @Override
    public String toString(){return "This ward is for " + department + " on the " + wing + " wing.";}
}
