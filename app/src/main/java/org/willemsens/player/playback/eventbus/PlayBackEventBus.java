package org.willemsens.player.playback.eventbus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import com.squareup.otto.Bus;

public class PlayBackEventBus extends BroadcastReceiver {
    private static volatile Bus bus;

    private static final String PAYLOAD = "PAYLOAD";

    public PlayBackEventBus() {
    }

    public static void register(Object o) {
        getInstance().register(o);
    }

    public static void unregister(Object o) {
        getInstance().unregister(o);
    }

    public static void postAcrossProcess(Parcelable parcelable, Context context) {
        Intent intent = new Intent(context, PlayBackEventBus.class);
        intent.putExtra(PAYLOAD, parcelable);
        context.sendBroadcast(intent);
    }

    private static void postWithinSameProcess(Parcelable parcelable) {
        getInstance().post(parcelable);
    }

    private static Bus getInstance() {
        if (bus == null) {
            synchronized (PlayBackEventBus.class) {
                if (bus == null) {
                    bus = new Bus();
                }
            }
        }
        return bus;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Parcelable parcelable = intent.getParcelableExtra(PAYLOAD);
        PlayBackEventBus.postWithinSameProcess(parcelable);
    }
}
