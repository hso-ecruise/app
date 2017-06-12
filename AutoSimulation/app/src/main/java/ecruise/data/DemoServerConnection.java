package ecruise.data;

/**
 * @author Tom
 * @since 04.06.2017.
 */
// for testing purposes
public class DemoServerConnection implements IServerConnection
{

    private BookingState bookingState = BookingState.BOOKED;
    private ChargingState chargingState = ChargingState.CHARGING;

    public DemoServerConnection()
    {
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Thread.sleep(15000);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                chargingState = ChargingState.FULL;
            }
        });
        thread.start();
    }

    @Override
    public boolean checkID(String chipCardUid)
    {
        return chipCardUid.equals("04:AA:38:62:02:49:80");
    }

    public void SetDischarging()
    {
        chargingState = ChargingState.DISCHARGING;
    }

    @Override
    public BookingState getBookingState()
    {
        return bookingState;
    }

    @Override
    public ChargingState getChargingState()
    {
        return chargingState;
    }

    @Override
    public boolean startTrip(String chipCardUid)
    {
        return false;
    }


    @Override
    public void endTrip(int distanceTravelled, int endCharingStationId)
    {

    }
}
