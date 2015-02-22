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
    public static final long TIME_TO_ENRAGE_MS = 10 * 60 * 1000; // 10 minutes
    public static final long UPDATE_INTERVAL = 100; // 100 ms

    private final Bus bus;
    private CrotaEnrageTimerUpdateEvent event;
    private boolean enraged;

    public CrotaEnrageTimer(Bus bus) {
        super(TIME_TO_ENRAGE_MS, UPDATE_INTERVAL);
        this.bus = bus;
        event = new CrotaEnrageTimerUpdateEvent();
    }

    @Override
    public void onTick(long millisUntilFinished) {
        event.setMillisUntilEnrage(millisUntilFinished);
        updateListeners();
    }

    @Override
    public void onFinish() {
        event.setMillisUntilEnrage(0L);
        enraged = true;
        updateListeners();
    }

    public boolean isEnraged() {
        return enraged;
    }

    public void reset() {
        cancel();
        enraged = false;
        event.setMillisUntilEnrage(TIME_TO_ENRAGE_MS);
    }

    private void updateListeners() {
        bus.post(event);
    }
}
