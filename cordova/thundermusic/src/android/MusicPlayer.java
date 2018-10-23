package fr.litarvan.thundermusic.core;

import java.io.IOException;

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
import android.view.KeyEvent;
import org.json.JSONException;
import org.json.JSONObject;

public class MusicPlayer implements OnPreparedListener, OnCompletionListener
{
    private Context context;
    private ResultReceiver resultReceiver;
    private MediaPlayer mediaPlayer;
    private MusicManager musicManager;
    private MusicControlNotification notification;

    private Song current;
    private boolean paused;
    private boolean askedPlay = false;

    public MusicPlayer(Context context, ResultReceiver resultReceiver, MusicManager musicManager)
    {
        this.context = context;
        this.resultReceiver = resultReceiver;
        this.musicManager = musicManager;
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
            AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            Intent headsetIntent = new Intent("music-controls-media-button");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, headsetIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            audioManager.registerMediaButtonEventReceiver(pendingIntent);
        } catch (Exception e) {
            Log.e("TM-MusicPlayer", "Couldn't register music buttons events", e);
        }
    }

    public void destroy() {
        if (current != null) {
            current = null;
            updateCurrentEvent();

            this.mediaPlayer.stop();
            this.mediaPlayer.release();
            this.mediaPlayer = null;
        }

        this.notification.destroy();
    }

    public void play(Song song) throws IOException
    {
        if (current != null) {
            askedPlay = true;
        }

        if (mediaPlayer == null) {
            this.mediaPlayer = new MediaPlayer();

            this.mediaPlayer.setOnPreparedListener(this);
            this.mediaPlayer.setOnCompletionListener(this);
        }

        mediaPlayer.reset();
        mediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setDataSource(song.getFile().getPath());
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.prepareAsync();

        current = song;
        paused = false;

        notification.update(song.getTitle(), song.getArtist(), song.getImage(), this.paused);
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
        notification.update(current.getTitle(), current.getArtist(), current.getImage(), this.paused);

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

    public void next() throws IOException
    {
        if (current == null) {
            return;
        }

        int index = getIndex();

        if (musicManager.getSongs().size() - 1 <= index) {
            index = 0;
        } else {
            index++;
        }

        play(musicManager.getSongs().get(index));
    }

    public void previous() throws IOException
    {
        if (current == null) {
            return;
        }

        if (mediaPlayer.getCurrentPosition() > 3000) {
            seek(0);
            return;
        }

        int index = getIndex();

        if (index == 0) {
            index = musicManager.getSongs().size();
        } else {
            index--;
        }

        play(musicManager.getSongs().get(index));
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

    protected int getIndex()
    {
        Song[] songs = musicManager.getSongs().toArray(new Song[0]);
        for (int i = 0; i < songs.length; i++)
        {
            if (songs[i].getId().equals(this.current.getId()))
            {
                return i;
            }
        }

        return 0;
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
        mediaPlayer.start();
        paused = false;

        updateCurrentEvent();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer)
    {
        if (!askedPlay) {
            try {
                next();
            } catch (IOException e) {
                Log.w("TM-MusicPlayer", "Couldn't set next music", e);
            }
        }

        askedPlay = false;
    }

    public Song getCurrent()
    {
        return current;
    }
}
