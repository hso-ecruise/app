package ecruise.logic;

import android.content.Context;
import ecruise.data.IScanDevice;
import ecruise.data.NFCReader;
import ecruise.data.Server;

/**
 * Created by Tom on 21.03.2017.
 */
// This class is a mapping for BookingState and ChargingState to the ScanLED
// it also changes in the event of scanning a Card
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
                if (Server.getConnection().checkID(scanDevice.scanUserId()))
                {
                    return ColorCode.GREEN;
                }
                else
                {
                    return ColorCode.RED;
                }
            case BOOKED_CHARGING:
                if (Server.getConnection().checkID(scanDevice.scanUserId()))
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
                return ColorCode.GREEN;
            case BLOCKED:
                return ColorCode.RED;
        }

        return ColorCode.YELLOW;
    }
}
