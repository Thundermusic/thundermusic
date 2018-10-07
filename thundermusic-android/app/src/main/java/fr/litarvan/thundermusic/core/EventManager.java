package fr.litarvan.thundermusic.core;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

public class EventManager
{
    private EventListener nextListener;

    public void listen(EventListener next)
    {
        this.nextListener = next;
    }

    public void error(String error)
    {
        try {
            JSONObject obj = new JSONObject();
            obj.put("type", "error");
            obj.put("error", error);

            emit(obj);
        } catch (JSONException e) {
            Log.e("TM-EventManager", "Couldn't send error JSON event", e);
        }
    }

    public void emit(JSONObject event)
    {
        if (nextListener != null) {
            nextListener.accept(event);
        }
    }

    public static abstract class EventListener
    {
        public abstract void accept(JSONObject event);
    }
}
