package fr.litarvan.thundermusic.core;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.*;
import android.util.Log;
import fr.litarvan.thundermusic.core.EventManager.EventListener;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.*;

public class Thundermusic extends CordovaPlugin
{
    public static final String VERSION = "1.0.0";

    private Messenger playerMessenger;
    private EventManager eventManager;
    private CallbackContext initCallback;

    private MusicManager musicManager;
    private DownloadManager downloadManager;

    private Runnable onConnected;

    private ServiceConnection playerConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            playerMessenger = new Messenger(iBinder);
            send(ThundermusicService.MSG_INIT, null);

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
        musicManager = new MusicManager(this.webView.getContext(), eventManager, new Runnable()
        {
            @Override
            public void run()
            {
                if (playerConnection != null) {
                    updateService();
                } else {
                    onConnected = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            updateService();
                        }
                    };
                }
            }
        });

        downloadManager = new DownloadManager(musicManager, eventManager);
        downloadManager.start();

        if (!cordova.hasPermission(READ_EXTERNAL_STORAGE) || !cordova.hasPermission(WRITE_EXTERNAL_STORAGE))
        {
            cordova.requestPermissions(this, 0, new String[] {READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE});
        }
        else
        {
            load();
        }

        Intent intent = new Intent(webView.getContext(), ThundermusicService.class);
        intent.putExtra("receiver", new ResultReceiver(null)
        {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData)
            {
                switch (resultCode)
                {
                    case ThundermusicService.RSP_ERROR:
                        String error = resultData.getString("error");

                        Log.e("Thundermusic", "Error from service : " + error);
                        eventManager.error(error);
                        break;
                    case ThundermusicService.RSP_POSITION:
                        JSONObject result = new JSONObject();
                        try {
                            result.put("type", "handler");
                            result.put("handler", "onPositionChange");
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
                }
            }
        });
        webView.getContext().bindService(intent, playerConnection, Context.BIND_AUTO_CREATE);
    }

    protected void load()
    {
        cordova.getThreadPool().submit(new Runnable()
        {
            @Override
            public void run()
            {
                try {
                    musicManager.load();
                } catch (Exception e) {
                    eventManager.error("Error while reading songs : " + e.getMessage());

                    initCallback.error(e.getMessage());
                }

                initCallback.success();
            }
        });
    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException
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
                        super.accept(event);
                        callbackContext.success(event);
                    }
                });
                return true;
            case "download":
                try {
                    downloadManager.download(SongToDownload.fromJSON(args.getJSONObject(0)));
                } catch (JSONException e) {
                    eventManager.error("Error while parsing song infos : " + e.getMessage());
                }
                break;
            case "update":
                try {
                    musicManager.update(Song.fromJSON(args.getJSONObject(0)));
                } catch (Exception e) {
                    eventManager.error("Error while parsing song infos : " + e.getMessage());
                }

                break;
            case "remove":
                try {
                    musicManager.remove(Song.fromJSON(args.getJSONObject(0)));
                } catch (Exception e) {
                    eventManager.error("Error while parsing song infos : " + e.getMessage());
                }
                break;
            case "play":
                bundle.putString("song", args.getJSONObject(0).toString());
                send(ThundermusicService.MSG_PLAY, bundle);
                break;
            case "unpause":
                send(ThundermusicService.MSG_UNPAUSE, null);
                break;
            case "pause":
                send(ThundermusicService.MSG_PAUSE, null);
                break;
            case "seek":
                bundle.putInt("position", args.getInt(0));
                send(ThundermusicService.MSG_SEEK, bundle);
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

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults)
    {
        load();
    }

    protected void send(int message, Bundle data)
    {
        Message msg = Message.obtain(null, message, 0, 0);
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

    public void updateService()
    {
        List<Song> songs = new ArrayList<>(musicManager.getSongs()); // Copy
        String[] jsons = new String[songs.size()];

        for (int i = 0; i < songs.size(); i++)
        {
            try {
                jsons[i] = songs.get(i).toJSON().toString();
            } catch (JSONException e) {
                Log.e("Thundermusic", "Error while sending songs", e);
                eventManager.error("Error while sending songs : " + e.getMessage());

                return;
            }
        }

        Bundle bundle = new Bundle();
        bundle.putStringArray("songs", jsons);

        send(ThundermusicService.MSG_UPDATE, bundle);
    }
}
