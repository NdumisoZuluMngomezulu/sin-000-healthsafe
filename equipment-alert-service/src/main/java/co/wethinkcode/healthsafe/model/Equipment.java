package co.wethinkcode.healthsafe.model;

import java.io.Serializable;

public class Equipment implements Serializable {
    private String name;
    private int quantity;
    private boolean damaged;

    public Equipment() {}

    public Equipment(String name, int number){
        this.name = name;
        this.quantity = number;
    }

    public String getName(){return this.name;}
    public int getQuantity(){return this.quantity;}
    public boolean isDamaged(){return this.damaged;}
    public void setStatus(boolean isDamaged){this.damaged = isDamaged;}
}
