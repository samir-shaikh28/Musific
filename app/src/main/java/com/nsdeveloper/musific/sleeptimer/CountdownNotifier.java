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

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <p>
 *     Creates status bar notifications that indicate a sleep timer countdown is active.
 * </p>
 * <p>
 *     Do not instantiate directly. Instead, call {@link #get(Context)} to retrieve an instance.
 * </p>
 *
 * @author Joel Andrews
 */
public class CountdownNotifier {

    private static ConcurrentMap<String, CountdownNotifier> allInstances = new ConcurrentHashMap<String, CountdownNotifier>();

    private static final int NOTIFICATION_ID = 2;

    private final Context context;
    private final NotificationManager notificationManager;
    private final Resources resources;
    private final TimeFormatFactory countdownTimeFormatFactory;

    /**
     * Constructs an instance of {@link CountdownNotifier}.
     *
     * @param context The context
     */
    private CountdownNotifier(Context context) {
        this(
                context,
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE),
                context.getResources(),
                new TimeFormatFactory(context)
        );
    }

    /**
     * Constructs an instance of {@link CountdownNotifier}. Should not be instantiated directly; call
     * {@link #get(Context)} instead.
     *
     * @param context The context
     * @param notificationManager The system notification manager
     * @param resources The app's resources
     * @param countdownTimeFormatFactory Produces the time format to use for the countdown
     */
    CountdownNotifier(
            Context context,
            NotificationManager notificationManager,
            Resources resources,
            TimeFormatFactory countdownTimeFormatFactory) {

        this.context = context.getApplicationContext();
        this.notificationManager = notificationManager;
        this.resources = resources;
        this.countdownTimeFormatFactory = countdownTimeFormatFactory;
    }

    /**
     * Returns an instance of this class for the specified context.
     *
     * @param context The context. Must not be null.
     *
     * @return A {@link CountdownNotifier}
     */
    public static CountdownNotifier get(Context context) {
        if (context == null) {
            throw new NullPointerException("Argument context cannot be null");
        }

        String instanceKey = context.getPackageName();

        // A thread safe way of retrieving the CountdownNotifier for the given context if it already exists, or creating
        // a new instance if not
        CountdownNotifier existingInstance = allInstances.putIfAbsent(instanceKey, new CountdownNotifier(context));
        if (existingInstance != null) {
            return existingInstance;
        } else {
            // A CountdownNotifier didn't yet exist for the given context; return the newly created instance
            return allInstances.get(instanceKey);
        }
    }

    /**
     * Returns a notification for use when a countdown is running.
     *
     * @param countdownEnds The date and time the countdown is set to expire
     *
     * @return A {@link Notification}
     */
    private Notification getNotification(Date countdownEnds) {
        DateFormat timeFormat = countdownTimeFormatFactory.getTimeFormat();
        String countdownEndsString = timeFormat.format(countdownEnds);
        String title = resources.getString(R.string.countdown_notification_title);
        String text = resources.getString(R.string.countdown_notification_text, countdownEndsString);

        PendingIntent tapIntent =
                PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(text)
                .setTicker(title)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(tapIntent)
                .setOngoing(true)
                .setAutoCancel(false);

        return builder.build();
    }

    /**
     * Posts the timer countdown notification to the system status bar.
     *
     * @param countdownEnds The date and time the countdown is set to expire
     */
    public void postNotification(Date countdownEnds) {
        Notification notification = getNotification(countdownEnds);

        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    /**
     * Cancels and removes the timer countdown notification from the system status bar. If the notification is not
     * present, this method has no effect.
     */
    public void cancelNotification() {
        notificationManager.cancel(NOTIFICATION_ID);
    }


    /**
     * Produces time formats according to the system's 12/24-hour clock preference.
     */
    public static class TimeFormatFactory {

        private final Context context;

        /**
         * Constructs an instance of {@link TimeFormatFactory}.
         *
         * @param context The context
         */
        public TimeFormatFactory(Context context) {
            this.context = context;
        }

        /**
         * Returns a time format according to the system's current 12/24-hour clock preference.
         *
         * @return A {@link DateFormat}
         */
        public DateFormat getTimeFormat() {
            return android.text.format.DateFormat.getTimeFormat(context);
        }
    }
}
