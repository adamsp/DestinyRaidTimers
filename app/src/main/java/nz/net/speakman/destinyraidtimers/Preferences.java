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

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Adam on 15-05-17.
 */
public class Preferences {

    private static final String PREFS_FILE_NAME = "nz.net.speakman.destinyraidtimers.Preferences.DEFAULT_FILE";

    private static final String KEY_SOUNDS = "nz.net.speakman.destinyraidtimers.Preferences.KEY_SOUNDS";
    private static final String KEY_VIBRATION = "nz.net.speakman.destinyraidtimers.Preferences.KEY_VIBRATION";

    private SharedPreferences sharedPreferences;

    public Preferences(Context ctx) {
        sharedPreferences = ctx.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE);
    }

    public boolean soundsEnabled() {
        return sharedPreferences.getBoolean(KEY_SOUNDS, true);
    }

    public void setSoundsEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_SOUNDS, enabled).apply();
    }

    public boolean vibrationEnabled() {
        return sharedPreferences.getBoolean(KEY_VIBRATION, true);
    }

    public void setVibrationEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_VIBRATION, enabled).apply();
    }

}
