package ecruise.data;

/**
 * Created by Tom on 28.03.2017.
 */
public interface IServerConnection
{
    boolean checkID(int UserID, int tripID);
    BookingState getBookingState();
    ChargingState getChargingState();
}
