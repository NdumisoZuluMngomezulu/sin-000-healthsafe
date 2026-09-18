package co.wethinkcode.healthsafe.model;

/**
 * A cleaned ward record, as exposed by IngestionServiceApp over REST for
 * ward-service to consume. See ingestion-service/README.md for the cleaning
 * rules applied to the raw wards-outdated.csv export.
 */
public class Ward {
    private String wardId;
    private String wing;
    private String department;
    private Integer bedsAvailable; // null when the source value was missing/invalid
    private String notes;          // human-readable explanation when a field was flagged

    public Ward() {
    }

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

    @Override
    public String toString() {
        return "Ward{" + wardId + ", " + wing + ", " + department + ", beds=" + bedsAvailable + "}";
    }
}
