package ecruise.data;

/**
 * Created by Tom on 24.06.2017.
 */

public class Trip
{
    private int tripId;
    private int startChargingStationId;

    public Trip(int tripId, int startChargingStationId)
    {
        this.tripId = tripId;
        this.startChargingStationId = startChargingStationId;
    }

    public int getTripId()
    {
        return tripId;
    }

    void setTripId(int tripId)
    {
        this.tripId = tripId;
    }

    public int getStartChargingStationId()
    {
        return startChargingStationId;
    }

    void setStartChargingStationId(int startChargingStationId)
    {
        this.startChargingStationId = startChargingStationId;
    }
}
