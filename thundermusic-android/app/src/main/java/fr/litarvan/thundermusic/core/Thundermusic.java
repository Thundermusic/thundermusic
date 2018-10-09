package fr.litarvan.thundermusic.core;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.util.Log;
import fr.litarvan.thundermusic.core.EventManager.EventListener;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Thundermusic extends CordovaPlugin
{
    public static final String VERSION = "1.0.0";
    public static final String API_URL = "https://api.thundermusic.litarvan.com/";

    private Messenger playerMessenger;
    private EventManager eventManager;
    private CallbackContext initCallback;

    private Runnable onConnected;

    private ServiceConnection playerConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            playerMessenger = new Messenger(iBinder);

            if (onConnected != null) {
                onConnected.run();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            playerMessenger = null;
        }
    };

    public Thundermusic()
    {
        eventManager = new EventManager();
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView)
    {
        Log.i("Thundermusic", "Thundermusic core v" + VERSION);
    }

    protected void init()
    {
        Intent intent = new Intent(webView.getContext(), ThundermusicService.class);
        intent.putExtra("receiver", new ResultReceiver(null)
        {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData)
            {
                JSONObject result = new JSONObject();

                switch (resultCode)
                {
                    case ThundermusicService.RSP_INITIALIZED:
                        initCallback.success();
                        break;
                    case ThundermusicService.RSP_ERROR:
                        String error = resultData.getString("error");

                        Log.e("Thundermusic", "Error from service : " + error);
                        eventManager.error(error);
                        break;
                    case ThundermusicService.RSP_POSITION:
                        try {
                            result.put("type", "position");
                            result.put("position", resultData.getInt("position"));
                            result.put("duration", resultData.getInt("duration"));

                            eventManager.emit(result);
                        } catch (JSONException e) {
                            Log.e("Thundermusic", "Error while sending position event", e);
                            eventManager.error("Error while sending position event : " + e.getMessage());
                        }

                        break;
                    case ThundermusicService.RSP_EVENT:
                        try {
                            eventManager.emit(new JSONObject(resultData.getString("event")));
                        } catch (JSONException e) {
                            Log.e("Thundermusic", "Error while sending event", e);
                            eventManager.error("Error while sending event : " + e.getMessage());
                        }

                        break;
                    case ThundermusicService.RSP_DOWNLOADS:
                        try {
                            JSONArray songs = new JSONArray(resultData.getString("downloads"));
                            JSONObject event = new JSONObject();
                            event.put("type", "downloads");
                            event.put("downloads", songs);

                            System.out.println("On part sur une emission de : " + songs.toString());
                            eventManager.emit(event);
                        } catch (JSONException e) {
                            Log.e("Thundermusic", "Error while sending downloads event", e);
                            eventManager.error("Error while sending downloads event : " + e.getMessage());
                        }

                        break;
                }
            }
        });
        webView.getContext().bindService(intent, playerConnection, Context.BIND_AUTO_CREATE);

        if (playerMessenger != null) {
            send(ThundermusicService.MSG_INIT, null);
        } else {
            onConnected = new Runnable()
            {
                @Override
                public void run()
                {
                    send(ThundermusicService.MSG_INIT, null);
                }
            };
        }
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException
    {
        Bundle bundle = new Bundle();

        switch (action)
        {
            case "init":
                initCallback = callbackContext;
                init();
                return true;
            case "listen":
                eventManager.listen(new EventListener()
                {
                    @Override
                    public void accept(JSONObject event)
                    {
                        callbackContext.success(event);
                    }
                });
                return true;
            case "download":
                System.out.println("On send le download");
                bundle.putString("song", args.getJSONObject(0).toString());
                send(ThundermusicService.MSG_DOWNLOAD, bundle);
                break;
            case "play":
                bundle.putString("song", args.getJSONObject(0).toString());
                send(ThundermusicService.MSG_PLAY, bundle);
                break;
            case "pause":
                send(ThundermusicService.MSG_PAUSE, null);
                break;
            case "next":
                send(ThundermusicService.MSG_NEXT, null);
                break;
            case "previous":
                send(ThundermusicService.MSG_PREVIOUS, null);
                break;
            case "seek":
                bundle.putInt("position", args.getInt(0));
                send(ThundermusicService.MSG_SEEK, bundle);
                break;
            case "update":
                bundle.putString("song", args.getJSONObject(0).toString());
                send(ThundermusicService.MSG_UPDATE, bundle);
                break;
            case "remove":
                bundle.putString("song", args.getJSONObject(0).toString());
                send(ThundermusicService.MSG_REMOVE, bundle);
                break;
            case "position":
                send(ThundermusicService.MSG_POSITION, null);
                return true;
            default:
                return false;
        }

        callbackContext.success();
        return true;
    }

    protected void send(int message, Bundle data)
    {
        /*String song = "";
        if (data != null) {
            song = data.getString("song");
            System.out.println("data : " + song);
        }*/

        Message msg = Message.obtain(null, message, 0, 0/*, song*/);
        if (data != null)
        {
            msg.setData(data);
        }

        try
        {
            playerMessenger.send(msg);
        }
        catch (RemoteException e)
        {
            Log.e("Thundermusic", "Error while sending message to service", e);
            eventManager.error("Error while sending message to service : " + e.getMessage());
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (playerConnection != null)
        {
            this.webView.getContext().unbindService(playerConnection);
        }
    }
}
