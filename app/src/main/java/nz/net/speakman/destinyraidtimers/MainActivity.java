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

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import butterknife.ButterKnife;
import butterknife.OnClick;
import nz.net.speakman.destinyraidtimers.crota.CrotaActivity;
import nz.net.speakman.destinyraidtimers.crota.CrotaHelpDialog;


public class MainActivity extends BaseRaidActivity {

    private static final String DIALOG_TAG = "nz.net.speakman.destinyraidtimers.MainActivity.DIALOG_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_crota_help) {
            new CrotaHelpDialog().show(getSupportFragmentManager(), DIALOG_TAG);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.activity_main_selection_crota_card)
    void onCrotaSelection() {
        startActivity(new Intent(this, CrotaActivity.class));
    }
}
