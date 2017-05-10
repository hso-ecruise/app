package ecruise.data;

/**
 * Created by Tom on 28.03.2017.
 */
public class ServerConnection implements IServerConnection
{
    @Override
    public boolean checkID(String chipCardUid, int carID)
    {
        switch (carID)
        {
            case 1:
                return chipCardUid.equals("04:C3:B2:FA:E7:49:80");
            case 2:
                return chipCardUid.equals("04:AA:38:62:02:49:80");
        }
        return false;
    }

    @Override
    public BookingState getBookingState()
    {
        return BookingState.BOOKED;
    }

    @Override
    public ChargingState getChargingState()
    {
        return ChargingState.FULL;
    }
}
