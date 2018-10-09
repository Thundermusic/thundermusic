package fr.litarvan.thundermusic.core;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

public class EventManager
{
    private Queue<JSONObject> queued;
    private EventListener nextListener;

    public EventManager()
    {
        this.queued = new LinkedList<>();
    }

    public void listen(EventListener next)
    {
        this.nextListener = next;

        if (!queued.isEmpty()) {
            nextListener.accept(queued.poll());
        }
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
        if (!queued.isEmpty() || nextListener == null || nextListener.emitted) {
            queued.offer(event);
            return;
        }

        nextListener.accept(event);
    }

    public static abstract class EventListener
    {
        private boolean emitted;

        public void accept(JSONObject event) {
            this.emitted = true;
        };

        public boolean isEmitted()
        {
            return emitted;
        }
    }
}
