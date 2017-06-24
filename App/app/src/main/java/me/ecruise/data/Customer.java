package me.ecruise.data;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import me.ecruise.activitys.BookingActivity;
import me.ecruise.activitys.LoginActivity;


public class Customer {
    private static Customer mInstance;
    private static Context mCtx;
    private static int id;
    private static String token;

    //Customer Data
    private String name = "";
    private String lastname = "";
    private String email = "";
    private String phoneNumber = "";
    private String password = "";
    private String street = "";
    private String houseNumber = "";
    private String extraAddressLine = "";
    private int zipCode = 0;
    private String city = "";
    private String country = "";

    public Customer(Context context) {
        mCtx = context;
    }

    /**
     * @param context
     * @return
     */
    public static synchronized Customer getInstance(Context context) {
        if (mInstance == null && context != null) {
            mInstance = new Customer(context);
        }
        return mInstance;
    }

    //Getter and Setter
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public String getExtraAddressLine() {
        return extraAddressLine;
    }

    public void setExtraAddressLine(String extraAddressLine) {
        this.extraAddressLine = extraAddressLine;
    }

    public int getZipCode() {
        return zipCode;
    }

    public void setZipCode(int zipCode) {
        this.zipCode = zipCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    private boolean nameChanged = false;
    private boolean lastnameChanged = false;
    private boolean emailChanged = false;
    private boolean phoneNumberChanged = false;
    private boolean passwordChanged = false;
    private boolean streetChanged = false;
    private boolean houseNumberChanged = false;
    private boolean extraAddressLineChanged = false;
    private boolean zipCodeChanged = false;
    private boolean cityChanged = false;
    private boolean countryChanged = false;

    /**
     * checks if there is a difference to the newCustomer
     * @param newCustomer
     * @return if there are changes
     */
    public boolean checkForChanges(Customer newCustomer) {
        nameChanged = !newCustomer.name.isEmpty() && !this.name.equals(newCustomer.name);
        lastnameChanged = !newCustomer.lastname.isEmpty() && !this.lastname.equals(newCustomer.lastname);
        emailChanged = !newCustomer.email.isEmpty() && !this.email.equals(newCustomer.email);
        phoneNumberChanged = !newCustomer.phoneNumber.isEmpty() && !this.phoneNumber.equals(newCustomer.phoneNumber);
        passwordChanged = !newCustomer.password.isEmpty() && !this.password.equals(newCustomer.password);
        streetChanged = !newCustomer.street.isEmpty() && !this.street.equals(newCustomer.street);
        houseNumberChanged = !newCustomer.houseNumber.isEmpty() && !this.houseNumber.equals(newCustomer.houseNumber);
        extraAddressLineChanged = !newCustomer.extraAddressLine.isEmpty() && !this.extraAddressLine.equals(newCustomer.extraAddressLine);
        zipCodeChanged = !(newCustomer.zipCode == 0) && !(this.zipCode == newCustomer.zipCode);
        cityChanged = !newCustomer.city.isEmpty() && !this.city.equals(newCustomer.city);
        countryChanged = !newCustomer.country.isEmpty() && !this.country.equals(newCustomer.country);
        Log.d("nameChanged", String.valueOf(nameChanged));
        Log.d("lastnameChanged", String.valueOf(lastnameChanged));
        Log.d("emailChanged", String.valueOf(emailChanged));
        Log.d("phoneNumberChanged", String.valueOf(phoneNumberChanged));
        Log.d("passwordChanged", String.valueOf(passwordChanged));
        Log.d("streetChanged", String.valueOf(streetChanged));
        Log.d("houseNumberChanged", String.valueOf(houseNumberChanged));
        Log.d("extraAddressLineChanged", String.valueOf(extraAddressLineChanged));
        Log.d("zipCodeChanged", String.valueOf(zipCodeChanged));
        Log.d("cityChanged", String.valueOf(cityChanged));
        Log.d("countryChanged", String.valueOf(countryChanged));

        Log.d("allinall", String.valueOf(nameChanged |
                lastnameChanged |
                emailChanged |
                phoneNumberChanged |
                passwordChanged |
                streetChanged |
                houseNumberChanged |
                extraAddressLineChanged |
                zipCodeChanged |
                cityChanged |
                countryChanged));
        
        return (nameChanged |
                lastnameChanged |
                emailChanged |
                phoneNumberChanged |
                passwordChanged |
                streetChanged |
                houseNumberChanged |
                extraAddressLineChanged |
                zipCodeChanged |
                cityChanged |
                countryChanged);
    }

    /**
     * gets the user data from the server
     * @param callback to wait for the server communication
     */
    public void getUserDataFromServer(DataCallback callback) {
        final DataCallback mCallback = callback;
        final String mToken = Customer.getInstance(mCtx).getToken();
        String url = "https://api.ecruise.me/v1/customers/" + id;
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        setUserData(response);
                        mCallback.onSuccess();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("access_token", mToken);
                return params;
            }
        };
        Server.getInstance(mCtx).addToRequestQueue(jsObjRequest);
    }

    /**
     * saves the response in the singleton customer
     * @param response
     */
    public void setUserData(JSONObject response) {
        try {
            this.setCity(response.getString("city"));
            this.setCountry(response.getString("country"));
            this.setEmail(response.getString("email"));
            this.setExtraAddressLine(response.getString("addressExtraLine"));
            this.setHouseNumber(response.getString("houseNumber"));
            this.setLastname(response.getString("lastName"));
            this.setName(response.getString("firstName"));
            this.setPhoneNumber(response.getString("phoneNumber"));
            this.setStreet(response.getString("street"));
            this.setZipCode(response.getInt("zipCode"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * updates the user data
     * @return
     */
    public boolean updateUserData(Customer customer, DataAnswerCallback callback) {
        boolean updated = checkForChanges(customer);
        if (updated) {
            Log.d("UPDATED", "YES");
            String url;
            JSONObject jsonObject = new JSONObject();
            String param;
            //send data to server
            try {
                if (passwordChanged) {
                    //patch password
                    Log.d("PATCH", "password");
                    url = "/password";
                    param = "\"" + customer.getPassword() + "\"";
                    patchString(url, param, callback);
                }
                if (emailChanged) {
                    //patch email
                    Log.d("PATCH", "email");
                    url = "/email";
                    param = "\"" + customer.getEmail() + "\"";
                    patchString(url, param, callback);
                }
                if (phoneNumberChanged) {
                    //patch phonenumber
                    Log.d("PATCH", "phone-number");
                    url = "/phone-number";
                    param = "\"" + customer.getPhoneNumber() + "\"";
                    patchString(url, param, callback);
                }
                if (cityChanged || streetChanged || countryChanged || houseNumberChanged || extraAddressLineChanged || zipCodeChanged) {
                    //patch address
                    Log.d("PATCH", "address");
                    url = "/address";
                    if (countryChanged)
                        jsonObject.put("country", customer.getCountry());
                    else
                        jsonObject.put("country", this.getCountry());
                    if (cityChanged)
                        jsonObject.put("city", customer.getCity());
                    else
                        jsonObject.put("city", this.getCity());
                    if (zipCodeChanged)
                        jsonObject.put("zipCode", customer.getZipCode());
                    else
                        jsonObject.put("zipCode", this.getZipCode());
                    if (streetChanged)
                        jsonObject.put("street", customer.getStreet());
                    else
                        jsonObject.put("street", this.getStreet());
                    if (houseNumberChanged)
                        jsonObject.put("houseNumber", customer.getHouseNumber());
                    else
                        jsonObject.put("houseNumber", this.getHouseNumber());
                    if (extraAddressLineChanged)
                        jsonObject.put("extraAddressLine", customer.getExtraAddressLine());
                    else
                        jsonObject.put("extraAddressLine", this.getExtraAddressLine());
                    patchJSON(url, jsonObject, callback);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.d("UPDATED", "NO");
            callback.onFailure();
        }
        Log.d("RETURN", Boolean.toString(updated));
        return updated;
    }

    /**
     *
     * @param url
     * @param data
     * @param callback
     */
    public void patchString(String url, String data, final DataAnswerCallback callback) {
        final DataAnswerCallback mCallback = callback;
        final String mToken = Customer.getInstance(mCtx).getToken();
        final String mUrl = url;
        final String mData = data;
        Log.d("STRING", data);
        String reqUrl = "https://api.ecruise.me/v1/customers/" + id + url;
        JsonStringRequest jsObjRequest = new JsonStringRequest
                (Request.Method.PATCH, reqUrl, data, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("RESPONSE", response.toString());
                            if (id != response.getInt("id")) {
                                Log.d("PATCH_FAIL", response.getString("id"));
                                mCallback.onFailure();
                            } else {
                                Log.d("PATCH_SUCCESS", response.getString("id"));
                                mCallback.onSuccess(mUrl);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("access_token", mToken);
                return params;
            }
        };
        Server.getInstance(mCtx).addToRequestQueue(jsObjRequest);
    }

    /**
     *
     * @param url
     * @param data
     * @param callback
     */
    public void patchJSON(String url, JSONObject data, DataAnswerCallback callback) {
        final DataAnswerCallback mCallback = callback;
        final String mToken = Customer.getInstance(mCtx).getToken();
        final String mUrl = url;
        Log.d("JSON", data.toString());
        String reqUrl = "https://api.ecruise.me/v1/customers/" + id + url;
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.PATCH, reqUrl, data, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("RESPONSE", response.toString());
                            if (id != response.getInt("id")) {
                                Log.d("PATCH_FAIL", response.getString("id"));
                                mCallback.onFailure();
                            } else {
                                Log.d("PATCH_SUCCESS", response.getString("id"));
                                mCallback.onSuccess(mUrl);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("access_token", mToken);
                return params;
            }
        };
        Log.d("Request", jsObjRequest.toString());
        Server.getInstance(mCtx).addToRequestQueue(jsObjRequest);
    }

    /**
     *
     * @param callback
     */
    public void registerUser(DataCallback callback) {
        Log.d("FUNCTION", "registerUser");
        final DataCallback mCallback = callback;
        String url = "https://api.ecruise.me/v1/public/register";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", email);
            jsonObject.put("password", password);
            jsonObject.put("firstName", name);
            jsonObject.put("lastName", lastname);
            jsonObject.put("phoneNumber", phoneNumber);
            jsonObject.put("country", country);
            jsonObject.put("city", city);
            jsonObject.put("zipCode", zipCode);
            jsonObject.put("street", street);
            jsonObject.put("houseNumber", houseNumber);
            jsonObject.put("addressExtraLine", extraAddressLine);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("RESPONSE", response.toString());
                        mCallback.onSuccess();

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mCallback.onFailure();
                    }
                }) {
        };
        Log.d("ADD", "REQUEST");
        Log.d("Request", jsObjRequest.toString());
        Server.getInstance(mCtx).addToRequestQueue(jsObjRequest);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public interface DataCallback {
        void onSuccess();

        void onFailure();
    }

    public interface DataAnswerCallback {
        void onSuccess(String answer);

        void onFailure();
    }
}
