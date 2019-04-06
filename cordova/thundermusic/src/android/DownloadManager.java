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
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import android.util.Log;
import fr.litarvan.thundermusic.BuildConfig;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DownloadManager
{
    private MusicManager musicManager;
    private EventManager eventManager;
    private BlockingQueue<SongToDownload> queue;
    private List<SongToDownload> downloads;

    public DownloadManager(MusicManager musicManager, EventManager eventManager)
    {
        this.musicManager = musicManager;
        this.eventManager = eventManager;
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
                    String url = song.getUrl();

                    song.setProgress(url == null ? -1 : 0);
                    update();

                    try {
                        if (url == null) {
                            download(BuildConfig.API_URL + "convert?query=" + URLEncoder.encode("https://www.youtube.com/watch?v=" + song.getId(), "UTF-8"), null, null, true);

                            song.setProgress(0);
                            update();

                            download(BuildConfig.API_URL + "download?id=" + song.getId(), new FileOutputStream(output), song, false);
                        } else {
                            download(song.getUrl(), new FileOutputStream(output), song, false);
                        }
                    } catch (IOException e) {
                        error("Error while downloading song", e);
                        output.delete();
                        song.setProgress(-3);

                        continue;
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

        byte[] data = new byte[1024];
        long total = 0;
        int count;

        while ((count = input.read(data)) != -1) {
            if (song != null) {
                total += count;

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
        Log.e("TM-DownloadManager", "Error while downloading", error);
        eventManager.error("Error while downloading : " +  message);
    }

    protected void update()
    {
        JSONObject event = new JSONObject();
        JSONArray array = new JSONArray();
        SongToDownload[] songs = downloads.toArray(new SongToDownload[0]);

        try {
            for (int i = 0; i < songs.length; i++) {
                array.put(i, songs[i].toJSON());
            }
        } catch (JSONException e) {
            error("Error while writing JSON", e);
        }

        try {
            event.put("type", "downloads");
            event.put("downloads", array);
        } catch (JSONException e) {
            error("Error while sending downloads event", e);
        }

        eventManager.emit(event);
    }
}
