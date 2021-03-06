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
 * Created by Adam on 15-02-15.
 */
public class CrotaMovementTimer extends CountDownTimer {
    public static final long UPDATE_INTERVAL = 100; // 100 ms
    public static final long MOVEMENT_PERIOD_MS = 1000 * 60; // 1 minute

    private Bus bus;
    private CrotaMovementTimerUpdateEvent event;
    private int currentPosition;
    private boolean running;

    public CrotaMovementTimer(Bus bus) {
        super(MOVEMENT_PERIOD_MS, UPDATE_INTERVAL);
        this.bus = bus;
        this.event = new CrotaMovementTimerUpdateEvent();
        event.setPosition(CrotaPosition.CENTER_R);
        currentPosition = CrotaPosition.CENTER_R.getCode();
    }

    @Override
    public void onTick(long millisUntilFinished) {
        running = true;
        event.setMillisUntilMove(millisUntilFinished);
        updateListeners();
    }

    @Override
    public void onFinish() {
        running = false;
        currentPosition = (currentPosition + 1) % 4;
        event.setPosition(CrotaPosition.get(currentPosition));
        event.setMillisUntilMove(0);
        updateListeners();
        start();
    }

    public void reset() {
        cancel();
        running = false;
        event.setPosition(CrotaPosition.CENTER_R);
        currentPosition = CrotaPosition.CENTER_R.getCode();
    }

    public boolean isRunning() {
        return running;
    }

    private void updateListeners() {
        bus.post(event);
    }
}
