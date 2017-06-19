package ecruise.logic;

import ecruise.data.*;
import org.json.JSONException;

import java.text.ParseException;
import java.util.concurrent.ExecutionException;

/**
 * Created by Tom on 17.06.2017.
 */

public class Car
{
    private final int CAR_ID;
    private StatusLED statusLED = new StatusLED();
    private ScanLED scanLED = new ScanLED();
    private IScanDevice scanDevice;

    public boolean isDriving()
    {
        return driving;
    }

    private boolean driving = false;

    public boolean isPausing()
    {
        return pausing;
    }

    private boolean pausing = false;
    private String driverUid = "";
    private int tripId = -1;

    public Car(int carId, IScanDevice scanDevice)
    {
        this.CAR_ID = carId;
        this.scanDevice = scanDevice;
    }


    public void endTrip(OnFinishedHandler<Boolean> onFinishedHandler)
    {
        Server.getConnection().endTrip(tripId, 10, 2, (patchedTripId) ->
        {
            if (patchedTripId == null)
            {
                onFinishedHandler.handle(false);
                return;
            }

            Server.getConnection().updatePosition(CAR_ID, 49.485133, 8.463558, (success) ->
            {
                if (success)
                {
                    driving = false;
                    onFinishedHandler.handle(true);
                    return;
                }
                else
                {
                    onFinishedHandler.handle(false);
                    return;
                }
            });


            onFinishedHandler.handle(true);
        });
    }

    private void getCarState(OnFinishedHandler<CarState> onFinishedHandler)
    {
        Server.getConnection().getCarStateTuple(CAR_ID, (stateTuple) ->
        {
            if (stateTuple == null)
            {
                onFinishedHandler.handle(null);
                return;
            }

            if (stateTuple.getBookingState() == null)
            {
                onFinishedHandler.handle(null);
                return;
            }

            if (stateTuple.getChargingState() == null)
            {
                onFinishedHandler.handle(null);
                return;
            }
            BookingState bState = stateTuple.getBookingState();
            ChargingState cState = stateTuple.getChargingState();

            if (bState == BookingState.AVAILABLE && cState == ChargingState.CHARGING)
            {
                onFinishedHandler.handle(CarState.AVAILABLE_CHARGING);
                return;
            }

            if (bState == BookingState.AVAILABLE && cState == ChargingState.FULL)
            {
                onFinishedHandler.handle(CarState.AVAILABLE_FULL);
                return;
            }

            if (bState == BookingState.BOOKED)
            {
                onFinishedHandler.handle(CarState.BOOKED);
                return;
            }

            if (bState == BookingState.BLOCKED)
            {
                onFinishedHandler.handle(CarState.BLOCKED);
                return;
            }

            if (cState == ChargingState.DISCHARGING)
            {
                onFinishedHandler.handle(CarState.DRIVING);
                return;
            }

            onFinishedHandler.handle(null);
            return;

        });

    }

    public void updatePositionToPausingPostion(OnFinishedHandler<Boolean> onFinishedHandler)
    {
        if (isPausing())
        {
            Server.getConnection().hasPositionRequest(CAR_ID, (hasPositionRequest) ->
            {
                if (hasPositionRequest)
                {
                    // https://goo.gl/maps/6rhVQWGBpQE2
                    Server.getConnection().updatePosition(CAR_ID, 49.502820, 8.499191, (success) ->
                    {
                        if (success)
                        {
                            onFinishedHandler.handle(true);
                            return;
                        }
                        else
                        {
                            onFinishedHandler.handle(false);
                            return;
                        }
                    });
                }
                else
                {
                    onFinishedHandler.handle(false);
                    return;
                }
            });
        }
        else
        {
            onFinishedHandler.handle(false);
            return;
        }
    }


    public void getStatusLedColorCode(OnFinishedHandler<ColorCode> onFinishedHandler)
    {
        if (isDriving())
        {
            onFinishedHandler.handle(ColorCode.OFF);
            return;
        }

        getCarState((carState) ->
        {
            if (carState == null)
            {
                onFinishedHandler.handle(null);
                return;
            }

            onFinishedHandler.handle(statusLED.getColorCode(carState));
            return;
        });
    }

    public void pause()
    {
        pausing = true;
    }

    public void scanNfc(OnFinishedHandler<ColorCode> onFinishedHandler)
    {
        String chipCardUid = scanDevice.scanChipCardUid();

        if (driving && !pausing)
        {
            onFinishedHandler.handle(ColorCode.OFF);
            return;
        }

        if (pausing)
        {
            if (driverUid.equals(chipCardUid))
            {
                pausing = false;
                onFinishedHandler.handle(ColorCode.GREEN);
                return;
            }
            else
            {
                onFinishedHandler.handle(ColorCode.RED);
                return;
            }
        }

        getCarState((carState ->
        {
            if (carState == null)
            {
                onFinishedHandler.handle(null);
                return;
            }

            if (carState == CarState.BLOCKED)
            {
                onFinishedHandler.handle(ColorCode.RED);
                return;
            }

            Server.getConnection().validId(chipCardUid, (validId) ->
            {
                if (!validId)
                {
                    onFinishedHandler.handle(ColorCode.RED);
                    return;
                }

                if (carState == CarState.BOOKED)
                {
                    // existing trip
                    Server.getConnection().hasBooked(CAR_ID, chipCardUid, (bookedTripId) ->
                    {
                        ColorCode colorCode = scanLED.getColorCode(carState, bookedTripId != null);
                        if (bookedTripId != null && colorCode == ColorCode.GREEN)
                        {
                            tripId = bookedTripId;
                            driverUid = chipCardUid;
                            driving = true;
                            pausing = false;
                            Logger.getInstance().log("Booking used to start trip");

                            Server.getConnection().updateChargingState(CAR_ID, ChargingState.DISCHARGING, (success) ->
                            {
                                if (success)
                                {
                                    onFinishedHandler.handle(colorCode);
                                    return;
                                }
                                else
                                {
                                    onFinishedHandler.handle(null);
                                    return;
                                }
                            });
                        }
                        else
                        {
                            onFinishedHandler.handle(colorCode);
                            return;
                        }
                    });
                }
                else
                {
                    ColorCode colorCode = scanLED.getColorCode(carState, false);

                    // start new trip
                    if (colorCode == ColorCode.GREEN)
                    {
                        Server.getConnection().createTrip(chipCardUid, CAR_ID, 2, (createdTripId) ->
                        {
                            if (createdTripId == null)
                            {
                                onFinishedHandler.handle(null);
                                return;
                            }

                            tripId = createdTripId;
                            driverUid = chipCardUid;
                            driving = true;
                            pausing = false;
                            Logger.getInstance().log("New spontaneous trip");
                            onFinishedHandler.handle(colorCode);
                            return;
                        });
                    }
                    else
                    {
                        onFinishedHandler.handle(colorCode);
                        return;
                    }
                }
            });
        }));
    }
}
