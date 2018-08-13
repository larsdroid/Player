package org.willemsens.player.playback.eventbus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import com.squareup.otto.Bus;
import org.willemsens.player.playback.PlayStatus;

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

    public static void postAcrossProcess(PlayStatus playStatus, Context context) {
        Intent intent = new Intent(context, PlayBackEventBus.class);
        intent.putExtra(PAYLOAD, (Parcelable) playStatus);
        context.sendBroadcast(intent);
    }

    public static void postWithinSameProcess(PlayStatus playStatus) {
        getInstance().post(playStatus);
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
        PlayStatus playStatus = intent.getParcelableExtra(PAYLOAD);
        PlayBackEventBus.postWithinSameProcess(playStatus);
    }
}
