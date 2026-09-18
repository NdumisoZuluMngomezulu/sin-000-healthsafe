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

    public Equipment(String name, int quantity, boolean damaged) {
        this.name = name;
        this.quantity = quantity;
        this.damaged = damaged;
    }

    public String getName(){return this.name;}
    public void setName(String name){this.name = name;}
    public int getQuantity(){return this.quantity;}
    public void setQuantity(int quantity){this.quantity = quantity;}
    public boolean isDamaged(){return this.damaged;}
    // Kept for backwards compatibility with earlier scaffold code.
    public void setStatus(boolean isDamaged){this.damaged = isDamaged;}
    // Jackson needs a setter matching the "damaged" property name to deserialize it.
    public void setDamaged(boolean damaged){this.damaged = damaged;}
}
