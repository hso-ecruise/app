package me.ecruise.data;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Jens Ullrich on 07.06.2017.
 */

public class Station {
    private LatLng pos;
    private boolean free;
    private int id;
    private int slots;
    private int slotsOccupied;

    /**
     *
     * @param id
     * @param pos
     * @param free
     * @param slots
     * @param slotsOccupied
     */
    public Station(int id, LatLng pos, boolean free, int slots, int slotsOccupied) {
        this.setId(id);
        this.setPos(pos);
        this.setFree(free);
        this.setSlots(slots);
        this.setSlotsOccupied(slotsOccupied);
    }

    public LatLng getPos() {
        return pos;
    }

    public boolean isFree() {
        return free;
    }

    public int getSlots() {
        return slots;
    }

    public int getSlotsOccupied() {
        return slotsOccupied;
    }

    public String getName(){
        return Integer.toString(getSlotsOccupied()) + "/" + Integer.toString(getSlots()) + " Slots";
    }

    public int getId() {
        return id;
    }

    public void setPos(LatLng pos) {
        this.pos = pos;
    }

    public void setFree(boolean free) {
        this.free = free;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setSlots(int slots) {
        this.slots = slots;
    }

    public void setSlotsOccupied(int slotsOccupied) {
        this.slotsOccupied = slotsOccupied;
    }
}
