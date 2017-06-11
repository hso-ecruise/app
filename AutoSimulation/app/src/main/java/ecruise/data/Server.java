package ecruise.data;

import ecruise.logic.Logger;
import ecruise.logic.CarState;

/**
 * Created by Tom on 21.03.2017.
 */
// This is a helper class to make the ServerConnection act like an singleton
public class Server
{
    private static IServerConnection connection;

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
