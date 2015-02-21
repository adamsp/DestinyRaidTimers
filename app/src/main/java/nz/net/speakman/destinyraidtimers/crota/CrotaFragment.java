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
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import nz.net.speakman.destinyraidtimers.BaseRaidFragment;
import nz.net.speakman.destinyraidtimers.R;
import nz.net.speakman.destinyraidtimers.crota.views.CrotaMovementCountdownView;
import nz.net.speakman.destinyraidtimers.crota.views.CrotaPositionView;

/**
 * Created by Adam on 15-02-15.
 */
public class CrotaFragment extends BaseRaidFragment {

    private static final String KEY_ENRAGE_TIMER_RUNNING = "nz.net.speakman.destinyraidtimers.crota.CrotaFragment.KEY_ENRAGE_TIMER_RUNNING";
    private static final String KEY_MOVEMENT_TIMER_RUNNING = "nz.net.speakman.destinyraidtimers.crota.CrotaFragment.KEY_MOVEMENT_TIMER_RUNNING";

    public static CrotaFragment newInstance() {
        return new CrotaFragment();
    }

    @Inject
    CrotaMovementTimer movementTimer;

    @Inject
    CrotaEnrageTimer enrageTimer;

    @InjectView(R.id.fragment_crota_time_elapsed)
    TextView timeElapsed;

    @InjectView(R.id.fragment_crota_time_elapsed_container)
    View timeElapsedContainer;

    @InjectView(R.id.fragment_crota_movement_progress)
    CrotaMovementCountdownView progressView;

    @InjectView(R.id.fragment_crota_position)
    CrotaPositionView positionView;

    @InjectView(R.id.fragment_crota_timer_indicator)
    ImageView timerIndicator;

    @InjectView(R.id.fragment_crota_timer_reset)
    View timerResetButton;

    @InjectView(R.id.fragment_crota_toolbar)
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.fragment_crota, container, false);
        ButterKnife.inject(this, rootView);
        ((ActionBarActivity)getActivity()).setSupportActionBar(toolbar);
        ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle("");
        if (enrageTimer.isEnraged()) {
            positionView.onEnrage();
            progressView.onEnrage();
        }
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_ENRAGE_TIMER_RUNNING, enrageTimerRunning);
        outState.putBoolean(KEY_MOVEMENT_TIMER_RUNNING, movementTimerRunning);
    }

    @Subscribe
    public void onEnrageTimerUpdateEvent(CrotaEnrageTimerUpdateEvent event) {
        timeElapsed.setText(formatMinutesFromMillis(event.getMillisUntilEnrage()));
    }

    @OnClick(R.id.fragment_crota_movement_progress)
    public void onTimerChangeClick() {
        if (!enrageTimerRunning) {
            startEnrageTimer();
        } else if (!movementTimerRunning) {
            startMovementTimer();
        }
    }

    @OnClick(R.id.fragment_crota_timer_reset)
    public void onResetClick() {
        reset();
    }

    private void startEnrageTimer() {
        showTimeElapsedContainer();
        enrageTimerRunning = true;
        enrageTimer.start();
        timerIndicator.setImageResource(R.drawable.crota_timer_button_movement);
        timerResetButton.setVisibility(View.VISIBLE);
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
    }

    private String formatMinutesFromMillis(long millis) {
        return String.format("%d:%02d", TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis % (1000 * 60)));
    }

    private void showTimeElapsedContainer() {
        ObjectAnimator.ofFloat(timeElapsedContainer, "translationY",
                timeElapsedContainer.getMeasuredHeight(), 0f)
                .setDuration(500).start();
    }

    private void hideTimeElapsedContainer() {
        ObjectAnimator.ofFloat(timeElapsedContainer, "translationY",
                0, timeElapsedContainer.getMeasuredHeight())
                .setDuration(500).start();
        timeElapsed.setText(formatMinutesFromMillis(0));
    }

}
