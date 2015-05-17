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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import nz.net.speakman.destinyraidtimers.consumables.ConsumablesActivity;
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
            Notification notif = getConsumableNotification(R.string.notification_title_consumable_expired,
                    R.string.notification_text_consumable_glimmer_expired,
                    R.drawable.consumable_glimmer);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID_GLIMMER_CONSUMABLE, notif);
        }
    }

    @Subscribe
    public void onTelemetryTimerUpdateEvent(TelemetryTimerUpdateEvent event) {
        if (event.timerIsFinished()) {
            Notification notif = getConsumableNotification(R.string.notification_title_consumable_expired,
                    R.string.notification_text_consumable_telemetry_expired,
                    R.drawable.consumable_telemetry);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID_TELEMTRY_CONSUMABLE, notif);
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

    private Notification getConsumableNotification(@StringRes int title, @StringRes int text, @DrawableRes int icon) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentIntent(getConsumablesIntent())
                .setDefaults(getNotificationDefaults())
                .setAutoCancel(true)
                .setVibrate(new long[]{1000})
                .setSmallIcon(icon)
                .setContentTitle(getString(title))
                .setContentText(getString(text));
        return builder.build();
    }

    private PendingIntent getConsumablesIntent() {
        Intent consumablesIntent = new Intent(this, ConsumablesActivity.class);
        TaskStackBuilder builder = TaskStackBuilder.create(this);
        builder.addParentStack(MainActivity.class);
        builder.addNextIntent(consumablesIntent);
        return builder.getPendingIntent(0, 0);
    }
}
