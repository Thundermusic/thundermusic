package fr.litarvan.thundermusic.core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import fr.litarvan.thundermusic.MainActivity;
import fr.litarvan.thundermusic.R;

public class MusicControlNotification
{
    public static final int ID = 6727;
    public static final String CHANNEL_ID = "thundermusic-control";

    private Context context;
    private NotificationManager notificationManager;
    private Notification.Builder notificationBuilder;
    private int notificationID;

    private File lastThumb;
    private Bitmap lastBitmap;

    public MusicControlNotification(Context context)
    {
        this.context = context;
        this.notificationID = ID;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= 26) {
            CharSequence name = CHANNEL_ID;
            String description = "Thundermusic control notification";

            int importance = NotificationManager.IMPORTANCE_LOW;

            NotificationChannel mChannel = new NotificationChannel(this.CHANNEL_ID, name, importance);
            mChannel.setDescription(description);

            this.notificationManager.createNotificationChannel(mChannel);
        }
    }

    public void update(String title, String artist, File thumb, boolean paused)
    {
        Bitmap bitmap = lastBitmap;

        if (lastThumb == null || !thumb.getAbsolutePath().equals(lastThumb.getAbsolutePath())) {
            try (BufferedInputStream buf = new BufferedInputStream(new FileInputStream(thumb))) {
                bitmap = BitmapFactory.decodeStream(buf);
            } catch (IOException e) {
                Log.e("TM-Notification", "Can't read bitmap '" + thumb.getAbsolutePath() + "'", e);
            }

            // Make it squared

            int x, y, width, height;
            if (bitmap.getWidth() > bitmap.getHeight()) {
                x = (bitmap.getWidth() - bitmap.getHeight()) / 2;
                y = 0;
                width = height = bitmap.getHeight();
            } else {
                x = 0;
                y = (bitmap.getHeight() - bitmap.getWidth()) / 2;
                width = height = bitmap.getWidth();
            }

            bitmap = Bitmap.createBitmap(bitmap, x, y, width - 90 /* ? */, height);

            lastThumb = thumb;
            lastBitmap = bitmap;
        }

        Notification.Builder builder = new Notification.Builder(context);
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            builder.setChannelId(this.CHANNEL_ID);
        }

        builder.setContentTitle(title);
        builder.setContentText(artist);
        builder.setWhen(0);

        builder.setOngoing(false);

        Intent dismissIntent = new Intent("music-controls-destroy");
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(context, 1, dismissIntent, 0);
        builder.setDeleteIntent(dismissPendingIntent);
        builder.setPriority(Notification.PRIORITY_MAX);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            builder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        builder.setSmallIcon(R.drawable.icon_notify);
        builder.setLargeIcon(bitmap);

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            builder.setColor(0x4b4b4b);
        }

        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.setAction(Intent.ACTION_MAIN);
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, 0);
        builder.setContentIntent(resultPendingIntent);

        Intent previousIntent = new Intent("music-controls-previous");
        PendingIntent previousPendingIntent = PendingIntent.getBroadcast(context, 1, previousIntent, 0);
        builder.addAction(R.drawable.ic_previous, "", previousPendingIntent);

        Intent pauseIntent = new Intent( "music-controls-pause");
        PendingIntent pausePendingIntent = PendingIntent.getBroadcast(context, 1, pauseIntent, 0);
        builder.addAction(paused ? R.drawable.ic_play : R.drawable.ic_pause, "", pausePendingIntent); // ou play

        Intent nextIntent = new Intent("music-controls-next");
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(context, 1, nextIntent, 0);
        builder.addAction(R.drawable.ic_next, "", nextPendingIntent);

        Intent destroyIntent = new Intent("music-controls-destroy");
        PendingIntent destroyPendingIntent = PendingIntent.getBroadcast(context, 1, destroyIntent, 0);
        builder.addAction(R.drawable.ic_close, "", destroyPendingIntent);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP){
            builder.setStyle(new Notification.MediaStyle().setShowActionsInCompactView(0, 1, 2, 4));
        }

        this.notificationBuilder = builder;

        Notification notif = this.notificationBuilder.build();
        this.notificationManager.notify(this.notificationID, notif);
    }

    public void destroy()
    {
        this.notificationManager.cancel(this.notificationID);
    }
}
