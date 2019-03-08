package fr.litarvan.thundermusic.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MusicControlBroadcastReceiver extends BroadcastReceiver
{
    private MusicPlayer player;

    public MusicControlBroadcastReceiver(MusicPlayer player)
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
                emitHandler("onNext");
                break;
            case "music-controls-previous":
                emitHandler("onPrevious");
                break;
            case "music-controls-pause":
                if (player.isPaused()) {
                    emitHandler("onPlay");
                } else {
                    emitHandler("onPause");
                }
                break;
            case "music-controls-media-button":
                KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (event.getAction() != KeyEvent.ACTION_DOWN) {
                    break;
                }
                int keyCode = event.getKeyCode();
                switch (keyCode) {
                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                        emitHandler("onNext");
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                        if (player.isPaused()) {
                            emitHandler("onPlay");
                        } else {
                            emitHandler("onPause");
                        }
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                        emitHandler("onPrevious");
                        break;
                    default:
                        System.out.println("BOUTON INCONNU : " + keyCode);
                        break;
                }
                break;
            case "music-controls-destroy":
                this.player.destroy();
                break;
            default:
                System.out.println("AHHHHHHHHHHH : " + intent.getAction());
                break;
        }
    }

    protected void emitHandler(String handler) {
        try {
            JSONObject object = new JSONObject();
            object.put("type", "handler");
            object.put("handler", handler);


            Bundle bundle = new Bundle();
            bundle.putString("event", object.toString());

            player.getResultReceiver().send(ThundermusicService.RSP_EVENT, bundle);
        } catch (JSONException e) {
            Log.e("Thundermusic", "Error while sending handler event", e);

            Bundle bundle = new Bundle();
            bundle.putString("error", "Error while sending handler event : " + e.getMessage());

            player.getResultReceiver().send(ThundermusicService.RSP_ERROR, bundle);
        }
    }
}
