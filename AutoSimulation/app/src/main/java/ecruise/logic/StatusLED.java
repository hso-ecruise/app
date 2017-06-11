package ecruise.logic;

import ecruise.data.BookingState;
import ecruise.data.ChargingState;
import ecruise.data.Server;

/**
 * Created by Tom on 21.03.2017.
 */

// This class is a mapping for BookingState and ChargingState to the StatusLED
// The StatusLED communicates with the Server
public class StatusLED
{
    public CarState calculateCarState()
    {
        BookingState bState = Server.getConnection().getBookingState();
        ChargingState cState = Server.getConnection().getChargingState();

        if (bState == BookingState.AVAILABLE && cState == ChargingState.CHARGING)
            return CarState.AVAILABLE_CHARGING;

        if (bState == BookingState.AVAILABLE && cState == ChargingState.FULL)
            return CarState.AVAILABLE_FULL;

        if (bState == BookingState.BOOKED && cState == ChargingState.CHARGING)
            return CarState.BOOKED_CHARGING;

        if (bState == BookingState.BOOKED && cState == ChargingState.FULL)
            return CarState.BOOKED_FULL;

        if (cState == ChargingState.DISCHARGING)
            return CarState.DISCHARGING;

        return CarState.BLOCKED;
    }

    public ColorCode calculateColorCode()
    {
        try
        {
            switch (calculateCarState())
            {
                case BOOKED_FULL:
                    return ColorCode.BLUE;
                case BOOKED_CHARGING:
                    return ColorCode.BLUE;
                case AVAILABLE_CHARGING:
                    return ColorCode.YELLOW;
                case AVAILABLE_FULL:
                    return ColorCode.GREEN;
                case BLOCKED:
                    return ColorCode.RED;
                case DISCHARGING:
                    return ColorCode.OFF;
            }
        } catch (UnsupportedOperationException e)
        {
            Logger.getInstance().log(e.getMessage());
        }

        return ColorCode.OFF;
    }
}
