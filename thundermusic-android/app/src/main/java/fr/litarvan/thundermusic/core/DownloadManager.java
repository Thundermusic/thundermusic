package fr.litarvan.thundermusic.core;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

public class DownloadManager
{
    private Context context;
    private MusicManager musicManager;
    private EventManager eventManager;
    private BlockingQueue<SongToDownload> queue;

    public DownloadManager(Context context, MusicManager musicManager, EventManager eventManager)
    {
        this.context = context;
        this.musicManager = musicManager;
        this.eventManager = eventManager;
        this.queue = new LinkedBlockingQueue<>();
    }

    public void start()
    {
        while (true) {
            SongToDownload song = this.queue.poll();
            download(this.context, song);
        }
    }

    public void download(SongToDownload song)
    {
        this.queue.offer(song);
    }

    protected void download(Context context, SongToDownload song)
    {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra("id", song.getId());
        intent.putExtra("output", musicManager.getFile(song));
        intent.putExtra("receiver", new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData)
            {
                super.onReceiveResult(resultCode, resultData);

                switch (resultCode)
                {
                    case DownloadService.CONVERTED:
                        sendEvent(song.getId(), "converted", null);
                        break;
                    case DownloadService.DOWNLOAD_PROGRESS:
                        sendEvent(song.getId(), "progress", String.valueOf(resultData.getInt("progress")));
                        break;
                    case DownloadService.FINISHED:
                        boolean ok = false;

                        try {
                            musicManager.create(song, new File(resultData.getString("output")), resultData.getByteArray("thumbnail"), true);
                            ok = true;
                        } catch (Exception e) {
                            Log.e("Thundermusic", "Error while downloading song", e);
                            eventManager.error("Error while downloading song : " + e.getMessage());
                        }

                        sendEvent(song.getId(), "finished", String.valueOf(ok));
                        break;
                    case DownloadService.ERROR:
                        eventManager.error(resultData.getString("message"));
                        break;
                }
            }
        });
        context.startService(intent);
    }

    protected void sendEvent(String song, String event, String param)
    {
        JSONObject result = new JSONObject();

        try {
            result.put("type", "download");
            result.put("song", song);
            result.put("value", event);

            if (param != null) {
                result.put("param", param);
            }
        } catch (JSONException e) {
            Log.e("TM-DownloadManager", "Error while creating JSON event", e);
        }

        eventManager.emit(result);
    }
}
