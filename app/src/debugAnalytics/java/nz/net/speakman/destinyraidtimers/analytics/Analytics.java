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

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

/**
 * Created by Adam on 15-02-15.
 */
public class Analytics {
    public static void initialize(Application app) {
        Timber.d("{analytics enabled} Enabling Crashlytics");
        Timber.d("{analytics enabled} Enabling Answers");
        Fabric fabric = new Fabric.Builder(app)
                .kits(new Crashlytics(), new Answers())
                .debuggable(true)
                .build();
        Fabric.with(fabric);
    }
}
