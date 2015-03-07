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

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import nz.net.speakman.destinyraidtimers.BaseRaidActivity;
import nz.net.speakman.destinyraidtimers.R;
import nz.net.speakman.destinyraidtimers.crota.views.CrotaMovementCountdownView;
import nz.net.speakman.destinyraidtimers.crota.views.CrotaPositionView;

/**
 * Created by Adam on 15-03-07.
 */
public class CrotaActivity extends BaseRaidActivity {

    private static final String DIALOG_TAG = "nz.net.speakman.destinyraidtimers.crota.CrotaActivity.DIALOG_TAG";
    private static final String KEY_ENRAGE_TIMER_RUNNING = "nz.net.speakman.destinyraidtimers.crota.CrotaActivity.KEY_ENRAGE_TIMER_RUNNING";
    private static final String KEY_MOVEMENT_TIMER_RUNNING = "nz.net.speakman.destinyraidtimers.crota.CrotaActivity.KEY_MOVEMENT_TIMER_RUNNING";

    @Inject
    CrotaMovementTimer movementTimer;

    @Inject
    CrotaEnrageTimer enrageTimer;

    @InjectView(R.id.activity_crota_enrage_countdown)
    TextView enrageCountdown;

    @InjectView(R.id.activity_crota_enrage_countdown_container)
    View enrageCountdownContainer;

    @InjectView(R.id.activity_crota_movement_progress)
    CrotaMovementCountdownView progressView;

    @InjectView(R.id.activity_crota_position)
    CrotaPositionView positionView;

    @InjectView(R.id.activity_crota_timer_indicator)
    ImageView timerIndicator;

    @InjectView(R.id.activity_crota_timer_reset)
    View timerResetButton;

    @InjectView(R.id.activity_crota_toolbar)
    Toolbar toolbar;

    private boolean enrageTimerRunning;
    private boolean movementTimerRunning;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            enrageTimerRunning = savedInstanceState.getBoolean(KEY_ENRAGE_TIMER_RUNNING, false);
            movementTimerRunning = savedInstanceState.getBoolean(KEY_MOVEMENT_TIMER_RUNNING, false);
        }
        setContentView(R.layout.activity_crota);
        ButterKnife.inject(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_crota_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_crota_help) {
            new CrotaHelpDialog().show(getSupportFragmentManager(), DIALOG_TAG);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (enrageTimerRunning) {
            timerResetButton.setVisibility(View.VISIBLE);
            enrageCountdownContainer.setTranslationY(0);
            timerIndicator.setImageResource(R.drawable.crota_timer_button_movement);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        if (movementTimerRunning) {
            timerIndicator.setVisibility(View.INVISIBLE);
        }
        if (enrageTimer.isEnraged()) {
            positionView.onEnrage();
            progressView.onEnrage();
            onEnrage();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_ENRAGE_TIMER_RUNNING, enrageTimerRunning);
        outState.putBoolean(KEY_MOVEMENT_TIMER_RUNNING, movementTimerRunning);
    }

    @Subscribe
    public void onEnrageTimerUpdateEvent(CrotaEnrageTimerUpdateEvent event) {
        if (event.isEnraged()) {
            onEnrage();
        } else {
            enrageCountdown.setText(formatMinutesFromMillis(event.getMillisUntilEnrage()));
        }
    }

    @OnClick(R.id.activity_crota_movement_progress)
    public void onTimerChangeClick() {
        if (!enrageTimerRunning) {
            startEnrageTimer();
        } else if (!movementTimerRunning) {
            startMovementTimer();
        }
    }

    @OnClick(R.id.activity_crota_timer_reset)
    public void onResetClick() {
        reset();
    }

    private void onEnrage() {
        movementTimer.reset();
        enrageCountdown.setText(R.string.crota_timer_action_enraged);
        timerIndicator.setImageResource(R.drawable.crota_timer_button_enrage);
        timerIndicator.setVisibility(View.VISIBLE);
    }

    private void startEnrageTimer() {
        showTimeElapsedContainer();
        enrageTimerRunning = true;
        enrageTimer.start();
        timerIndicator.setImageResource(R.drawable.crota_timer_button_movement);
        timerResetButton.setVisibility(View.VISIBLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void startMovementTimer() {
        movementTimerRunning = true;
        movementTimer.start();
        timerIndicator.setVisibility(View.INVISIBLE);
    }

    private void reset() {
        enrageTimerRunning = false;
        enrageTimer.reset();
        movementTimerRunning = false;
        movementTimer.reset();
        hideTimeElapsedContainer();
        positionView.reset();
        progressView.reset();
        timerIndicator.setImageResource(R.drawable.crota_timer_button_crystal);
        timerIndicator.setVisibility(View.VISIBLE);
        timerResetButton.setVisibility(View.INVISIBLE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private String formatMinutesFromMillis(long millis) {
        return String.format("%d:%02d", TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis % (1000 * 60)));
    }

    private void showTimeElapsedContainer() {
        ObjectAnimator.ofFloat(enrageCountdownContainer, "translationY",
                enrageCountdownContainer.getMeasuredHeight(), 0f)
                .setDuration(500).start();
    }

    private void hideTimeElapsedContainer() {
        ObjectAnimator.ofFloat(enrageCountdownContainer, "translationY",
                0, enrageCountdownContainer.getMeasuredHeight())
                .setDuration(500).start();
        enrageCountdown.setText(formatMinutesFromMillis(0));
    }
}
