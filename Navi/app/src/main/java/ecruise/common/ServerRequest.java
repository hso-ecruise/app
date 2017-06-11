package ecruise.common;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Aivan on 17.05.2017.
 */

public class ServerRequest
{
    public ServerRequest(Context ctx)
    {
        mCtx = ctx;
    }

    private Context mCtx;

    /**
     *  This method call server for a json object
     *
     *  @param url given Url to call
     *  @param callback the callback object, given to the activity
     *
     *  @return json object
     **/
    /*public void generateJsonObject(String url, VolleyCallbackObject callback)
    {
        final VolleyCallbackObject mCallback = callback;
        final String mToken = "01aebbbdcbcf50a9b8ae1f8cf121aa2ea2bf3d2556467a7d822cea437b361780b2a2dc19a95ab7f8c91eb830e91c29e3095484588f3532f6ef7a000bb643940b";
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
                        params.put("access_token", mToken);
                        return params;
                    }
                };

       Server.getInstance(mCtx).addToRequestQueue(jsObjRequest);
    }*/


    /**
     *  This method call server for a json Array object
     *
     *  @param url given Url to call
     *  @param callback the callback object, given to the activity
     *
     *  @return json Array object
     **/
    public void generateJsonArray(String url, VolleyCallbackArray callback)
    {
        final VolleyCallbackArray mCallback = callback;
        final String mToken = "cfda19711d1a74dde7697a30d2cff2afa49e86726d5394f160cda3680b30eb575b9643e4519a9ee17296373ec4e93eb744eadb29636eccc8669fbc1868810d4b";
        JsonArrayRequest jsArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>()
                {
                    @Override
                    public void onResponse(JSONArray response)
                    {
                        if(response == null)
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
                        params.put("access_token", mToken);
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
