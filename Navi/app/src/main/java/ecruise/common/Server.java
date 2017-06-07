package ecruise.common;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;

import com.android.volley.toolbox.ImageLoader;

import com.android.volley.toolbox.Volley;


public class Server
{
    private static Server mInstance;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private static Context mCtx;

    /**
     *  Constructor
     **/
    private Server(Context context)
    {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }

    /**
     *  This method create a synchronized instance of Server depending on context
     *
     *  @param context context form Activity
     *
     *  @return server instance
     **/
    public static synchronized Server getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new Server(context);
        }
        return mInstance;
    }


    /**
     *  This method create a synchronized instance of Server depending on context
     *
     *  @return srequest Queue for Volley
     **/
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    /**
     *  This method add the request to request Que
     *
     *  @param req request for que adding
     *
     **/
    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public boolean checkLoginData(String email, String password) {
        return true;
    }

}