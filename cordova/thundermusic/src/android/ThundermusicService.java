package fr.litarvan.thundermusic.core;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.support.annotation.Nullable;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ThundermusicService extends Service
{
    public static final int MSG_INIT = 1;
    public static final int MSG_UPDATE = 2;
    public static final int MSG_PLAY = 3;
    public static final int MSG_PAUSE = 4;
    public static final int MSG_UNPAUSE = 5;
    public static final int MSG_SEEK = 6;
    public static final int MSG_POSITION = 7;

    public static final int RSP_ERROR = 8;
    public static final int RSP_EVENT = 9;
    public static final int RSP_POSITION = 10;

    private Messenger messenger;
    private ResultReceiver receiver;

    private MusicPlayer player;

    private Song[] songs = new Song[] {};

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
            final Message cpy = Message.obtain(msg); // Needed

            switch (msg.what)
            {
                case MSG_INIT:
                    init(appContext);
                    break;
                case MSG_UPDATE:
                    String[] jsons = cpy.getData().getStringArray("songs");
                    Song[] array = new Song[jsons.length];

                    for (int i = 0; i < jsons.length; i++) {
                        try {
                            array[i] = Song.fromJSON(new JSONObject(jsons[i]));
                        } catch (JSONException e) {
                            Log.e("Thundermusic", "Error while parsing song infos", e);
                            error("Error while parsing song infos : " + e.getMessage());

                            return;
                        }
                    }

                    songs = array;
                    break;
                case MSG_PLAY:
                    Thread t2 = new Thread()
                    {
                        @Override
                        public void run()
                        {
                            try {
                                player.play(Song.fromJSON(new JSONObject(cpy.getData().getString("song"))));
                            } catch (JSONException | IOException e) {
                                Log.e("Thundermusic", "Error while parsing song infos", e);
                                error("Error while parsing song infos : " + e.getMessage());
                            }
                        }
                    };
                    t2.start();
                    break;
                case MSG_PAUSE:
                    if (!player.isPaused()) {
                        player.pause();
                    }
                    break;
                case MSG_UNPAUSE:
                    if (player.isPaused()) {
                        player.pause();
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
                default:
                    super.handleMessage(msg);
            }
        }
    }

    public void init(Context context)
    {
        player = new MusicPlayer(context, receiver);
        player.init();
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

    public Song[] getSongs()
    {
        return songs;
    }
}
