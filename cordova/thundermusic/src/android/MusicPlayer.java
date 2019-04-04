package fr.litarvan.thundermusic.core;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MusicPlayer implements OnPreparedListener, OnCompletionListener
{
    private Context context;
    private ResultReceiver resultReceiver;
    private MediaPlayer mediaPlayer;
    private MusicControlNotification notification;

    private Song current;
    private boolean paused;

    public MusicPlayer(Context context, ResultReceiver resultReceiver)
    {
        this.context = context;
        this.resultReceiver = resultReceiver;
    }

    public void init()
    {
        this.notification = new MusicControlNotification(context);
        BroadcastReceiver receiver = new MusicControlBroadcastReceiver(this);

        context.registerReceiver(receiver, new IntentFilter("music-controls-previous"));
        context.registerReceiver(receiver, new IntentFilter("music-controls-pause"));
        context.registerReceiver(receiver, new IntentFilter("music-controls-play"));
        context.registerReceiver(receiver, new IntentFilter("music-controls-next"));
        context.registerReceiver(receiver, new IntentFilter("music-controls-media-button"));
        context.registerReceiver(receiver, new IntentFilter("music-controls-destroy"));

        context.registerReceiver(receiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));

        try {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            Intent headsetIntent = new Intent("music-controls-media-button");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, headsetIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            audioManager.registerMediaButtonEventReceiver(pendingIntent);
        } catch (Exception e) {
            Log.e("TM-MusicPlayer", "Couldn't register music buttons events", e);
        }
    }

    public void destroy() {
        if (mediaPlayer != null) {
            this.mediaPlayer.stop();
            this.mediaPlayer.release();
            this.mediaPlayer = null;
        }

        if (current != null) {
            current = null;
            updateCurrentEvent();
        }

        this.notification.destroy();
    }

    public void play(Song song) throws IOException
    {
        /*System.out.println("Asked to play : " + song.getTitle());

        if (current != null) {
            System.out.println("Askedplay=true");
            askedPlay = true;
        }*/

        if (mediaPlayer == null) {
            this.mediaPlayer = new MediaPlayer();
            this.mediaPlayer.setOnPreparedListener(this);
        }

        this.mediaPlayer.setOnCompletionListener(null);

        mediaPlayer.reset();
        mediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setDataSource(song.getFile().getPath());
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.prepareAsync();

        current = song;
        paused = false;

        notification.update(song.getTitle(), song.getArtist(), song.getThumbnail(), this.paused);
    }

    public void pause()
    {
        if (current == null) {
            return;
        }

        if (paused) {
            mediaPlayer.start();
        } else {
            mediaPlayer.pause();
        }

        paused = !paused;
        notification.update(current.getTitle(), current.getArtist(), current.getThumbnail(), this.paused);

        JSONObject object = new JSONObject();

        try {
            object.put("type", "pause");
            object.put("paused", paused);

            Bundle bundle = new Bundle();
            bundle.putString("event", object.toString());

            resultReceiver.send(ThundermusicService.RSP_EVENT, bundle);
        } catch (JSONException e) {
            Bundle bundle = new Bundle();
            bundle.putString("error", "Error while sending pause event : " + e.getMessage());

            resultReceiver.send(ThundermusicService.RSP_ERROR, bundle);
        }
    }

    public void seek(int ms)
    {
        if (current == null) {
            return;
        }

        mediaPlayer.seekTo(ms);
    }

    public int getPosition()
    {
        if (current == null || mediaPlayer == null) {
            return -1;
        }

        return mediaPlayer.getCurrentPosition();
    }

    public int getDuration()
    {
        if (current == null || mediaPlayer == null) {
            return -1;
        }

        return mediaPlayer.getDuration();
    }

    public void updateCurrentEvent()
    {
        JSONObject object = new JSONObject();

        try {
            object.put("type", "play");
            object.put("song", current == null ? null : current.toJSON());

            Bundle bundle = new Bundle();
            bundle.putString("event", object.toString());

            resultReceiver.send(ThundermusicService.RSP_EVENT, bundle);
        } catch (JSONException e) {
            Bundle bundle = new Bundle();
            bundle.putString("error", "Error while sending play event : " + e.getMessage());

            resultReceiver.send(ThundermusicService.RSP_ERROR, bundle);
        }
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer)
    {
        this.mediaPlayer.setOnCompletionListener(this);

        mediaPlayer.start();
        paused = false;

        updateCurrentEvent();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer)
    {
       //System.out.println("COMPLETIONNNN");
       //System.out.println("Completion, asked play ? " + askedPlay);

        //if (!askedPlay) {
            //System.out.println("Asked play is false so going next");
            //try {
            //    next();
            //} catch (IOException e) {
            //    Log.w("TM-MusicPlayer", "Couldn't set next music", e);
            //}
        //}

        JSONObject object = new JSONObject();

        try {
            object.put("type", "handler");
            object.put("handler", "onEnd");

            Bundle bundle = new Bundle();
            bundle.putString("event", object.toString());

            resultReceiver.send(ThundermusicService.RSP_EVENT, bundle);
        } catch (JSONException e) {
            Bundle bundle = new Bundle();
            bundle.putString("error", "Error while sending end event : " + e.getMessage());

            resultReceiver.send(ThundermusicService.RSP_ERROR, bundle);
        }

        /*System.out.println("askedplay=false");
        askedPlay = false;*/
    }

    public Song getCurrent()
    {
        return current;
    }

    public ResultReceiver getResultReceiver()
    {
        return resultReceiver;
    }

    public boolean isPaused()
    {
        return paused;
    }
}
