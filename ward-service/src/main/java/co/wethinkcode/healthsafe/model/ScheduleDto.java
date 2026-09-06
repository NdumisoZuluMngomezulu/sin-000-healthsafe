package co.wethinkcode.healthsafe.model;

import java.io.Serializable;

public class ScheduleDto {
    private String ward;
    private String department;
    
}

/*
package co.wethinkcode.healthsafe.model;

import java.io.Serializable;

public class WardIngestionDto implements Serializable {
    private String wardId;
    private String specialty;
    private String wing;

    // Default constructor is required by Jackson for deserialisation
    public WardIngestionDto() {}

    public WardIngestionDto(String wardId, String specialty, String wing) {
        this.wardId = wardId;
        this.specialty = specialty;
        this.wing = wing;
    }

    // Getters and Setters
    public String getWardId() { return wardId; }
    public void setWardId(String wardId) { this.wardId = wardId; }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    public String getWing() { return wing; }
    public void setWing(String wing) { this.wing = wing; }
} */
