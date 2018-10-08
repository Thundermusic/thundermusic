package fr.litarvan.thundermusic.core;

import java.io.IOException;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

public class ThundermusicService extends Service
{
    public static final int MSG_INIT = 1;
    public static final int MSG_DOWNLOAD = 2;
    public static final int MSG_PLAY = 3;
    public static final int MSG_PREVIOUS = 4;
    public static final int MSG_PAUSE = 5;
    public static final int MSG_NEXT = 6;
    public static final int MSG_SEEK = 7;
    public static final int MSG_POSITION = 8;
    public static final int MSG_UPDATE = 9;
    public static final int MSG_REMOVE = 10;

    public static final int RSP_INITIALIZED = 12;
    public static final int RSP_ERROR = 13;
    public static final int RSP_EVENT = 14;
    public static final int RSP_POSITION = 15;

    private Messenger messenger;
    private ResultReceiver receiver;

    private MusicManager musicManager;
    private MusicPlayer player;
    private DownloadManager downloadManager;

    public class IncomingHandler extends Handler
    {
        private Context appContext;

        public IncomingHandler(Context appContext)
        {
            this.appContext = appContext;
        }

        @Override
        public void handleMessage(Message msg)
        {
            Message cpy = Message.obtain(msg); // Needed

            switch (msg.what)
            {
                case MSG_INIT:
                    Thread t = new Thread()
                    {
                        @Override
                        public void run()
                        {
                            init(appContext);
                            receiver.send(RSP_INITIALIZED, new Bundle());
                        }
                    };
                    t.start();

                    break;
                case MSG_DOWNLOAD:
                    try {
                        downloadManager.download(SongToDownload.fromJSON(new JSONObject(cpy.getData().getString("song"))));
                    } catch (JSONException e) {
                        Log.e("Thundermusic", "Error while parsing song infos", e);
                        error("Error while parsing song infos : " + e.getMessage());
                    }

                    break;
                case MSG_PLAY:
                    Thread t2 = new Thread()
                    {
                        @Override
                        public void run()
                        {
                            try {
                                player.play(Song.fromJSON(new JSONObject(cpy.getData().getString("song"))));
                                System.out.println("Obj : " + cpy.getData().getString("song"));
                                //player.play(Song.fromJSON(new JSONObject((String) cpy.obj)));
                            } catch (JSONException | IOException e) {
                                Log.e("Thundermusic", "Error while parsing song infos", e);
                                error("Error while parsing song infos : " + e.getMessage());
                            }
                        }
                    };
                    t2.start();
                    break;
                case MSG_PREVIOUS:
                    try {
                        player.previous();
                    } catch (IOException e) {
                        Log.e("Thundermusic", "Error while reading previous song", e);
                        error("Error while reading previous song : " + e.getMessage());
                    }
                    break;
                case MSG_PAUSE:
                    player.pause();
                    break;
                case MSG_NEXT:
                    try {
                        player.next();
                    } catch (IOException e) {
                        Log.e("Thundermusic", "Error while reading next song", e);
                        error("Error while reading next song : " + e.getMessage());
                    }
                    break;
                case MSG_SEEK:
                    player.seek(msg.getData().getInt("position"));
                    break;
                case MSG_POSITION:
                    Bundle bundle = new Bundle();
                    bundle.putInt("position", player.getPosition());
                    bundle.putInt("duration", player.getDuration());
                    receiver.send(RSP_POSITION, bundle);
                    break;
                case MSG_UPDATE:
                    try {
                        musicManager.update(Song.fromJSON(new JSONObject(msg.getData().getString("song"))));
                    } catch (Exception e) {
                        Log.e("Thundermusic", "Error while parsing song infos", e);
                        error("Error while parsing song infos : " + e.getMessage());
                    }
                    break;
                case MSG_REMOVE:
                    try {
                        musicManager.remove(msg.getData().getParcelable("song"));
                    } catch (Exception e) {
                        Log.e("Thundermusic", "Error while parsing song infos", e);
                        error("Error while parsing song infos : " + e.getMessage());
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    public void init(Context context)
    {
        musicManager = new MusicManager(context, receiver);
        player = new MusicPlayer(context, receiver, musicManager);
        //downloadManager = new DownloadManager(context, musicManager, receiver); TODO: Download manager

        musicManager.setPlayer(player);

        try {
            musicManager.load();
        } catch (Exception e) {
            Log.e("Thundermusic", "Error while reading songs", e);
            error("Error while reading songs : " + e.getMessage());
        }
    }

    protected void error(String message)
    {
        Bundle data = new Bundle();
        data.putString("error", message);

        receiver.send(RSP_ERROR, data);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        this.messenger = new Messenger(new IncomingHandler(this));
        this.receiver = intent.getParcelableExtra("receiver");

        return messenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        // TODO: Remove notification ? Stop self ?
        return super.onUnbind(intent);
    }
}
