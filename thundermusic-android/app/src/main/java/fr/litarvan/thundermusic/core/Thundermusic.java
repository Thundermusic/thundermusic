package fr.litarvan.thundermusic.core;

import java.io.IOException;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
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
    private MusicManager musicManager;
    private MusicPlayer player;
    private DownloadManager downloadManager;

    private ServiceConnection playerConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            playerMessenger = new Messenger(iBinder);
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
        musicManager = new MusicManager(webView.getContext(), eventManager);
        player = new MusicPlayer(this, webView.getContext(), eventManager, musicManager);
        downloadManager = new DownloadManager(webView.getContext(), musicManager, eventManager);

        musicManager.setPlayer(player);

        try {
            musicManager.load();
        } catch (Exception e) {
            Log.e("Thundermusic", "Error while reading songs", e);
            eventManager.error("Error while reading songs : " + e.getMessage());
        }

        Intent intent = new Intent(webView.getContext(), MediaPlayerService.class);
        intent.putExtra("receiver", new ResultReceiver(null)
        {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData)
            {
                System.out.println("IL A DIT : " + resultCode);
            }
        });
        webView.getContext().bindService(intent, playerConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException
    {
        switch (action) {
            case "init":
                cordova.getThreadPool().execute(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        init();
                        callbackContext.success("OK");
                    }
                });
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
                try {
                    downloadManager.download(SongToDownload.fromJSON(args.getJSONObject(0)));
                } catch (JSONException e) {
                    Log.e("Thundermusic", "Error while parsing song infos", e);
                    eventManager.error("Error while parsing song infos : " + e.getMessage());
                }
                break;
            case "play":
                cordova.getThreadPool().execute(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try {
                            player.play(Song.fromJSON(args.getJSONObject(0)));
                        } catch (JSONException | IOException e) {
                            Log.e("Thundermusic", "Error while parsing song infos", e);
                            eventManager.error("Error while parsing song infos : " + e.getMessage());
                        }
                    }
                });
                break;
            case "pause":
                player.pause();
                break;
            case "next":
                try {
                    player.next();
                } catch (IOException e) {
                    Log.e("Thundermusic", "Error while reading next song", e);
                    eventManager.error("Error while reading next song : " + e.getMessage());
                }
                break;
            case "previous":
                try {
                    player.previous();
                } catch (IOException e) {
                    Log.e("Thundermusic", "Error while reading previous song", e);
                    eventManager.error("Error while reading previous song : " + e.getMessage());
                }
                break;
            case "seek":
                player.seek(args.getInt(0));
                break;
            case "update":
                try {
                    musicManager.update(Song.fromJSON(args.getJSONObject(0)));
                } catch (Exception e) {
                    Log.e("Thundermusic", "Error while parsing song infos", e);
                    eventManager.error("Error while parsing song infos : " + e.getMessage());
                }
                break;
            case "remove":
                try {
                    musicManager.remove(Song.fromJSON(args.getJSONObject(0)));
                } catch (Exception e) {
                    Log.e("Thundermusic", "Error while parsing song infos", e);
                    eventManager.error("Error while parsing song infos : " + e.getMessage());
                }
                break;
            case "position":
                JSONObject object = new JSONObject();
                object.put("position", player.getPosition());
                object.put("duration", player.getDuration());

                callbackContext.success(object);
                return true;
            default:
                return false;
        }

        callbackContext.success();
        return true;
    }

    public Messenger getPlayerMessenger()
    {
        return playerMessenger;
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
