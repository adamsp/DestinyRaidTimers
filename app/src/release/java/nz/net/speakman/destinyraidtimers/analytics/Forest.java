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

package nz.net.speakman.destinyraidtimers.analytics;

import com.crashlytics.android.Crashlytics;

import timber.log.Timber;

/**
 * Created by Adam on 15-02-15.
 */
public class Forest {
    /**
     * Logs WARN and ERROR level log messages to Crashlytics.
     * Doesn't log to Logcat - logging in production kills puppies.
     * THINK OF THE PUPPIES.
     */
    public static class CrashlyticsTree implements Timber.Tree {
        @Override
        public void v(String message, Object... args) { }

        @Override
        public void v(Throwable t, String message, Object... args) { }

        @Override
        public void d(String message, Object... args) { }

        @Override
        public void d(Throwable t, String message, Object... args) { }

        @Override
        public void i(String message, Object... args) { }

        @Override
        public void i(Throwable t, String message, Object... args) { }

        @Override
        public void w(String message, Object... args) {
            Crashlytics.log(String.format(message, args));
        }

        @Override
        public void w(Throwable t, String message, Object... args) {
            Crashlytics.log(String.format(message, args));
            Crashlytics.logException(t);
        }

        @Override
        public void e(String message, Object... args) {
            Crashlytics.log(String.format(message, args));
        }

        @Override
        public void e(Throwable t, String message, Object... args) {
            Crashlytics.log(String.format(message, args));
            Crashlytics.logException(t);
        }
    }
    public static Timber.Tree uproot() {
        return new CrashlyticsTree();
    }
}
