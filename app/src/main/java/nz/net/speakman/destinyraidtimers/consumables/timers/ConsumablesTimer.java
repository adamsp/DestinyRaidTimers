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

package nz.net.speakman.destinyraidtimers.consumables.timers;

import android.os.CountDownTimer;

import com.squareup.otto.Bus;

/**
 * Created by Adam on 15-03-28.
 */
public class ConsumablesTimer extends CountDownTimer {
    private Bus bus;
    private final ConsumablesTimerUpdateEvent event;
    private boolean running;

    public ConsumablesTimer(Bus bus, ConsumablesTimerUpdateEvent event,
                              long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
        this.bus = bus;
        this.event = event;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        running = true;
        event.setMillisRemaining(millisUntilFinished);
        updateListeners();
    }

    @Override
    public void onFinish() {
        running = false;
        event.setMillisRemaining(0);
        updateListeners();
    }

    public void reset() {
        cancel();
        running = false;
        event.setMillisRemaining(0);
    }

    public boolean isRunning() {
        return running;
    }

    private void updateListeners() {
        bus.post(event);
    }
}
