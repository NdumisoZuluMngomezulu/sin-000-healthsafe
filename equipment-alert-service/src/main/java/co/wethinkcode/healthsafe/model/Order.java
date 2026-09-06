package co.wethinkcode.healthsafe.model;

public class Order {
    private String type; //repair, purchase
    private Equipment equipment;
    private String wardId;
    private String department;

    public Order(String type, Equipment equipment, String wardId, String department){
        this.type = type;
        this.equipment = equipment;
        this.wardId = wardId;
        this.department = department;
    }
}
