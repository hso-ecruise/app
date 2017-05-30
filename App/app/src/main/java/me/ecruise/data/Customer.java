package me.ecruise.data;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class Customer {
    private boolean loading;

    private static Customer mInstance;
    private static Context mCtx;
    private static int id;
    private static String token;

    //Customer Data
    private String name;
    private String lastname;
    private String email;
    private String phoneNumber;
    private String password;
    private String street;
    private String houseNumber;
    private String extraAddressLine;
    private String zipCode;
    private String city;
    private String country;

    public Customer(Context context) {
        mCtx = context;
    }

    public static synchronized Customer getInstance(Context context) {
        if (mInstance == null) {
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

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
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

    private boolean nameChanged;
    private boolean lastnameChanged;
    private boolean emailChanged;
    private boolean phoneNumberChanged;
    private boolean passwordChanged;
    private boolean streetChanged;
    private boolean houseNumberChanged;
    private boolean extraAddressLineChanged;
    private boolean zipCodeChanged;
    private boolean cityChanged;
    private boolean countryChanged;

    public boolean checkForChanges(Customer newCustomer) {
        boolean nameChanged = !newCustomer.name.isEmpty() && !this.name.equals(newCustomer.name);
        boolean lastnameChanged = !newCustomer.lastname.isEmpty() && !this.lastname.equals(newCustomer.lastname);
        boolean emailChanged = !newCustomer.email.isEmpty() && !this.email.equals(newCustomer.email);
        boolean phoneNumberChanged = !newCustomer.phoneNumber.isEmpty() && !this.phoneNumber.equals(newCustomer.phoneNumber);
        boolean passwordChanged = !newCustomer.password.isEmpty() && !this.password.equals(newCustomer.password);
        boolean streetChanged = !newCustomer.street.isEmpty() && !this.street.equals(newCustomer.street);
        boolean houseNumberChanged = !newCustomer.houseNumber.isEmpty() && !this.houseNumber.equals(newCustomer.houseNumber);
        boolean extraAddressLineChanged = !newCustomer.extraAddressLine.isEmpty() && !this.extraAddressLine.equals(newCustomer.extraAddressLine);
        boolean zipCodeChanged = !newCustomer.zipCode.isEmpty() && !this.zipCode.equals(newCustomer.zipCode);
        boolean cityChanged = !newCustomer.city.isEmpty() && !this.city.equals(newCustomer.city);
        boolean countryChanged = !newCustomer.country.isEmpty() && !this.country.equals(newCustomer.country);

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

    public void getUserDataFromServer(DataCallback callback) {
        final DataCallback mCallback = callback;
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
                });
        Server.getInstance(mCtx).addToRequestQueue(jsObjRequest);
    }

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
            this.setZipCode(response.getString("zipCode"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean updateUserData(Customer customer) {
        boolean updated = checkForChanges(customer);
        if (updated) {
            //send data to server
            if(passwordChanged) {
                //patch password
            }
            if(emailChanged) {
                //patch email
            }
            if(phoneNumberChanged) {
                //patch phonenumber
            }
            if(cityChanged||streetChanged||countryChanged||houseNumberChanged||extraAddressLineChanged||zipCodeChanged) {
                //patch address
            }
        }
        return updated;
    }

    public void registerUser()
    {
        String url = "https://api.ecruise.me/v1/customers/";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("Email", email);
            jsonObject.put("Password", password);
            jsonObject.put("FirstName", name);
            jsonObject.put("LastName", lastname);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            id = response.getInt("id");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                });
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

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public interface DataCallback {
        void onSuccess();
    }
}
