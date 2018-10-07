package fr.litarvan.thundermusic.core;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;

public class MediaPlayerService extends Service
{
    public static final int MSG_PLAY = 1;
    public static final int MSG_PREVIOUS = 2;
    public static final int MSG_PAUSE = 3;
    public static final int MSG_NEXT = 4;
    public static final int MSG_SEEK = 5;
    public static final int MSG_DURATION = 6;

    private Messenger messenger;
    private ResultReceiver receiver;

    public class IncomingHandler extends Handler
    {
        private Context appContext;

        public IncomingHandler(Context appContext)
        {
            this.appContext = appContext;
        }

        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_PLAY:

                    break;
                case MSG_PREVIOUS:

                    break;
                case MSG_PAUSE:

                    break;
                case MSG_NEXT:

                    break;
                case MSG_SEEK:

                    break;
                case MSG_DURATION:

                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        this.messenger = new Messenger(new IncomingHandler(this));
        this.receiver = intent.getParcelableExtra("receiver");

        return messenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        // TODO: Remove notification ? Stop self ?
        return super.onUnbind(intent);
    }
}
