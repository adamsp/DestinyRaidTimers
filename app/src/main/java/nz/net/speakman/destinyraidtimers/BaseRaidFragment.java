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

import android.app.Activity;
import android.support.v4.app.Fragment;

import com.squareup.otto.Bus;

import javax.inject.Inject;

/**
 * Created by Adam on 15-02-15.
 */
public class BaseRaidFragment extends Fragment {

    @Inject
    protected Bus bus;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((RaidApplication)activity.getApplicationContext()).inject(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        bus.register(this);
    }

    @Override
    public void onPause(){
        super.onPause();
        bus.unregister(this);
    }
}
