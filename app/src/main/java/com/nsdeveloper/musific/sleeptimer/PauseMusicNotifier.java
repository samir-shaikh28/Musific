/*
Copyright (c) 2013 Joel Andrews
Distributed under the MIT License: http://opensource.org/licenses/MIT
 */

package com.nsdeveloper.musific.sleeptimer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.NotificationCompat;

import com.nsdeveloper.musific.R;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <p>
 *     Creates status bar notifications that indicate music playback has been paused.
 * </p>
 * <p>
 *     Do not instantiate directly. Instead, call {@link #get(Context)} to retrieve an instance.
 * </p>
 *
 * @author Joel Andrews
 */
public class PauseMusicNotifier {

    private static ConcurrentMap<String, PauseMusicNotifier> allInstances = new ConcurrentHashMap<String, PauseMusicNotifier>();

    private static final int NOTIFICATION_ID = 1;

    private final Context context;
    private final NotificationManager notificationManager;
    private final Resources resources;

    /**
     * Constructs an instance of {@link PauseMusicNotifier}.
     *
     * @param context The context
     */
    private PauseMusicNotifier(Context context) {
        this(context, (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE), context.getResources());
    }

    /**
     * Constructs an instance of {@link PauseMusicNotifier}. Should not be instantiated directly; call
     * {@link #get(Context)} instead.
     *
     * @param context The context
     * @param notificationManager The system notification manager
     * @param resources The app's resources
     */
    PauseMusicNotifier(Context context, NotificationManager notificationManager, Resources resources) {
        this.context = context.getApplicationContext();
        this.notificationManager = notificationManager;
        this.resources = resources;
    }

    /**
     * Returns an instance of this class for the specified context.
     *
     * @param context The context. Must not be null.
     *
     * @return A {@link PauseMusicNotifier}
     */
    public static PauseMusicNotifier get(Context context) {
        if (context == null) {
            throw new NullPointerException("Argument context cannot be null");
        }

        String instanceKey = context.getPackageName();

        // A thread safe way of retrieving the PauseMusicNotifier for the given context if it already exists, or creating
        // a new instance if not
        PauseMusicNotifier existingInstance = allInstances.putIfAbsent(instanceKey, new PauseMusicNotifier(context));
        if (existingInstance != null) {
            return existingInstance;
        } else {
            // A PauseMusicNotifier didn't yet exist for the given context; return the newly created instance
            return allInstances.get(instanceKey);
        }
    }

    /**
     * Returns a notification for use when music playback is paused.
     *
     * @return A {@link Notification}
     */
    private Notification getNotification() {
        String title = resources.getString(R.string.paused_music_notification_title);
        String text = resources.getString(R.string.paused_music_notification_text);

        PendingIntent tapIntent =
                PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(text)
                .setTicker(title)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(tapIntent)
                .setAutoCancel(true);

        return builder.build();
    }

    /**
     * Posts a music paused notification to the system status bar.
     */
    public void postNotification() {
        Notification notification = getNotification();

        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    /**
     * Cancels and removes the music paused notification from the system status bar. If the notification is not present,
     * this method has no effect.
     */
    public void cancelNotification() {
        notificationManager.cancel(NOTIFICATION_ID);
    }
}
