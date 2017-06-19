package ecruise.data;

import org.json.JSONException;

import java.text.ParseException;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;

/**
 * Created by Tom on 28.03.2017.
 */
public interface IServerConnection
{
    // Handler returns ChargingState and BookingState, or null if error, or at least one of them is invalid
    void getCarStateTuple(int carId, OnFinishedHandler<CarStateTuple> onFinishedHandler);

    // Handler returns tripId, or null if chipCardUid is invalid or error if not found
    void hasBooked(int carId, String chipCardUid, OnFinishedHandler<Integer> onFinishedHandler);

    // Handler returns success, false if error
    void updateChargingState(int carId, ChargingState chargingState, OnFinishedHandler<Boolean> onFinishedHandler);

    // Handler returns success, or false if invalid or error (never null)
    void validId(String chipCardUid, OnFinishedHandler<Boolean> onFinishedHandler);

    // Handler returns tripId of the newly created trip, or null if error
    void createTrip(String chipCardUid, int carId, int startChargingStationId, OnFinishedHandler<Integer> onFinishedHandler);

    // Handler returns tripId of the ended trip, or null if error
    void endTrip(int tripId, int distanceTravelled, int endChargingStationId, OnFinishedHandler<Integer> onFinishedHandler);

    // Handler returns success, also false if error (never null)
    void updatePosition(int carId, double latitude, double longitude, OnFinishedHandler<Boolean> onFinishedHandler);

    // Handler returns if request is there, also false if error (never null)
    void hasPositionRequest(int carId, OnFinishedHandler<Boolean> onFinishedHandler);
}
