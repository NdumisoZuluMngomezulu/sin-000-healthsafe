package co.wethinkcode.healthsafe.model;

import java.io.Serializable;

public class Equipment implements Serializable {
    private String name;
    private int quantity;

    public Equipment() {}

    public Equipment(String name, int number){
        this.name = name;
        this.quantity = number;
    }

    public String getName(){return this.name;}
    public int getQuantity(){return this.quantity;}
}
