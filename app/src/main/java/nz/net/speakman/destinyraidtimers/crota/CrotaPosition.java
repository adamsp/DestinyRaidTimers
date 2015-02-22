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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Adam on 15-02-21.
 */
public enum CrotaPosition {
    ENRAGE(-2),
    RESET(-1),
    CENTER_R(0),
    RIGHT(1),
    CENTER_L(2),
    LEFT(3);

    private static final Map<Integer, CrotaPosition> lookup = new HashMap<>();

    static {
        for (CrotaPosition p : EnumSet.allOf(CrotaPosition.class)) {
            lookup.put(p.getCode(), p);
        }
    }

    private int code;

    private CrotaPosition(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static CrotaPosition get(int code) {
        return lookup.get(code);
    }
}
