package ecruise.logic;

import ecruise.data.IScanDevice;
import ecruise.data.Server;

/**
 * Created by Tom on 21.03.2017.
 */
// This class is a mapping for BookingState and ChargingState to the ScanLED
// it will also change in the event of scanning a Card
// the ScanLED accesses the Server and the NFCReader to calculate its own state
public class ScanLED
{
    private IScanDevice scanDevice;

    public ScanLED(IScanDevice scanDevice)
    {
        this.scanDevice = scanDevice;
    }

    public ColorCode calculateColorCode()
    {
        switch (new StatusLED().calculateCarState())
        {
            case BOOKED_FULL:
                if (Server.getConnection().checkID(scanDevice.scanChipCardUid()))
                {
                    return ColorCode.GREEN;
                }
                else
                {
                    return ColorCode.RED;
                }
            case BOOKED_CHARGING:
                if (Server.getConnection().checkID(scanDevice.scanChipCardUid()))
                {
                    return ColorCode.YELLOW;
                }
                else
                {
                    return ColorCode.RED;
                }
            case AVAILABLE_CHARGING:
                return ColorCode.YELLOW;
            case AVAILABLE_FULL:
                String chipCardId = scanDevice.scanChipCardUid();
                if (Server.getConnection().startTrip(chipCardId))
                    return ColorCode.GREEN;
                else
                    return ColorCode.RED;
            case BLOCKED:
                return ColorCode.RED;
        }

        return ColorCode.YELLOW;
    }
}
