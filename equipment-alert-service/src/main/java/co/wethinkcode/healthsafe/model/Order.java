package co.wethinkcode.healthsafe.model;

/** A repair/purchase order raised in response to an equipment failure alert. */
public class Order {
    private String type; // repair, purchase
    private Equipment equipment;
    private String wardId;
    private String department;

    public Order() {
    }

    public Order(String type, Equipment equipment, String wardId, String department){
        this.type = type;
        this.equipment = equipment;
        this.wardId = wardId;
        this.department = department;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Equipment getEquipment() { return equipment; }
    public void setEquipment(Equipment equipment) { this.equipment = equipment; }

    public String getWardId() { return wardId; }
    public void setWardId(String wardId) { this.wardId = wardId; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}
