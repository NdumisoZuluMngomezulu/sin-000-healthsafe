package co.wethinkcode.healthsafe.model;

import java.io.Serializable;

public class StaffingEvent implements Serializable {
    private String eventType; //schedule generated, schedule updated, schedule deleted
    private Object payload; //HOLDS actual data
    private long timestamp;

    public StaffingEvent() {}

    public StaffingEvent(String eventType, Object payload) {
        this.eventType = eventType;
        this.payload = payload;
        this.timestamp = System.currentTimeMillis();
    }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public Object getPayload() { return payload; }
    public void setPayload(Object payload) { this.payload = payload; }
}
