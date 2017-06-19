package ecruise.logic;

import ecruise.data.BookingState;
import ecruise.data.ChargingState;
import ecruise.data.Server;

/**
 * Created by Tom on 21.03.2017.
 */

// This class is a mapping for BookingState and ChargingState to the StatusLED
// The StatusLED communicates with the Server
class StatusLED
{
    ColorCode getColorCode(CarState carState)
    {
        try
        {
            switch (carState)
            {
                case BOOKED:
                    return ColorCode.BLUE;
                case AVAILABLE_CHARGING:
                    return ColorCode.YELLOW;
                case AVAILABLE_FULL:
                    return ColorCode.GREEN;
                case BLOCKED:
                    return ColorCode.RED;
                case DRIVING:
                    return ColorCode.OFF;
            }
        }
        catch (IllegalStateException e)
        {
            e.printStackTrace();
            Logger.getInstance().log(e.getMessage());
        }

        return ColorCode.OFF;
    }
}
