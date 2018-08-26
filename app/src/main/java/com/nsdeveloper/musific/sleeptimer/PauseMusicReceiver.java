/*
Copyright (c) 2013 Joel Andrews
Distributed under the MIT License: http://opensource.org/licenses/MIT
 */

package com.nsdeveloper.musific.sleeptimer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Handles broadcast events intended to pause music playback indefinitely.
 *
 * @author Joel Andrews
 */
public class PauseMusicReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        pauseMusic(context, TimerManager.get(context), CountdownNotifier.get(context));
    }

    /**
     * Pauses all music playback on the device.
     *
     * @param context The context in which the receiver is running
     *
     * @param timerManager The pause music timer manager
     * @param countdownNotifier The countdown notifier
     */
    void pauseMusic(Context context, TimerManager timerManager, CountdownNotifier countdownNotifier) {
        timerManager.cancelTimer();
        countdownNotifier.cancelNotification();

        // The service will be responsible for actually pausing playback and ensuring it remains paused until explicitly
        // restarted
        Intent serviceIntent = new Intent(context, PauseMusicService.class);
        context.stopService(serviceIntent);
        context.startService(new Intent(context, PauseMusicService.class));
    }
}
