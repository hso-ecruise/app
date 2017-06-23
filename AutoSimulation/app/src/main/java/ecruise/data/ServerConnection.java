package ecruise.data;

import android.content.Context;
import android.util.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.*;
import ecruise.logic.JsonDate;
import ecruise.logic.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * Created by Tom on 28.03.2017.
 */
// the interface to the api.ecruise.me
public class ServerConnection implements IServerConnection
{
    private static final String AUTH_EMAIL = "admin@ecruise.me";
    private static final String AUTH_PASSWORD = "ecruiseAdmin123!!!";
    private String accessToken;

    private RequestQueue requestQueue;

    private void request(final JsonRequest req)
    {
        if (req.getBody() != null)
        {
            Logger.getInstance().log("\uD83C\uDF10" + req.getUrl().substring(22)
                    + " \uD83D\uDCE6" + new String(req.getBody(), StandardCharsets.UTF_8).replace("\"ecruiseAdmin123!!!\"", "<<PASSWORD REDACTED>>"));
        }
        else
        {
            Logger.getInstance().log("\uD83C\uDF10" + req.getUrl().substring(22) + " \uD83D\uDCE6\u2205");
        }
        requestQueue.add(req);
    }

    public ServerConnection(Context ctx, OnFinishedHandler<Boolean> onFinishedHandler)
    {
        requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        authenticate(onFinishedHandler);
    }

    @Override
    public void getCarStateTuple(int carId, OnFinishedHandler<CarStateTuple> onFinishedHandler)
    {
        getCarById(carId, (car) ->
        {
            if (car == null)
            {
                onFinishedHandler.handle(null);
                return;
            }

            ChargingState chargingState = null;
            try
            {
                switch (car.getInt("chargingState"))
                {
                    case 1:
                        chargingState = ChargingState.DISCHARGING;
                        break;
                    case 2:
                        chargingState = ChargingState.CHARGING;
                        break;
                    case 3:
                        chargingState = ChargingState.FULL;
                        break;
                    default:
                        Log.e("ServerConnection", "Unknown chargingState received from Server");
                        onFinishedHandler.handle(null);
                        return;
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
                onFinishedHandler.handle(null);
                return;
            }

            Log.d("ServerConnection", "chargingState received " + chargingState);

            BookingState bookingState = null;
            try
            {
                switch (car.getInt("bookingState"))
                {
                    case 1:
                        bookingState = BookingState.AVAILABLE;
                        break;
                    case 2:
                        bookingState = BookingState.BOOKED;
                        break;
                    case 3:
                        bookingState = BookingState.BLOCKED;
                        break;
                    default:
                        Log.e("ServerConnection", "Unknown bookingState received from Server");
                        onFinishedHandler.handle(null);
                        return;
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
                onFinishedHandler.handle(null);
                return;
            }

            Log.d("ServerConnection", "bookingState received " + bookingState);

            onFinishedHandler.handle(new CarStateTuple(chargingState, bookingState));
            return;
        });
    }

    @Override
    public void hasBooked(int carId, String chipCardUid, OnFinishedHandler<Integer> onFinishedHandler)
    {
        getTrips(carId, (trips) ->
        {
            if (trips == null)
            {
                onFinishedHandler.handle(null);
                return;
            }

            try
            {
                for (int i = 0; i < trips.length(); i++)
                {
                    JSONObject trip = trips.getJSONObject(i);
                    int tripId = trip.getInt("tripId");

                    String endDate = trip.getString("endDate");

                    // trip is new
                    if (endDate.equals("null"))
                    {
                        String bookedCustomerId = trip.getString("customerId");
                        getChipCardUidFromCustomerId(bookedCustomerId, (chipCardUidOfBooked) ->
                        {
                            if (chipCardUidOfBooked == null)
                            {
                                onFinishedHandler.handle(null);
                                return;
                            }

                            if (chipCardUidOfBooked.equals(chipCardUid))
                            {
                                onFinishedHandler.handle(tripId);
                                return;
                            }
                            else
                            {
                                Logger.getInstance().logInfo("ChipCardUid not right Customer");
                                onFinishedHandler.handle(null);
                                return;
                            }
                        });
                        return;
                    }
                }
                // nothing found
                onFinishedHandler.handle(null);
                return;
            }
            catch (JSONException e)
            {
                e.printStackTrace();
                onFinishedHandler.handle(null);
                return;
            }
        });
    }

    @Override
    public void updateChargingState(int carId, ChargingState chargingState, OnFinishedHandler<Boolean> onFinishedHandler)
    {
        String patch;
        try
        {
            patch = "\"" + ChargingState.values()[chargingState.ordinal()] + "\"";
            ParametricThread<Boolean, String> thread = new ParametricThread<>((param) ->
            {
                String url = "https://api.ecruise.me/v1/cars/" + carId + "/chargingstate";
                RequestFuture<JSONObject> future = RequestFuture.newFuture();
                JsonStringRequest jsonObjectRequest = new JsonStringRequest
                        (Request.Method.PATCH, url, param, future, future)
                {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError
                    {
                        Map<String, String> params = new HashMap<>();
                        params.put("access_token", accessToken);
                        return params;
                    }
                };
                request(jsonObjectRequest);

                JSONObject response = null;
                int patchedCarId = -1;
                try
                {
                    response = future.get();
                    patchedCarId = response.getInt("id");
                    Logger.getInstance().logInfo("Patched ChargingState to " + param);
                    return true;
                }
                catch (InterruptedException | ExecutionException | JSONException e)
                {
                    e.printStackTrace();
                    Logger.getInstance().logError("Could not Patch ChargingState");
                    return false;
                }
            }, onFinishedHandler, patch);
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
            onFinishedHandler.handle(false);
            return;
        }
    }


    @Override
    public void validId(String chipCardUid, OnFinishedHandler<Boolean> onFinishedHandler)
    {
        getCustomerIdFromChipCardUid(chipCardUid, (result) ->
                {
                    if (result == null)
                    {
                        onFinishedHandler.handle(false);
                        return;
                    }
                    else
                    {
                        onFinishedHandler.handle(true);
                        return;
                    }
                }
        );
    }

    @Override
    public void createTrip(String chipCardUid, int carId, OnFinishedHandler<Integer> onFinishedHandler)
    {
        getCustomerIdFromChipCardUid(chipCardUid, (result) ->
        {
            if (result == null)
            {
                onFinishedHandler.handle(null);
                return;
            }

            JSONObject trip = new JSONObject();

            getCarChargingStation(carId, (carChargingStation) ->
            {
                try
                {
                    trip.put("tripId", null);
                    trip.put("carId", carId);
                    trip.put("customerId", result);
                    trip.put("startDate", new JsonDate(Calendar.getInstance()).getString());
                    trip.put("endDate", null);

                    if (carChargingStation == null)
                    {
                        Logger.getInstance().logWarning("startChargingStation is " + 1);
                        trip.put("startChargingStationId", 1);
                    }
                    else
                    {
                        Logger.getInstance().logInfo("startChargingStation is " +
                                carChargingStation.getInt("chargingStationId"));
                        trip.put("startChargingStationId", carChargingStation.getInt("chargingStationId"));
                    }

                    trip.put("endChargingStationId", null);
                    trip.put("distanceTravelled", null);

                    ParametricThread<Integer, JSONObject> thread = new ParametricThread<>((param) ->
                    {
                        String url = "https://api.ecruise.me/v1/trips";

                        RequestFuture<JSONObject> tripFuture = RequestFuture.newFuture();
                        JsonObjectRequest jsonTripObjectRequest = new JsonObjectRequest
                                (Request.Method.POST, url, param, tripFuture, tripFuture)
                        {
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError
                            {
                                Map<String, String> params = new HashMap<>();
                                params.put("access_token", accessToken);
                                return params;
                            }
                        };
                        request(jsonTripObjectRequest);

                        JSONObject tripResponse = null;
                        int tripId = -1;
                        try
                        {
                            tripResponse = tripFuture.get();
                            tripId = tripResponse.getInt("id");
                            Logger.getInstance().logInfo("Trip created with ID " + tripId);
                        }
                        catch (InterruptedException | ExecutionException | JSONException e)
                        {
                            e.printStackTrace();
                            Logger.getInstance().logError("Trip not created");
                            return null;
                        }
                        return tripId == -1 ? null : tripId;
                    }, (tripId) ->
                    {
                        if (tripId == null)
                        {
                            onFinishedHandler.handle(null);
                            return;
                        }

                        createBooking(result, tripId, (bookingId) ->
                        {
                            if (bookingId == null)
                            {
                                onFinishedHandler.handle(null);
                                return;
                            }
                            onFinishedHandler.handle(tripId);
                        });

                    }, trip);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                    onFinishedHandler.handle(null);
                    return;
                }
            });
        });
    }

    @Override
    public void endTrip(int carId, int tripId, int distanceTravelled, OnFinishedHandler<Integer> onFinishedHandler)
    {

        getRandomFreeChargingStation((chargingStation) ->
        {

            if (chargingStation != null)
            {
                ParametricThread<Integer, int[]> thread = new ParametricThread<>((param) ->
                {

                    String url = "https://api.ecruise.me/v1/trips/" + param[1];

                    JSONObject patch = new JSONObject();
                    try
                    {
                        patch.put("DistanceTravelled", param[2]);
                        patch.put("EndChargingStationId", chargingStation.getInt("chargingStationId"));

                        RequestFuture<JSONObject> future = RequestFuture.newFuture();
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                                (Request.Method.PATCH, url, patch, future, future)
                        {
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError
                            {
                                Map<String, String> params = new HashMap<>();
                                params.put("access_token", accessToken);
                                return params;
                            }
                        };
                        request(jsonObjectRequest);

                        JSONObject result = null;
                        int patchedTripId = -1;
                        try
                        {
                            result = future.get(); // this will block
                            patchedTripId = result.getInt("id");
                            Logger.getInstance().logInfo("Patched trip " + patchedTripId + " to end");

                            updatePosition(param[0], chargingStation.getDouble("latitude"),
                                    chargingStation.getDouble("longitude"), (success) ->
                                    {
                                    });

                            return patchedTripId;
                        }
                        catch (InterruptedException | JSONException | ExecutionException e)
                        {
                            e.printStackTrace();
                            Logger.getInstance().logError("Trip not ended with ID " + param[0]);
                            return null;
                        }
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                        Logger.getInstance().logError("Trip not ended with ID " + param[0]);
                        return null;
                    }
                }, onFinishedHandler, new int[]{carId, tripId, distanceTravelled});
            }
            else
            {
                onFinishedHandler.handle(null);
            }
        });
    }

    @Override
    public void updatePosition(int carId, double latitude, double longitude, OnFinishedHandler<Boolean> onFinishedHandler)
    {
        ParametricThread<Boolean, Object[]> thread = new ParametricThread<>((param) ->
        {
            String url = "https://api.ecruise.me/v1/cars/" + Integer.toString((int) param[0]) + "/position/"
                    + Double.toString((double) param[1]) + "/" + Double.toString((double) param[2]);

            RequestFuture<JSONObject> future = RequestFuture.newFuture();
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.PATCH, url, null, future, future)
            {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError
                {
                    Map<String, String> params = new HashMap<>();
                    params.put("access_token", accessToken);
                    return params;
                }
            };
            request(jsonObjectRequest);

            try
            {
                JSONObject result = future.get();
                Logger.getInstance().logInfo("Patched Position to " + Double.toString((double) param[1])
                        + "N " + Double.toString((double) param[2]) + "E");
            }
            catch (InterruptedException | ExecutionException e)
            {
                e.printStackTrace();
                Logger.getInstance().logError("Could not patch Position");
                return false;
            }

            return true;
        }, onFinishedHandler, new Object[]{carId, latitude, longitude});
    }

    @Override
    public void updateChargeLevel(int carId, double chargeLevel, OnFinishedHandler<Boolean> onFinishedHandler)
    {
        ParametricThread<Boolean, Object[]> thread = new ParametricThread<>((param) ->
        {
            String url = "https://api.ecruise.me/v1/cars/" + Integer.toString((int) param[0]) + "/chargelevel";

            String patchString = "\"" + Double.toString((double) param[1]) + "\"";

            RequestFuture<JSONObject> future = RequestFuture.newFuture();
            JsonStringRequest jsonObjectRequest = new JsonStringRequest
                    (Request.Method.PATCH, url, patchString, future, future)
            {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError
                {
                    Map<String, String> params = new HashMap<>();
                    params.put("access_token", accessToken);
                    return params;
                }
            };
            request(jsonObjectRequest);

            try
            {
                JSONObject result = future.get();
                Logger.getInstance().logInfo("Patched ChargeLevel to " + Double.toString((double) param[1]) + "%");
            }
            catch (InterruptedException | ExecutionException e)
            {
                e.printStackTrace();
                Logger.getInstance().logError("Could not patch ChargeLevel");
                return false;
            }

            return true;
        }, onFinishedHandler, new Object[]{carId, chargeLevel});
    }

    @Override
    public void hasPositionRequest(int carId, OnFinishedHandler<Boolean> onFinishedHandler)
    {
        ParametricThread<Boolean, Integer> thread = new ParametricThread<>((param) ->
        {
            String url = "https://api.ecruise.me/v1/cars/" + param + "/is-wanted";

            RequestFuture<JSONObject> future = RequestFuture.newFuture();
            StatusRequest statusRequest = new StatusRequest
                    (Request.Method.GET, url, null, future, future)
            {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError
                {
                    Map<String, String> params = new HashMap<>();
                    params.put("access_token", accessToken);
                    return params;
                }
            };

            request(statusRequest);

            JSONObject positionRequest = null;
            try
            {
                positionRequest = future.get();
                String code = positionRequest.getString("code");

                if (code.equals("200"))
                {
                    Logger.getInstance().logInfo("is-wanted: " + code);
                    return true;
                }

                Logger.getInstance().logInfo("not is-wanted: " + code);
                return false;
            }
            catch (InterruptedException | ExecutionException | JSONException e)
            {
                e.printStackTrace();
            }
            Logger.getInstance().logError("Can't ask for position request");
            return false;
        }, onFinishedHandler, carId);
    }

    private void authenticate(OnFinishedHandler<Boolean> onFinishedHandler)
    {
        ParametricThread<Boolean, Void> thread = new ParametricThread<>((x) ->
        {
            String url = "https://api.ecruise.me/v1/public/login/" + AUTH_EMAIL;
            String stringRequest = "\"" + AUTH_PASSWORD + "\"";
            RequestFuture<JSONObject> future = RequestFuture.newFuture();
            JsonStringRequest jsonStringRequest = new JsonStringRequest(url, stringRequest, future, future);

            request(jsonStringRequest);

            try
            {
                JSONObject response = future.get(5, TimeUnit.SECONDS);
                accessToken = response.getString("token");

                Log.d("TOKEN", accessToken);
            }
            catch (ExecutionException | JSONException | InterruptedException | TimeoutException e1)
            {
                e1.printStackTrace();
                Logger.getInstance().logError("Authentication failure");
                return false;
            }
            Logger.getInstance().logInfo("Authenticated on https://api.ecruise.me");
            return true;
        }, onFinishedHandler, null);
    }

    private void getCustomerIdFromChipCardUid(String chipCardUid, OnFinishedHandler<Integer> onFinishedHandler)
    {
        ParametricThread<Integer, String> thread = new ParametricThread<>((param) ->
        {
            String url = "https://api.ecruise.me/v1/customers/";

            RequestFuture<JSONArray> future = RequestFuture.newFuture();
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                    (Request.Method.GET, url, null, future, future)
            {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError
                {
                    Map<String, String> params = new HashMap<>();
                    params.put("access_token", accessToken);
                    return params;
                }
            };

            request(jsonArrayRequest);

            JSONArray customers = null;
            try
            {
                customers = future.get();

                for (int i = 0; i < customers.length(); i++)
                {
                    JSONObject customer = customers.getJSONObject(i);
                    if (customer.getString("chipCardUid").equals(chipCardUid))
                    {
                        int customerId = customer.getInt("customerId");
                        Logger.getInstance().logInfo("ChipCardUid " + chipCardUid
                                + " <=> CustomerId " + customerId + " found");
                        return customerId;
                    }
                }
            }
            catch (JSONException | InterruptedException | ExecutionException e)
            {
                e.printStackTrace();
            }
            Logger.getInstance().logInfo("ChipCardUid " + chipCardUid + " <=> no Customer found");
            return null;
        }, onFinishedHandler, chipCardUid);
    }

    private void getChipCardUidFromCustomerId(String customerId, OnFinishedHandler<String> onFinishedHandler)
    {
        ParametricThread<String, String> thread = new ParametricThread<>((param) ->
        {
            String url = "https://api.ecruise.me/v1/customers/" + param;

            RequestFuture<JSONObject> future = RequestFuture.newFuture();
            JsonObjectRequest jsonArrayRequest = new JsonObjectRequest
                    (Request.Method.GET, url, null, future, future)
            {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError
                {
                    Map<String, String> params = new HashMap<>();
                    params.put("access_token", accessToken);
                    return params;
                }
            };
            request(jsonArrayRequest);

            JSONObject customer = null;
            try
            {
                customer = future.get();
                String chipCardUid = customer.getString("chipCardUid");

                if (chipCardUid != null && !chipCardUid.equals("null"))
                {
                    Logger.getInstance().logInfo("CustomerId " + param + " <=> ChipCardUid " + chipCardUid + " found");
                }
                else
                {
                    Logger.getInstance().logInfo("CustomerId " + param + " <=> no ChipCardUid found");
                }
                return chipCardUid;
            }
            catch (JSONException | NullPointerException | InterruptedException | ExecutionException e)
            {
                e.printStackTrace();
            }
            Logger.getInstance().logError("CustomerId " + param + " <=> no ChipCardUid found");
            return null;
        }, onFinishedHandler, customerId);
    }

    private void getCarById(int carId, OnFinishedHandler<JSONObject> onFinishedHandler)
    {
        ParametricThread<JSONObject, Integer> thread = new ParametricThread<>((param) ->
        {
            String url = "https://api.ecruise.me/v1/cars/" + param;
            RequestFuture<JSONObject> future = RequestFuture.newFuture();
            JsonObjectRequest request = new JsonObjectRequest
                    (Request.Method.GET, url, null, future, future)
            {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError
                {
                    Map<String, String> params = new HashMap<>();
                    params.put("access_token", accessToken);
                    return params;
                }
            };
            request(request);

            try
            {
                JSONObject car = future.get();
                double chargeLevel = car.getDouble("chargeLevel");
                Logger.getInstance().logInfo("Car" + param + " \uD83D\uDD0B"
                        + new DecimalFormat("#.#").format(chargeLevel) + "%");
                return car;
            }
            catch (InterruptedException | ExecutionException | JSONException e)
            {
                e.printStackTrace();
            }
            Logger.getInstance().logError("Car " + param + " not found");
            return null;
        }, onFinishedHandler, carId);
    }

    private void getTrips(int carId, OnFinishedHandler<JSONArray> onFinishedHandler)
    {
        ParametricThread<JSONArray, Integer> thread = new ParametricThread<>((param) ->
        {
            String url = "https://api.ecruise.me/v1/trips/by-car/" + param;

            RequestFuture<JSONArray> future = RequestFuture.newFuture();
            JsonArrayRequest request = new JsonArrayRequest
                    (Request.Method.GET, url, null, future, future)
            {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError
                {
                    Map<String, String> params = new HashMap<>();
                    params.put("access_token", accessToken);
                    return params;
                }
            };
            request(request);

            try
            {
                JSONArray trips = future.get();
                Logger.getInstance().logInfo("Trips for car " + param + " pulled");
                return trips;
            }
            catch (InterruptedException | ExecutionException e)
            {
                e.printStackTrace();
            }
            Logger.getInstance().logError("Trips for car " + param + " not found");
            return null;
        }, onFinishedHandler, carId);
    }

    private void createBooking(int customerId, int tripId, OnFinishedHandler<Integer> onFinishedHandler)
    {
        JSONObject booking = new JSONObject();
        try
        {
            booking.put("bookingId", null);
            booking.put("customerId", customerId);
            booking.put("tripId", tripId);
            booking.put("invoiceItemId", null);
            booking.put("bookingPositionLatitude", 49.488085);
            booking.put("bookingPositionLongitude", 8.462774);
            booking.put("bookingDate", new JsonDate(Calendar.getInstance()).getString());
            booking.put("plannedDate", null);

            ParametricThread<Integer, JSONObject> thread = new ParametricThread<>((param) ->
            {
                String url = "https://api.ecruise.me/v1/bookings";
                RequestFuture<JSONObject> future = RequestFuture.newFuture();
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                        (Request.Method.POST, url, param, future, future)
                {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError
                    {
                        Map<String, String> params = new HashMap<>();
                        params.put("access_token", accessToken);
                        return params;
                    }
                };
                request(jsonObjectRequest);

                JSONObject response = null;
                int bookingId = -1;
                try
                {
                    response = future.get();
                    bookingId = response.getInt("id");
                    Logger.getInstance().logInfo("Booking created with ID " + bookingId);
                    return bookingId;
                }
                catch (InterruptedException | ExecutionException | JSONException e)
                {
                    e.printStackTrace();
                    Logger.getInstance().logError("Booking not created");
                    return null;
                }
            }, onFinishedHandler, booking);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            onFinishedHandler.handle(null);
            return;
        }
    }

    private void getCarChargingStation(int carId, OnFinishedHandler<JSONObject> onFinishedHandler)
    {
        ParametricThread<JSONObject, Integer> thread = new ParametricThread<>((param) ->
        {
            String url = "https://api.ecruise.me/v1/car-charging-stations/by-car/" + param;

            RequestFuture<JSONArray> future = RequestFuture.newFuture();
            JsonArrayRequest request = new JsonArrayRequest
                    (Request.Method.GET, url, null, future, future)
            {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError
                {
                    Map<String, String> params = new HashMap<>();
                    params.put("access_token", accessToken);
                    return params;
                }
            };
            request(request);

            try
            {
                JSONArray trips = future.get();
                Logger.getInstance().logInfo("Car-charging-stations for car " + param + " pulled");
                return trips.getJSONObject(trips.length() - 1);
            }
            catch (InterruptedException | ExecutionException | JSONException e)
            {
                e.printStackTrace();
            }
            Logger.getInstance().logError("Car-charging-stations for car " + param + " not found");
            return null;
        }, onFinishedHandler, carId);
    }

    private void getRandomFreeChargingStation(OnFinishedHandler<JSONObject> onFinishedHandler)
    {
        ParametricThread<JSONObject, Void> thread = new ParametricThread<>((empty) ->
        {
            String url = "https://api.ecruise.me/v1/charging-stations";

            RequestFuture<JSONArray> future = RequestFuture.newFuture();
            JsonArrayRequest request = new JsonArrayRequest
                    (Request.Method.GET, url, null, future, future)
            {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError
                {
                    Map<String, String> params = new HashMap<>();
                    params.put("access_token", accessToken);
                    return params;
                }
            };
            request(request);

            try
            {
                JSONArray chargingStations = future.get();
                JSONArray freeStations = new JSONArray();

                for (int i = 0; i < chargingStations.length(); i++)
                {
                    JSONObject candidate = chargingStations.getJSONObject(i);
                    if (candidate.getInt("slotsOccupied") < candidate.getInt("slots"))
                    {
                        freeStations.put(candidate);
                    }
                }

                if (freeStations.length() == 0)
                {
                    Logger.getInstance().logError("No free ChargingStations for ending trip. Keep driving");
                }

                JSONObject randomFreeStation = freeStations.getJSONObject(new Random().nextInt(freeStations.length()));

                Logger.getInstance().logInfo("ChargingStation " + randomFreeStation.getInt("chargingStationId")
                        + " randomly picked from " + freeStations.length() + " free Stations for ending trip");
                return randomFreeStation;
            }
            catch (InterruptedException | ExecutionException | JSONException e)
            {
                e.printStackTrace();
            }
            Logger.getInstance().logError("No ChargingStations found");
            return null;
        }, onFinishedHandler, null);
    }
}
