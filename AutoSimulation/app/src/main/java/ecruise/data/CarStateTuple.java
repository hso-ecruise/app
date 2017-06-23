package ecruise.data;

/**
 * Created by Tom on 18.06.2017.
 */

public class CarStateTuple
{
    private ChargingState chargingState;
    private BookingState bookingState;

    CarStateTuple(ChargingState chargingState, BookingState bookingState)
    {
        this.chargingState = chargingState;
        this.bookingState = bookingState;
    }

    public ChargingState getChargingState()
    {
        return chargingState;
    }

    public BookingState getBookingState()
    {
        return bookingState;
    }
}
