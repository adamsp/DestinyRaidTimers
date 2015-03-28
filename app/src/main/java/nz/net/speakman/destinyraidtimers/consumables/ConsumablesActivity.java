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

package nz.net.speakman.destinyraidtimers.consumables;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import nz.net.speakman.destinyraidtimers.BaseRaidActivity;
import nz.net.speakman.destinyraidtimers.R;
import nz.net.speakman.destinyraidtimers.consumables.views.Consumables10CountdownView;
import nz.net.speakman.destinyraidtimers.consumables.views.Consumables30CountdownView;

/**
 * Created by Adam on 15-03-28.
 */
public class ConsumablesActivity extends BaseRaidActivity {

    private static final String DIALOG_TAG = "nz.net.speakman.destinyraidtimers.crota.ConsumablesActivity.DIALOG_TAG";

    @Inject
    Consumables10Timer consumables10Timer;

    @Inject
    Consumables30Timer consumables30Timer;

    @InjectView(R.id.activity_consumables_10_countdown)
    Consumables10CountdownView consumables10CountdownView;

    @InjectView(R.id.activity_consumables_30_countdown)
    Consumables30CountdownView consumables30CountdownView;

    @InjectView(R.id.activity_consumables_toolbar)
    Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consumables);
        ButterKnife.inject(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}