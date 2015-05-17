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

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import nz.net.speakman.destinyraidtimers.consumables.timers.GlimmerTimerUpdateEvent;
import nz.net.speakman.destinyraidtimers.consumables.timers.TelemetryTimerUpdateEvent;

/**
 * Created by Adam on 15-05-17.
 */
public class NotifyService extends Service {

    private static final int NOTIFICATION_ID_GLIMMER_CONSUMABLE = 0;
    private static final int NOTIFICATION_ID_TELEMTRY_CONSUMABLE = 1;

    @Inject
    protected Bus bus;

    @Inject
    protected Preferences preferences;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ((RaidApplication)getApplicationContext()).inject(this);
        bus.register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
    }

    @Subscribe
    public void onGlimmerTimerUpdateEvent(GlimmerTimerUpdateEvent event) {
        if (event.timerIsFinished()) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setDefaults(getNotificationDefaults())
                    .setVibrate(new long[]{1000})
                    .setSmallIcon(R.drawable.consumable_glimmer)
                    .setContentTitle(getString(R.string.notification_title_consumable_expired))
                    .setContentText(getString(R.string.notification_text_consumable_glimmer_expired));
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID_GLIMMER_CONSUMABLE, builder.build());
        }
    }

    @Subscribe
    public void onTelemetryTimerUpdateEvent(TelemetryTimerUpdateEvent event) {
        if (event.timerIsFinished()) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setDefaults(getNotificationDefaults())
                    .setVibrate(new long[]{1000})
                    .setSmallIcon(R.drawable.consumable_telemetry)
                    .setContentTitle(getString(R.string.notification_title_consumable_expired))
                    .setContentText(getString(R.string.notification_text_consumable_telemetry_expired));
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID_TELEMTRY_CONSUMABLE, builder.build());
        }
    }

    private int getNotificationDefaults() {
        int defaults = NotificationCompat.DEFAULT_LIGHTS;
        if (preferences.soundsEnabled()) {
            defaults |= NotificationCompat.DEFAULT_SOUND;
        }
        if (preferences.vibrationEnabled()) {
            defaults |= NotificationCompat.DEFAULT_VIBRATE;
        }
        return defaults;
    }
}
