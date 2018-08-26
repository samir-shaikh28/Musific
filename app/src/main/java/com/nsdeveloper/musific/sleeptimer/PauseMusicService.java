/*
Copyright (c) 2013 Joel Andrews
Distributed under the MIT License: http://opensource.org/licenses/MIT
 */

package com.nsdeveloper.musific.sleeptimer;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

/**
 * <p>
 * A service that pauses all music playback on the device and ensures that it remains paused until playback is
 * explicitly started again.
 * </p>
 * <p>
 * Certain music apps (e.g. Rdio) do not adhere to Android's guidelines for
 * <a href="http://developer.android.com/training/managing-audio/audio-focus.html#HandleFocusLoss">
 *     handling the loss of audio focus
 * </a>
 * and will immediately reclaim audio focus as soon as it's available; therefore, this service claims audio focus and
 * attempts to keep it until another app explicitly requests audio focus. See
 * <a href="https://github.com/rdio/api/issues/90">Rdio API issue #90</a> for a more detailed description of the
 * problem.
 * </p>
 *
 * @author Joel Andrews
 */
public class PauseMusicService extends IntentService {

    private static final String LOG_TAG = PauseMusicService.class.getName();

    private AudioManager audioManager;
    private AudioManager.OnAudioFocusChangeListener listener;
    private PauseMusicNotifier notifier;
    private boolean isMusicPaused;

    private final Object syncLock = new Object();

    /**
     * Constructs an instance of {@link PauseMusicService}.
     */
    public PauseMusicService() {
        super(PauseMusicService.class.getName());
    }

    @Override
    public final void onCreate() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        onCreate(
                audioManager,
                new AudioFocusListener(audioManager),
                PauseMusicNotifier.get(getApplicationContext())
        );
    }

    /**
     * Initializes the service's dependencies.
     *
     * @param audioManager The audio manager to use
     * @param listener The audio focus change listener to use
     * @param notifier Used to post notifications when music playback is paused
     */
    protected void onCreate(
            AudioManager audioManager,
            AudioManager.OnAudioFocusChangeListener listener,
            PauseMusicNotifier notifier) {

        super.onCreate();

        this.audioManager = audioManager;
        this.listener = listener;
        this.notifier = notifier;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        // This should force the service to remain running without requiring a foreground service with a persistent
        // notification. Source: http://stackoverflow.com/a/12017536
        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Attempt to hold onto audio focus indefinitely; only if another app explicitly requests audio focus
        // will this service willingly let it go
        if (pauseAndNotify()) {
            Log.i(LOG_TAG, "Successfully paused music playback");

            isMusicPaused = true;

            synchronized (syncLock) {
                while (isMusicPaused) {
                    try {
                        // Wait indefinitely to ensure that the service is not recycled and that it keeps audio focus
                        syncLock.wait();
                    } catch (InterruptedException ex) {
                        Log.d(LOG_TAG, "Service interrupted", ex);
                    }
                }
            }
        } else {
            Log.e(LOG_TAG, "Failed to pause music playback");
        }
    }

    /**
     * Pauses all music playback on the device and posts a notification to the status bar.
     *
     * @return {@code true} if playback was successfully paused; otherwise, {@code false}
     */
    boolean pauseAndNotify() {
        if (pauseMusicPlayback()) {
            notifier.postNotification();

            return true;
        } else {
            return false;
        }
    }

    /**
     * Pauses all music playback on the device.
     *
     * @return {@code true} if playback was successfully paused; otherwise, {@code false}
     */
    private boolean pauseMusicPlayback() {
        // Taking audio focus should force other apps to pause/stop music playback
        int audioFocusResult =
                audioManager.requestAudioFocus(listener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return audioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Because this is a sticky service, we should generally only get here if music playback has been manually
        // restarted or the user has manually killed the service's process; in any event, release all resources
        audioManager.abandonAudioFocus(listener);
        isMusicPaused = false;
        notifier.cancelNotification();

        synchronized (syncLock) {
            syncLock.notify();
        }

        notifier = null;
        listener = null;
        audioManager = null;
    }

    /**
     * A listener for audio focus change events.
     */
    class AudioFocusListener implements AudioManager.OnAudioFocusChangeListener {

        private AudioManager audioManager;

        /**
         * Constructs an instance of {@link AudioFocusListener}.
         *
         * @param audioManager The audio manager to use.
         */
        public AudioFocusListener(AudioManager audioManager) {
            this.audioManager = audioManager;
        }

        @Override
        public void onAudioFocusChange(int focusChange) {

            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    Log.d(LOG_TAG, "Audio focus lost permanently");

                    // Since audio focus has been permanently taken by another process (e.g. the user explicitly
                    // restarted music playback), the service can be stopped and audio focus released so this process
                    // does not automatically reclaim audio focus when/if the new owner relinquishes it
                    audioManager.abandonAudioFocus(this);
                    stopSelf();

                    break;
                default:
                    Log.d(LOG_TAG, "Audio focus changed: " + focusChange);
            }
        }
    }
}
