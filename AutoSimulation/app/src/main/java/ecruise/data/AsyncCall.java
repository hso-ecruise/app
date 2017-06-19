package ecruise.data;

/**
 * Created by Tom on 17.06.2017.
 */

interface AsyncCall<Result, Param>
{
    Result operation(Param param);
}
