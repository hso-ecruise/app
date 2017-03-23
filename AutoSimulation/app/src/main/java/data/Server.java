package data;

import logic.Logger;
import logic.CarState;

import java.util.InvalidPropertiesFormatException;

/**
 * Created by Tom on 21.03.2017.
 */
public class Server
{
    private static Server ourInstance = new Server();
    private Logger logger;

    public static Server getInstance()
    {
        return ourInstance;
    }

    private Server()
    {
    }

    public boolean checkID(int UserID, int tripID)
    {
        Logger.getInstance().log("Demo Checking User Credentials");
        return true;
    }

    public CarState getCarState() throws UnsupportedOperationException
    {
        Logger.getInstance().log("Demo Contacting Server for State");
        Logger.getInstance().log("Demo Recieved Answer for Car State: 235637");

        BookingState bState = BookingState.BOOKED; // To be replaced by server request
        ChargingState cState = ChargingState.CHARGING; // To be replaced by server request


        if (bState == BookingState.AVAILABLE && cState == ChargingState.CHARGING)
            return CarState.AVAILABLE_CHARGING;

        if (bState == BookingState.AVAILABLE && cState == ChargingState.FULL)
            return CarState.AVAILABLE_FULL;

        if (bState == BookingState.BOOKED && cState == ChargingState.CHARGING)
            return CarState.BOOKED_CHARGING;

        if (bState == BookingState.BOOKED && cState == ChargingState.FULL)
            return CarState.BOOKED_FULL;

        if(cState == ChargingState.DISCHARGING)
            throw new UnsupportedOperationException("Auto befindet sich nicht an der Ladesäule");

        return CarState.BLOCKED;
    }

}
