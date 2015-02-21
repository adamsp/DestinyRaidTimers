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

/**
 * Created by Adam on 15-02-15.
 */
public class CrotaMovementTimerUpdateEvent {
    private long millisUntilMove;
    private CrotaPosition position;

    public long getMillisUntilMove() {
        return millisUntilMove;
    }

    public void setMillisUntilMove(long millisUntilMove) {
        this.millisUntilMove = millisUntilMove;
    }

    public CrotaPosition getPosition() {
        return position;
    }

    public void setPosition(CrotaPosition position) {
        this.position = position;
    }
}
