package me.ecruise.data;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;


public class ServerRequest
{
    public ServerRequest(Context ctx)
    {
        mCtx = ctx;
    }

    private Context mCtx;

    public void generateJsonObject(String url, VolleyCallbackObject callback)
    {
        final VolleyCallbackObject mCallback = callback;
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
                });

        Server.getInstance(mCtx).addToRequestQueue(jsObjRequest);
    }

    public void generateJsonArray(String url, VolleyCallbackArray callback)
    {
        final VolleyCallbackArray mCallback = callback;
        JsonArrayRequest jsArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>()
                {
                    @Override
                    public void onResponse(JSONArray response)
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