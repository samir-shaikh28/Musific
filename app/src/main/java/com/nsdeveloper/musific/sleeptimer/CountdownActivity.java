/*
Copyright (c) 2013 Joel Andrews
Distributed under the MIT License: http://opensource.org/licenses/MIT
 */

package com.nsdeveloper.musific.sleeptimer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.app.Activity;
import android.os.CountDownTimer;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.nsdeveloper.musific.R;

import java.util.Calendar;
import java.util.Date;

/**
 * Displays a dynamic countdown timer.
 *
 * @author Joel Andrews
 */
public class CountdownActivity extends Activity {

    private static final String LOG_TAG = CountdownActivity.class.getName();

    private TextView timeRemainingView;
    private CountDownTimer countDownTimer;
    private TimerManager timerManager;
    private CountdownNotifier countdownNotifier;

    @SuppressLint("MissingSuperCall")
    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        onCreate(savedInstanceState, TimerManager.get(this), CountdownNotifier.get(this));
    }

    /**
     * Initializes the activity's dependencies.
     *
     * @param savedInstanceState The activity's previous state
     * @param timerManager The timer manager to use
     * @param countdownNotifier The countdown notifier to use
     */
    protected void onCreate(Bundle savedInstanceState, TimerManager timerManager, CountdownNotifier countdownNotifier) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_countdown);

        this.timeRemainingView = (TextView) findViewById(R.id.time_remaining_view);

        this.timerManager = timerManager;
        this.countdownNotifier = countdownNotifier;
    }

    @Override
    protected void onResume() {
        super.onResume();

        startCountdown();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    /**
     * Initializes and starts the countdown timer.
     */
    private void startCountdown() {
        Calendar calendarNow = Calendar.getInstance();
        Date scheduledTime = timerManager.getScheduledTime();

        if (scheduledTime == null || scheduledTime.getTime() <= calendarNow.getTimeInMillis()) {
            // The timer has already expired; return to the caller
            prepareReturnToSender();

            return;
        }

        long timerMillis = scheduledTime.getTime() - calendarNow.getTimeInMillis();

        countDownTimer = new MyCountDownTimer(timerMillis).start();

        countdownNotifier.postNotification(scheduledTime);
    }

    /**
     * Stops the countdown timer.
     *
     * @param view The view that triggered this action
     */
    public void stopCountdown(View view) {
        Log.d(LOG_TAG, "Sleep timer canceled by view " + view.getId());

        timerManager.cancelTimer();
        countdownNotifier.cancelNotification();

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        Toast.makeText(this, R.string.timer_cancelled, Toast.LENGTH_SHORT).show();

        // Finish the activity
        prepareReturnToSender();
    }


    public void hide(View v){
        prepareReturnToHome();

    }


    /**
     * Sets the result to {@link #RESULT_OK} and finishes execution. Callers should immediately return after invoking
     * this method.
     */
    private void prepareReturnToSender() {
        setResult(RESULT_OK);
        finish();
    }

    public void prepareReturnToHome(){
        setResult(RESULT_CANCELED);
        finish();
    }


    /**
     * A countdown timer that updates the time remaining text view every 1 second.
     */
    private class MyCountDownTimer extends CountDownTimer {

        // The number of milliseconds between updates of the countdown timer
        private static final long TICK_INTERVAL = 1000;

        public MyCountDownTimer(long millisInFuture) {
            super(millisInFuture, TICK_INTERVAL);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            long secondsRemaining = millisUntilFinished / 1000;
            String timeRemainingString = DateUtils.formatElapsedTime(secondsRemaining);

            CountdownActivity.this.timeRemainingView.setText(timeRemainingString);
        }

        @Override
        public void onFinish() {
            CountdownActivity.this.prepareReturnToSender();
        }
    }
}
