package ecruise.logic;

import ecruise.data.*;

import java.util.Calendar;
import java.util.Random;

/**
 * Created by Tom on 17.06.2017.
 */

public class Car
{
    private final int CAR_ID;
    private StatusLED statusLED = new StatusLED();
    private ScanLED scanLED = new ScanLED();
    private IScanDevice scanDevice;

    private boolean driving = false;
    private boolean pausing = false;
    private Calendar limitTime; // The DateTime when the Car is sending its position. 24h after start of trip
    private int limitTimeValue = 24;
    private int limitTimeUnit = Calendar.HOUR;

    public boolean isDriving()
    {
        return driving;
    }

    public boolean isPausing()
    {
        return pausing;
    }

    private String driverUid = "";
    private int tripId = -1;

    public Car(int carId, IScanDevice scanDevice)
    {
        this.CAR_ID = carId;
        this.scanDevice = scanDevice;
    }

    public void endTrip(OnFinishedHandler<Boolean> onFinishedHandler)
    {
        int distanceTravelled = new Random().nextInt((50 - 2) + 1) + 2;
        Server.getConnection().endTrip(CAR_ID, tripId, distanceTravelled, (patchedTripId) ->
        {
            if (patchedTripId == null)
            {
                onFinishedHandler.handle(false);
                return;
            }

            int chargingLevel = new Random().nextInt((95 - 80) + 1) + 80;
            Server.getConnection().updateChargeLevel(CAR_ID, chargingLevel, (successChargeLevel) ->
            {
                if (!successChargeLevel)
                {
                    onFinishedHandler.handle(false);
                    return;
                }

                driving = false;
                onFinishedHandler.handle(true);
                return;
            });
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

    public void updatePositionToPausingPosition(OnFinishedHandler<Boolean> onFinishedHandler)
    {
        if (isPausing())
        {
            Server.getConnection().hasPositionRequest(CAR_ID, (hasPositionRequest) ->
            {
                if (limitTime != null)
                {
                    if (limitTime.before(Calendar.getInstance()))
                    {
                        Logger.getInstance().logInfo("Pausing time exhausted. Sending position.");
                        hasPositionRequest = true;
                        limitTime = null;
                    }
                }

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

            if (carState == CarState.DRIVING)
            {
                driving = true;
                pausing = false;
                limitTime = Calendar.getInstance();
                limitTime.add(limitTimeUnit, limitTimeValue);
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
                limitTime = Calendar.getInstance();
                limitTime.add(limitTimeUnit, limitTimeValue);
                onFinishedHandler.handle(ColorCode.GREEN);
                return;
            }
            else
            {
                onFinishedHandler.handle(ColorCode.RED);
                return;
            }
        }
        Server.getConnection().validId(chipCardUid, (validId) ->
        {
            if (!validId)
            {
                onFinishedHandler.handle(ColorCode.RED);
                return;
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
                    onFinishedHandler.handle(scanLED.getColorCode(carState, false));
                    return;
                }

                if (carState == CarState.BOOKED)
                {
                    // existing trip
                    Server.getConnection().hasBooked(CAR_ID, chipCardUid, (bookedTrip) ->
                    {
                        ColorCode colorCode = scanLED.getColorCode(carState, bookedTrip != null);
                        if (bookedTrip != null && colorCode == ColorCode.GREEN)
                        {
                            tripId = bookedTrip.getTripId();
                            driverUid = chipCardUid;
                            driving = true;
                            pausing = false;
                            limitTime = Calendar.getInstance();
                            limitTime.add(limitTimeUnit, limitTimeValue);
                            Logger.getInstance().logInfo("Booking used to start trip");

                            Server.getConnection().updateChargingState(CAR_ID, ChargingState.DISCHARGING, (successChargingState) ->
                            {
                                Server.getConnection().decreaseSlotsOccupied(bookedTrip.getStartChargingStationId(), (successDecrease) ->
                                {
                                    onFinishedHandler.handle(colorCode);
                                    return;
                                });
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
                        Server.getConnection().createTrip(chipCardUid, CAR_ID, (createdTripId) ->
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
                            limitTime = Calendar.getInstance();
                            limitTime.add(limitTimeUnit, limitTimeValue);
                            Logger.getInstance().logInfo("New spontaneous trip");
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
            }));
        });
    }
}
