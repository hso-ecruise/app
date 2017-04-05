package ecruise.data;

/**
 * Created by Tom on 28.03.2017.
 */
public class ServerConnection implements IServerConnection
{
    @Override
    public boolean checkID(int UserID, int tripID)
    {
        return true;
    }

    @Override
    public BookingState getBookingState()
    {
        return BookingState.BOOKED;
    }

    @Override
    public ChargingState getChargingState()
    {
        return ChargingState.CHARGING;
    }
}
