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

package nz.net.speakman.destinyraidtimers.crota;

import android.os.CountDownTimer;

import com.squareup.otto.Bus;

/**
 * Created by Adam on 15-02-21.
 */
public class CrotaEnrageTimer extends CountDownTimer {
    /**
     * From crystal activation to glass break is 22 seconds.
     * From glass break, Crota enrages after 10 minutes: https://www.bungie.net/en/Forum/Post/84580574/0/0
     */
    public static final long TIME_TO_GLASS_BREAK_MS = 22 * 1000;
    /**
     * Time from the first 'Crotas Presence Debuff' (crystal activation) until enrage.
     * Includes {@link #TIME_TO_GLASS_BREAK_MS}.
     */
    public static final long TIME_TO_ENRAGE_MS = 10 * 60 * 1000 + TIME_TO_GLASS_BREAK_MS;
    public static final long UPDATE_INTERVAL = 100; // 100 ms

    private final Bus bus;
    private CrotaEnrageTimerUpdateEvent event;
    private boolean enraged;
    private boolean running;

    public CrotaEnrageTimer(Bus bus) {
        super(TIME_TO_ENRAGE_MS, UPDATE_INTERVAL);
        this.bus = bus;
        event = new CrotaEnrageTimerUpdateEvent();
    }

    @Override
    public void onTick(long millisUntilFinished) {
        running = true;
        event.setMillisUntilEnrage(millisUntilFinished);
        updateListeners();
    }

    @Override
    public void onFinish() {
        running = false;
        event.setMillisUntilEnrage(0L);
        enraged = true;
        updateListeners();
    }

    public boolean isEnraged() {
        return enraged;
    }

    public void reset() {
        cancel();
        running = false;
        enraged = false;
        event.setMillisUntilEnrage(TIME_TO_ENRAGE_MS);
    }

    public boolean isRunning() {
        return running;
    }

    private void updateListeners() {
        bus.post(event);
    }
}
