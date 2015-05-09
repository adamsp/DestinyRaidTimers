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

package nz.net.speakman.destinyraidtimers.consumables.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import nz.net.speakman.destinyraidtimers.R;
import nz.net.speakman.destinyraidtimers.consumables.Consumables10Timer;
import nz.net.speakman.destinyraidtimers.consumables.Consumables10TimerUpdateEvent;
import nz.net.speakman.destinyraidtimers.consumables.ConsumablesTimer;

/**
 * Created by Adam on 15-03-28.
 */
public class Consumables10CountdownView extends ConsumablesCountdownView {

    @Inject
    Consumables10Timer consumables10Timer;

    public Consumables10CountdownView(Context context) {
        super(context);
    }

    public Consumables10CountdownView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Consumables10CountdownView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Consumables10CountdownView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Subscribe
    public void onConsumable10TimerUpdate(Consumables10TimerUpdateEvent event) {
        long timeRemainingMs = event.getMillisRemaining();
        onTimerUpdated(timeRemainingMs, Consumables10Timer.TOTAL_TIME_MS);
    }

    protected String getDefaultText() {
        return formatMinutesFromMillis(Consumables10Timer.TOTAL_TIME_MS);
    }

    protected ConsumablesTimer getTimer() {
        return consumables10Timer;
    }

    @Override
    protected int getConsumableIconResource() {
        return R.drawable.consumable_glimmer;
    }
}
