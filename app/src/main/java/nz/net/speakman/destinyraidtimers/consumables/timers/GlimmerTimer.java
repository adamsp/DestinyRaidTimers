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

import com.squareup.otto.Bus;

/**
 * Created by Adam on 15-03-28.
 */
public class GlimmerTimer extends ConsumablesTimer {
    public static final long UPDATE_INTERVAL = 100; // 100 ms
    public static final long TOTAL_TIME_MS = 1000 * 10 * 60; // 10 minutes

    public GlimmerTimer(Bus bus) {
        super(bus, new GlimmerTimerUpdateEvent(), TOTAL_TIME_MS, UPDATE_INTERVAL);
    }
}
