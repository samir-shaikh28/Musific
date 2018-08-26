/*
Copyright (c) 2013 Joel Andrews
Distributed under the MIT License: http://opensource.org/licenses/MIT
 */

package com.nsdeveloper.musific.sleeptimer;

import android.content.Intent;
import android.app.Activity;

import java.util.Date;

/**
 * The launching point for the sleep timer.
 *
 * @author Joel Andrews
 */
public class MainActivity extends Activity {

    @Override
    protected void onResume() {
        super.onResume();

        launchActivity();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_OK:
                break;
            case RESULT_CANCELED:
                finish();

                return;
            default:
                throw new IllegalArgumentException("Argument resultCode must be be either RESULT_OK or RESULT_CANCELED");
        }
    }

    /**
     * Launches the appropriate activity depending on whether the sleep timer is currently running
     * ({@link CountdownActivity}) or not ({@link SetTimerActivity}).
     */
    private void launchActivity() {
        Date scheduledTime = TimerManager.get(this).getScheduledTime();
        Intent intent = getActivityIntent(new Date(), scheduledTime);

        startActivityForResult(intent, 0);
    }

    /**
     * Returns an intent that can be used to launch an activity to either schedule a timer or display the timer
     * countdown depending on whether a timer is currently scheduled for the future.
     *
     * @param now The current date and time
     * @param scheduledTime The date and time at which the timer should expire
     *
     * @return An {@link Intent} to launch either {@link SetTimerActivity} or {@link CountdownActivity}.
     */
    Intent getActivityIntent(Date now, Date scheduledTime) {
        Class activityClass;
        if (scheduledTime == null || scheduledTime.getTime() <= now.getTime()) {
            // If the scheduled time occurs in the past, we treat it as if the timer is no longer running
            activityClass = SetTimerActivity.class;
        } else {

            activityClass = CountdownActivity.class;
        }

        return new Intent(this, activityClass)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
}
