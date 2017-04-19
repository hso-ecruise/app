package ecruise.data;

import ecruise.logic.Logger;
import ecruise.logic.CarState;

/**
 * Created by Tom on 21.03.2017.
 */
public class Server
{
    private static IServerConnection connection;
    private Logger logger;

    public static IServerConnection getConnection()
    {
        if(connection == null)
            throw new NullPointerException("Server Connection is not set");
        return connection;
    }

    public static void setConnection(IServerConnection connection)
    {
        Server.connection = connection;
    }

    private Server()
    {
    }
}
