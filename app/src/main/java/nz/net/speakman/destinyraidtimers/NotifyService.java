/*
 * Copyright 2015 Adam Speakman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nz.net.speakman.destinyraidtimers;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.io.IOException;

import javax.inject.Inject;

import nz.net.speakman.destinyraidtimers.consumables.timers.GlimmerTimerUpdateEvent;
import nz.net.speakman.destinyraidtimers.consumables.timers.TelemetryTimerUpdateEvent;
import timber.log.Timber;

/**
 * Created by Adam on 15-05-17.
 */
public class NotifyService extends Service implements MediaPlayer.OnPreparedListener {

    private MediaPlayer mediaPlayer;
    private boolean prepared;

    @Inject
    protected Bus bus;

    private MediaPlayer.OnCompletionListener onCompletedPlayAgain = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            mp.setOnCompletionListener(null);
            playNotificationSound();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ((RaidApplication)getApplicationContext()).inject(this);
        bus.register(this);
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if (notification == null) {
                Timber.w("No default notification sound is available.");
                return;
            }
            mediaPlayer = new MediaPlayer();
            // Using the Notification stream for non-notifications is a) probably bad practice and
            // b) doesn't even work if you have an Android Wear device attached and the phone is muted.
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(getApplicationContext(), notification);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Timber.e(e, "Failure setting up media player.");
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        prepared = true;
    }

    @Subscribe
    public void onGlimmerTimerUpdateEvent(GlimmerTimerUpdateEvent event) {
        if (event.timerIsFinished()) {
            playNotificationSound();
        }
    }

    @Subscribe
    public void onTelemetryTimerUpdateEvent(TelemetryTimerUpdateEvent event) {
        if (event.timerIsFinished()) {
            playNotificationSound();
        }
    }

    /**
     * Plays the notification sound. If the sound is currently playing, queues another playback (only
     * queues one extra playback at a time).
     */
    private void playNotificationSound() {
        if (!prepared) return;

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.setOnCompletionListener(onCompletedPlayAgain);
        } else {
            mediaPlayer.start();
        }
    }
}
