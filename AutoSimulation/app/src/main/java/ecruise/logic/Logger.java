package ecruise.logic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tom on 22.03.2017.
 */
// The Logger is implemented with a simple observer-pattern
public class Logger
{
    private static Logger ourInstance = new Logger();

    private List<ILogListener> listeners = new ArrayList<>();

    public static Logger getInstance()
    {
        return ourInstance;
    }

    private Logger()
    {
    }

    public void addListener(ILogListener listener)
    {
        listeners.add(listener);
    }

    public void removeListener(ILogListener listener)
    {
        listeners.remove(listener);
    }

    public void log(String text)
    {
        for (ILogListener listener : listeners)
        {
            listener.log(text);
        }
    }

    public void logInfo(String text)
    {
        for (ILogListener listener : listeners)
        {
            listener.log("(i)" + text);
        }
    }

    public void logWarning(String text)
    {
        for (ILogListener listener : listeners)
        {
            listener.log("(!)" + text);
        }
    }

    public void logError(String text)
    {
        for (ILogListener listener : listeners)
        {
            listener.log("(x)" + text);
        }
    }
}
