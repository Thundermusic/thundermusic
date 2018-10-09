package fr.litarvan.thundermusic.core;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;

public class DownloadManager
{
    private MusicManager musicManager;
    private ResultReceiver receiver;
    private BlockingQueue<SongToDownload> queue;
    private List<SongToDownload> downloads;

    public DownloadManager(MusicManager musicManager, ResultReceiver receiver)
    {
        this.musicManager = musicManager;
        this.receiver = receiver;
        this.queue = new LinkedBlockingQueue<>();
        this.downloads = new ArrayList<>();
    }

    public void download(SongToDownload song)
    {
        this.queue.offer(song);
        this.downloads.add(song);
    }

    public void start()
    {
        Thread t = new Thread() {
            @Override
            public void run()
            {
                while (true) {
                    SongToDownload song;
                    try {
                        song = queue.poll(Integer.MAX_VALUE, TimeUnit.DAYS);
                    } catch (InterruptedException e) {
                        break;
                    }

                    File output = musicManager.getFile(song);

                    song.setProgress(-1);
                    update();

                    try {
                        download(Thundermusic.API_URL + "convert?query=" + URLEncoder.encode("https://www.youtube.com/watch?v=" + song.getId()), null, null, true);
                    } catch (IOException e) {
                        error("Error while converting song", e);
                        return;
                    }

                    song.setProgress(0);
                    update();

                    try {
                        download(Thundermusic.API_URL + "download?id=" + song.getId(), new FileOutputStream(output), song, false);
                    } catch (IOException e) {
                        error("Error while downloading song", e);
                        return;
                    }

                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    try {
                        download(song.getThumbnail(), out, null, false);
                    } catch (IOException e) {
                        error("Error while downloading song thumbnail", e);
                        return;
                    }

                    byte[] thumbnail = out.toByteArray();
                    try {
                        out.close();
                    } catch (IOException ignored) { }

                    try {
                        musicManager.create(song, output, thumbnail, true);
                    } catch (Exception e) {
                        error("Error while adding song", e);
                    }

                    SongToDownload[] songs = downloads.toArray(new SongToDownload[0]);
                    for (int i = 0; i < songs.length; i++)
                    {
                        if (songs[i] == song)
                        {
                            downloads.remove(i);
                            update();
                            break;
                        }
                    }
                }
            }
        };
        t.start();
    }

    protected void download(String urlStr, OutputStream output, SongToDownload song, boolean post) throws IOException
    {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        if (post) {
            connection.setRequestMethod("POST");
        }

        connection.connect();

        int fileLength = connection.getContentLength();

        InputStream input = new BufferedInputStream(connection.getInputStream());

        int lastProgress = 0;

        byte data[] = new byte[1024];
        long total = 0;
        int count;

        while ((count = input.read(data)) != -1) {
            total += count;

            if (song != null) {
                int progress = (int) (total * 100 / fileLength);
                if (progress - lastProgress >= 10) {
                    song.setProgress(progress);
                    update();

                    lastProgress = progress;
                }
            }

            if (output != null) {
                output.write(data, 0, count);
            }
        }

        if (output != null) {
            output.flush();
            output.close();
        }

        input.close();
    }

    protected void error(String message, Exception error)
    {
        Bundle bundle = new Bundle();
        bundle.putString("error", message + " : " + error.getMessage());

        receiver.send(ThundermusicService.RSP_ERROR, bundle);
        Log.e("TM-DownloadManager", "Error while downloading", error);
    }

    protected void update()
    {
        Bundle bundle = new Bundle();
        JSONArray array = new JSONArray();
        SongToDownload[] songs = downloads.toArray(new SongToDownload[0]);

        try {
            for (int i = 0; i < songs.length; i++) {
                array.put(i, songs[i].toJSON());
            }
        } catch (JSONException e) {
            error("Error while writing JSON", e);
        }

        bundle.putString("downloads", array.toString());
        receiver.send(ThundermusicService.RSP_DOWNLOADS, bundle);
    }
}
