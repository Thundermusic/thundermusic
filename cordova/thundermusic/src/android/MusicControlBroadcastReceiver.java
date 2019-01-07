package fr.litarvan.thundermusic.core;

import java.io.IOException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import org.json.JSONException;
import org.json.JSONObject;

public class MusicControlBroadcastReceiver extends BroadcastReceiver
{
    private MusicPlayer player;

    public MusicControlBroadcastReceiver(EventManager player)
    {
        this.player = player;
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent == null) {
            return;
        }

        switch (intent.getAction()) {
            case "music-controls-next":
                send("onNext");
                break;
            case "music-controls-previous":
                send("onPrevious");
                break;
            case "music-controls-pause":
                send("onPause");
                break;
            case "music-controls-media-button":
                KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (event.getAction() != KeyEvent.ACTION_DOWN) {
                    break;
                }
                int keyCode = event.getKeyCode();
                switch (keyCode) {
                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                        send("onNext");
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                        send("onPause");
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                        send("onPrevious");
                        break;
                }
                break;
            /*case "music-controls-destroy":
                send("stop");
                break;*/
            default:
                // TODO: Headphone disconnect event
                System.out.println("Unknown event : " + intent.getAction());
                break;
        }
    }

    protected void send(String handler) {
        JSONObject obj = new JSONObject();

        try {
            obj.put("type", "handler");
            obj.put("handler", handler);

            eventManager.emit(obj);
        } catch (JSONException e) {
            eventManager.error("Error while sending handler JSON : " + e.getMessage());
        }
    }
}
