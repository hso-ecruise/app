package me.ecruise.data;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Jens Ullrich on 07.06.2017.
 */

public class Car {
    private LatLng pos;
    private boolean full;
    private boolean available;
    private int id;
    private int chargingLevel;
    private String name;

    public Car(int id, LatLng pos, boolean full, boolean available, int chargingLevel, String name) {
        this.setId(id);
        this.setPos(pos);
        this.setFull(full);
        this.setAvailable(available);
        this.setChargingLevel(chargingLevel);
        this.setName(name);
    }

    public String getName(){
        return name;
    }

    public int getId() {
        return id;
    }

    public LatLng getPos() {
        return pos;
    }

    public void setPos(LatLng pos) {
        this.pos = pos;
    }

    public boolean isFull() {
        return full;
    }

    public void setFull(boolean full) {
        this.full = full;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getChargingLevel() {
        return chargingLevel;
    }

    /**
     *
     * @param chargingLevel
     */
    public void setChargingLevel(int chargingLevel) {
        if(chargingLevel < 25)
            this.chargingLevel = 0;
        if(chargingLevel >= 25 && chargingLevel < 50)
            this.chargingLevel = 25;
        if(chargingLevel >= 50 && chargingLevel < 75)
            this.chargingLevel = 50;
        if(chargingLevel >= 75 && chargingLevel < 100)
            this.chargingLevel = 75;
        if(chargingLevel == 100)
            this.chargingLevel = 100;
    }

    public void setName(String name) {
        this.name = name;
    }
}
