package fr.litarvan.thundermusic.core;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.util.Log;

public class DownloadService extends IntentService
{
    public static final int CONVERTED = 6726;
    public static final int DOWNLOAD_PROGRESS = 6727;
    public static final int FINISHED = 6728;
    public static final int ERROR = 6777;

    public DownloadService()
    {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent)
    {
        if (intent == null) {
            return;
        }

        ResultReceiver receiver = intent.getParcelableExtra("receiver");

        String id = intent.getStringExtra("id");
        String output = intent.getStringExtra("output");

        try {
            download(Thundermusic.API_URL + "convert?query=" + URLEncoder.encode("https://www.youtube.com/watch?v=" + id), null, null);
        } catch (IOException e) {
            error(receiver, "Error while converting song", e);
            return;
        }

        receiver.send(CONVERTED, new Bundle());

        try {
            download(Thundermusic.API_URL + "download?id=" + id, new FileOutputStream(output), receiver);
        } catch (IOException e) {
            error(receiver, "Error while downloading song", e);
            return;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            download(intent.getStringExtra("thumbnail"), out, receiver);
        } catch (IOException e) {
            error(receiver, "Error while downloading song thumbnail", e);
            return;
        }

        byte[] thumbnail = out.toByteArray();
        try {
            out.close();
        } catch (IOException ignored) { }

        Bundle bundle = new Bundle();
        bundle.putByteArray("thumbnail", thumbnail);
        bundle.putString("output", output);
        receiver.send(FINISHED, new Bundle());
    }

    protected void download(String urlStr, OutputStream output, ResultReceiver receiver) throws IOException
    {
        URL url = new URL(urlStr);
        URLConnection connection = url.openConnection();
        connection.connect();

        int fileLength = connection.getContentLength();

        InputStream input = new BufferedInputStream(connection.getInputStream());

        byte data[] = new byte[1024];
        long total = 0;
        int count;

        while ((count = input.read(data)) != -1) {
            total += count;

            if (receiver != null) {
                Bundle resultData = new Bundle();
                resultData.putInt("progress" ,(int) (total * 100 / fileLength));
                receiver.send(DOWNLOAD_PROGRESS, resultData);
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

    protected void error(ResultReceiver receiver, String msg, Exception error)
    {
        Log.e("TM-DownloadService", msg, error);

        Bundle resultData = new Bundle();
        resultData.putString("type", error.getClass().getSimpleName());
        resultData.putString("message", msg);
        receiver.send(ERROR, resultData);
    }
}
