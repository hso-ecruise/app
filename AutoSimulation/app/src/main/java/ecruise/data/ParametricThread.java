package ecruise.data;

import android.os.AsyncTask;

/**
 * Created by Tom on 17.06.2017.
 */

class ParametricThread<Result, Param>
{
    private AsyncCall<Result, Param> asyncCall;
    private OnFinishedHandler<Result> onFinishedHandler;

    ParametricThread(AsyncCall<Result, Param> asyncCall, OnFinishedHandler<Result> onFinishedHandler, Param param)
    {
        this.asyncCall = asyncCall;
        this.onFinishedHandler = onFinishedHandler;
        startAsyn(param);
    }

    private void startAsyn(Param param)
    {
        CustomThread thread = new CustomThread(param);
        thread.execute();
    }

    private class CustomThread extends AsyncTask<Void, Void, Void>
    {
        Param param;
        Result result;

        CustomThread(Param param)
        {
            this.param = param;
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            result = asyncCall.operation(param);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            onFinishedHandler.handle(result);
            super.onPostExecute(aVoid);
        }
    }
}
