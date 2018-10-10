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

        /*
        case 'music-controls-next':
			// Do something
			break;
		case 'music-controls-previous':
			// Do something
			break;
		case 'music-controls-pause':
			// Do something
			break;
		case 'music-controls-play':
			// Do something
			break;
		case 'music-controls-destroy':
			// Do something
			break;

		// External controls (iOS only)
    	case 'music-controls-toggle-play-pause' :
			// Do something
			break;
    	case 'music-controls-seek-to':
			const seekToInSeconds = JSON.parse(action).position;
			MusicControls.updateElapsed({
				elapsed: seekToInSeconds,
				isPlaying: true
			});
			// Do something
			break;

		// Headset events (Android only)
		// All media button events are listed below
		case 'music-controls-media-button' :
			// Do something
			break;
		case 'music-controls-headset-unplugged':
			// Do something
			break;
		case 'music-controls-headset-plugged':
			// Do something
			break;
		default:
			break;
         */

        switch (intent.getAction()) {
            case "music-controls-next":

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
        }
    }
}
