package ecruise.data;

/**
 * Created by Tom on 17.06.2017.
 */

public interface OnFinishedHandler<Param>
{
    void handle(Param param);
}
