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
import android.util.AttributeSet;

import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import nz.net.speakman.destinyraidtimers.R;
import nz.net.speakman.destinyraidtimers.consumables.timers.GlimmerTimer;
import nz.net.speakman.destinyraidtimers.consumables.timers.GlimmerTimerUpdateEvent;
import nz.net.speakman.destinyraidtimers.consumables.timers.ConsumablesTimer;

/**
 * Created by Adam on 15-03-28.
 */
public class GlimmerCountdownView extends ConsumablesCountdownView {

    @Inject
    GlimmerTimer glimmerTimer;

    public GlimmerCountdownView(Context context) {
        super(context);
    }

    public GlimmerCountdownView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GlimmerCountdownView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public GlimmerCountdownView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Subscribe
    public void onGlimmerTimerUpdate(GlimmerTimerUpdateEvent event) {
        long timeRemainingMs = event.getMillisRemaining();
        onTimerUpdated(timeRemainingMs, GlimmerTimer.TOTAL_TIME_MS);
    }

    protected String getDefaultText() {
        return formatMinutesFromMillis(GlimmerTimer.TOTAL_TIME_MS);
    }

    protected ConsumablesTimer getTimer() {
        return glimmerTimer;
    }

    @Override
    protected int getConsumableIconResource() {
        return R.drawable.consumable_glimmer;
    }
}
