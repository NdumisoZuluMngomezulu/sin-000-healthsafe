package co.wethinkcode.healthsafe.model;

public class Equipment {
    private String name;
    private int quantity;

    public Equipment(String name, int number){
        this.name = name;
        this.quantity = number;
    }

    public String getName(){return this.name;}
    public int getQuantity(){return this.quantity;}
}
