package data;

import logic.Logger;
import logic.CarState;

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

    public boolean checkID(int UserID, int carId)
    {
        Logger.getInstance().log("Demo Checking User Credentials");
        return true;
    }

    public CarState getCarState()
    {
        Logger.getInstance().log("Demo Contacting Server for State");
        Logger.getInstance().log("Demo Recieved Answer for Car State: 235637");
        return CarState.LADEN_DANACH_GEBUCHT;
    }

}
