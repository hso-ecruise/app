package ecruise.data;

/**
 * Created by Tom on 28.03.2017.
 */
public interface IServerConnection
{
    boolean checkID(String chipCardUid, int tripID);
    BookingState getBookingState();
    ChargingState getChargingState();
}
