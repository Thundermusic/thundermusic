package fr.litarvan.thundermusic.core;

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

    private EventManager eventManager;
    private CallbackContext initCallback;

    private MusicManager musicManager;
    private DownloadManager downloadManager;

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
        musicManager = new MusicManager(this.webView.getContext(), eventManager);
        downloadManager = new DownloadManager(musicManager, eventManager);

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

        downloadManager.start();
    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException
    {
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
            default:
                return false;
        }

        callbackContext.success();
        return true;
    }
}
