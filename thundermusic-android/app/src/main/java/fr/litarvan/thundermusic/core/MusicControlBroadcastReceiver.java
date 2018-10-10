package fr.litarvan.thundermusic.core;

import java.io.IOException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

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
                try {
                    this.player.next();
                } catch (IOException ignored) {
                }
                break;
            case "music-controls-previous":
                try {
                    this.player.previous();
                } catch (IOException ignored) {
                }
                break;
            case "music-controls-pause":
                this.player.pause();
                break;
            case "music-controls-media-button":
                KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (event.getAction() != KeyEvent.ACTION_DOWN) {
                    break;
                }
                int keyCode = event.getKeyCode();
                switch (keyCode) {
                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                        try {
                            this.player.next();
                        } catch (IOException ignored) {
                        }
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                        this.player.pause();
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                        try {
                            this.player.previous();
                        } catch (IOException ignored) {
                        }
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
}
