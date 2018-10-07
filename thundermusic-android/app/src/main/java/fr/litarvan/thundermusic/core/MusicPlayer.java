package fr.litarvan.thundermusic.core;

import java.io.IOException;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.PowerManager;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

public class MusicPlayer implements OnPreparedListener, OnCompletionListener
{
    private Thundermusic thundermusic;
    private Context context;
    private EventManager eventManager;
    private MediaPlayer mediaPlayer;
    private MusicManager musicManager;

    private Song current;
    private boolean paused;
    private boolean askedPlay = false;

    public MusicPlayer(Thundermusic thundermusic, Context context, EventManager eventManager, MusicManager musicManager)
    {
        this.thundermusic = thundermusic;
        this.context = context;
        this.eventManager = eventManager;
        this.mediaPlayer = new MediaPlayer();
        this.musicManager = musicManager;
        this.mediaPlayer.setOnPreparedListener(this);
        this.mediaPlayer.setOnCompletionListener(this);
    }

    public void play(Song song) throws IOException
    {
        if (current != null) {
            askedPlay = true;
        }

        mediaPlayer.reset();
        mediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setDataSource(song.getFile().getPath());
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.prepareAsync();

        current = song;
    }

    public void pause()
    {
        if (paused) {
            mediaPlayer.start();
        } else {
            mediaPlayer.pause();
        }

        paused = !paused;

        JSONObject object = new JSONObject();

        try {
            object.put("type", "pause");
            object.put("paused", paused);

            eventManager.emit(object);
        } catch (JSONException e) {
            Log.e("TM-MusicPlayer", "Error while sending pause event", e);
            eventManager.error("Error while sending pause event : " + e.getMessage());
        }
    }

    public void next() throws IOException
    {
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
        if (current != null && mediaPlayer.getCurrentPosition() > 3000) {
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

    @Override
    public void onPrepared(MediaPlayer mediaPlayer)
    {
        mediaPlayer.start();
        paused = false;

        JSONObject object = new JSONObject();

        try {
            object.put("type", "play");
            object.put("song", current.toJSON());

            eventManager.emit(object);
        } catch (JSONException e) {
            Log.e("TM-MusicPlayer", "Error while sending play event", e);
            eventManager.error("Error while sending player event : " + e.getMessage());
        }
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
