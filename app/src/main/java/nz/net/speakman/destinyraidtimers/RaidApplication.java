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

import android.app.Application;
import android.content.Intent;

import dagger.ObjectGraph;
import nz.net.speakman.destinyraidtimers.analytics.Analytics;
import nz.net.speakman.destinyraidtimers.analytics.Forest;
import timber.log.Timber;

/**
 * Created by Adam on 15-02-15.
 */
public class RaidApplication extends Application {

    private static RaidApplication mContext;
    private ObjectGraph graph;

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(Forest.uproot());
        Analytics.initialize(this);
        mContext = this;
        graph = ObjectGraph.create(new BaseRaidModule());
        inject(this);
        startService(new Intent(this, NotifyService.class));
    }

    public static RaidApplication getApplication() {
        return mContext;
    }

    public void inject(Object object) {
        graph.inject(object);
    }
}
