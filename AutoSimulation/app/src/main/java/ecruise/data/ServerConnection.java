package ecruise.data;

import android.content.Context;
import android.util.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import ecruise.logic.JsonDate;
import ecruise.logic.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
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

    private <T> void request(final Request<T> req)
    {
        Logger.getInstance().log(req.toString());
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
                                Logger.getInstance().log("ChipCardUid not right Customer");
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
                    Logger.getInstance().log("Patched ChargingState to " + param);
                    return true;
                }
                catch (InterruptedException | ExecutionException | JSONException e)
                {
                    e.printStackTrace();
                    Logger.getInstance().log("Could not Patch ChargingState (Error)");
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
    public void createTrip(String chipCardUid, int carId, int startChargingStationId, OnFinishedHandler<Integer> onFinishedHandler)
    {
        getCustomerIdFromChipCardUid(chipCardUid, (result) ->
        {
            if (result == null)
            {
                onFinishedHandler.handle(null);
                return;
            }

            JSONObject trip = new JSONObject();

            try
            {
                trip.put("tripId", 0);
                trip.put("carId", carId);
                trip.put("customerId", result);
                trip.put("startDate", new JsonDate(Calendar.getInstance()).getString());
                trip.put("endDate", null);
                trip.put("startChargingStationId", startChargingStationId);
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
                        Logger.getInstance().log("Trip created with ID " + tripId);
                    }
                    catch (InterruptedException | ExecutionException | JSONException e)
                    {
                        e.printStackTrace();
                        Logger.getInstance().log("Trip not created (Error)");
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
    }

    @Override
    public void endTrip(int tripId, int distanceTravelled, int endChargingStationId, OnFinishedHandler<Integer> onFinishedHandler)
    {
        ParametricThread<Integer, int[]> thread = new ParametricThread<>((param) ->
        {
            String url = "https://api.ecruise.me/v1/trips/" + param[0];

            JSONObject patch = new JSONObject();
            try
            {
                patch.put("DistanceTravelled", param[1]);
                patch.put("EndChargingStationId", param[2]);

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
                    Logger.getInstance().log("Patched trip " + patchedTripId + " to end");

                    return patchedTripId;
                }
                catch (InterruptedException | JSONException | ExecutionException e)
                {
                    e.printStackTrace();
                    Logger.getInstance().log("Trip not ended with ID " + param[0] + " (Error)");
                    return null;
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
                Logger.getInstance().log("Trip not ended with ID " + param[0] + " (Error)");
                return null;
            }
        }, onFinishedHandler, new int[]{tripId, distanceTravelled, endChargingStationId});
    }

    @Override
    public void updatePosition(int carId, double latitude, double longitude, OnFinishedHandler<Boolean> onFinishedHandler)
    {
        ParametricThread<Boolean, Object[]> thread = new ParametricThread<>((param) ->
        {
            String url = "https://api.ecruise.me/v1/cars/" + Integer.toString((int) param[0]) + "/position/" + Double.toString((double) param[1]) + "/" + Double.toString((double) param[2]);

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
                Logger.getInstance().log("Patched Position to " + Double.toString((double) param[1]) + "N " + Double.toString((double) param[2]) + "E");
            }
            catch (InterruptedException | ExecutionException e)
            {
                e.printStackTrace();
                Logger.getInstance().log("Could not patch Position (Error)");
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
                Logger.getInstance().log("Patched ChargeLevel to " + Double.toString((double) param[1]) + "%");
            }
            catch (InterruptedException | ExecutionException e)
            {
                e.printStackTrace();
                Logger.getInstance().log("Could not patch ChargeLevel (Error)");
                return false;
            }

            return true;
        }, onFinishedHandler, new Object[]{carId, chargeLevel});
    }

    @Override
    public void hasPositionRequest(int carId, OnFinishedHandler<Boolean> onFinishedHandler)
    {
        // TODO: Implement when Backend is ready

        ParametricThread<Boolean, Integer> thread = new ParametricThread<>((param) ->
        {
            String url = "https://api.ecruise.me/v1/" + param;

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

            JSONObject positionRequest = null;
            try
            {
                positionRequest = future.get();
                boolean hasOne = positionRequest.getBoolean("hasOne");
                Logger.getInstance().log("Get Position request: " + hasOne);
                return hasOne;
            }
            catch (JSONException | InterruptedException | ExecutionException e)
            {
                e.printStackTrace();
            }
            Logger.getInstance().log("Can't ask for position request (Error)");
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
            JsonStringRequest jsObjRequest = new JsonStringRequest(url, stringRequest, future, future);

            String value = null;
            try
            {
                value = new String(jsObjRequest.getBody(), "UTF-8");
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }

            request(jsObjRequest);

            try
            {
                JSONObject response = future.get(5, TimeUnit.SECONDS);
                accessToken = response.getString("token");

                Log.d("TOKEN", accessToken);
            }
            catch (ExecutionException | JSONException | InterruptedException | TimeoutException e1)
            {
                e1.printStackTrace();
                Logger.getInstance().log("Authentication failure");
                return false;
            }
            Logger.getInstance().log("Authenticated on api.ecruise.me");
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
                        Logger.getInstance().log("ChipCardUid " + chipCardUid + " <=> CustomerId " + customerId + " found");
                        return customerId;
                    }
                }
            }
            catch (JSONException | InterruptedException | ExecutionException e)
            {
                e.printStackTrace();
            }
            Logger.getInstance().log("ChipCardUid " + chipCardUid + " <=> no Customer found");
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
                Logger.getInstance().log("CustomerId " + param + " <=> ChipCardUid " + chipCardUid + " found");
                return chipCardUid;
            }
            catch (JSONException | NullPointerException | InterruptedException | ExecutionException e)
            {
                e.printStackTrace();
            }
            Logger.getInstance().log("CustomerId " + param + " <=> no ChipCardUid found (Error)");
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
                Logger.getInstance().log("Car " + param + " pulled");
                return car;
            }
            catch (InterruptedException | ExecutionException e)
            {
                e.printStackTrace();
            }
            Logger.getInstance().log("Car " + param + " not found (Error)");
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
                Logger.getInstance().log("Trips for car " + param + " pulled");
                return trips;
            }
            catch (InterruptedException | ExecutionException e)
            {
                e.printStackTrace();
            }
            Logger.getInstance().log("Trips for car " + param + " not found (Error)");
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
                    Logger.getInstance().log("Booking created with ID " + bookingId);
                    return bookingId;
                }
                catch (InterruptedException | ExecutionException | JSONException e)
                {
                    e.printStackTrace();
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
}
