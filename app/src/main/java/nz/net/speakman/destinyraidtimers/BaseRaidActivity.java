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

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.squareup.otto.Bus;

import javax.inject.Inject;

import de.keyboardsurfer.android.widget.crouton.Crouton;

/**
 * Created by Adam on 15-03-07.
 */
public class BaseRaidActivity extends ActionBarActivity {

    @Inject
    protected Bus bus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((RaidApplication)getApplicationContext()).inject(this);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Crouton.cancelAllCroutons();
    }
}
