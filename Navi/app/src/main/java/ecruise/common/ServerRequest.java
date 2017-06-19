package ecruise.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.LauncherApps;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by Aivan on 17.05.2017.
 */

public class ServerRequest
{
    public ServerRequest(Context ctx, String token)
    {
        mCtx = ctx;
        this.token = token;
    }

    private Context mCtx;
    private String token;

    /**
     *  This method call server for a json object
     *
     *  @param callback the callback object, given to the activity
     *
     *  @return json object
     **/
   /* public void generateJsonObject(String url, VolleyCallbackObject callback) throws JSONException {
        final VolleyCallbackObject mCallback = callback;
        getToken();
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response)
                    {
                        mCallback.onSuccess(response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        error.printStackTrace();
                    }
                })
                {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError
                    {
                        Map<String, String> params = new HashMap<>();
                        params.put("access_token", token);
                        return params;
                    }
                };

       Server.getInstance(mCtx).addToRequestQueue(jsObjRequest);
    }*/

   public void getToken(VolleyCallbackObject callback)
   {
       String url = "https://api.ecruise.me/v1/public/login/admin@ecruise.me";
       final VolleyCallbackObject mCallback = callback;
       String stringRequest = "\"" + "ecruiseAdmin123!!!" + "\"";
       RequestFuture<JSONObject> future = RequestFuture.newFuture();
       JsonStringRequest jsObjRequest = new JsonStringRequest
               (Request.Method.POST, url, stringRequest, new Response.Listener<JSONObject>()
               {
                   @Override
                   public void onResponse(JSONObject response)
                   {
                       mCallback.onSuccess(response);
                   }
               }, new Response.ErrorListener() {

                   @Override
                   public void onErrorResponse(VolleyError error)
                   {
                       error.printStackTrace();
                   }
               });

       Log.d("SR", "Added to que");
       Server.getInstance(mCtx).addToRequestQueue(jsObjRequest);
   }


    /**
     *  This method call server for a json Array object
     *
     *  @param url given Url to call
     *  @param callback the callback object, given to the activity
     *
     *  @return json Array object
     **/
    public void generateJsonArray(String url, VolleyCallbackArray callback) throws JSONException {
        final VolleyCallbackArray mCallback = callback;
        JsonArrayRequest jsArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>()
                {
                    @Override
                    public void onResponse(JSONArray response)
                    {
                        Log.d("ONSUCCESS ARRAY", response.toString());
                        if(response != null)
                            mCallback.onSuccess(response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        error.printStackTrace();
                    }
                })
                {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError
                    {
                        Map<String, String> params = new HashMap<>();
                        Log.d("TOKEN2", token);
                        params.put("access_token", token);
                        return params;
                    }
                };

        Log.d("SR", "Added to que");
        Server.getInstance(mCtx).addToRequestQueue(jsArrayRequest);
    }


    public interface VolleyCallbackArray
    {
        void onSuccess(JSONArray result);
    }

    public interface VolleyCallbackObject
    {
        void onSuccess(JSONObject result);
    }
}
