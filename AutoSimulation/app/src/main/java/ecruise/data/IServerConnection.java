package ecruise.data;

/**
 * Created by Tom on 28.03.2017.
 */
public interface IServerConnection
{
    boolean checkID(String chipCardUid);
    BookingState getBookingState();
    ChargingState getChargingState();
    boolean startTrip(String chipCardUid);
    void endTrip(int distanceTravelled, int endCharingStationId);
}
