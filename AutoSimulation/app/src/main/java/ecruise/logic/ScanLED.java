package ecruise.logic;

import ecruise.data.IScanDevice;
import ecruise.data.Server;

/**
 * Created by Tom on 21.03.2017.
 */
// This class is a mapping for BookingState and ChargingState to the ScanLED
// it will also change in the event of scanning a Card
// the ScanLED accesses the Server and the NFCReader to calculate its own state
class ScanLED
{
    ColorCode getColorCode(CarState carState, boolean hasBooked)
    {
        switch (carState)
        {
            case BOOKED:
                if (hasBooked)
                {
                    return ColorCode.GREEN;
                }
                else
                {
                    return ColorCode.RED;
                }
            case AVAILABLE_CHARGING:
                return ColorCode.YELLOW;
            case AVAILABLE_FULL:
                return ColorCode.GREEN;
            case BLOCKED:
                return ColorCode.RED;
        }

        return ColorCode.YELLOW;
    }
}
