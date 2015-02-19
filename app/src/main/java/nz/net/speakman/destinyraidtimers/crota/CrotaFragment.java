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

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.sefford.circularprogressdrawable.CircularProgressDrawable;
import com.squareup.otto.Subscribe;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import nz.net.speakman.destinyraidtimers.BaseRaidFragment;
import nz.net.speakman.destinyraidtimers.R;

/**
 * Created by Adam on 15-02-15.
 */
public class CrotaFragment extends BaseRaidFragment {

    private abstract static class AnimationEndListener implements Animator.AnimatorListener {

        @Override public void onAnimationStart(Animator animation) { }

        @Override public void onAnimationRepeat(Animator animation) { }

        @Override public void onAnimationCancel(Animator animation) { }
    }

    private static final String KEY_TIMER_RUNNING = "nz.net.speakman.destinyraidtimers.crota.KEY_TIMER_RUNNING";
    private static final String KEY_PROGRESS = "nz.net.speakman.destinyraidtimers.crota.KEY_PROGRESS";
    private static final String KEY_CURRENT_POSITION = "nz.net.speakman.destinyraidtimers.crota.KEY_CURRENT_POSITION";

    private static final int RESET_ANIMATION_DURATION = 750;

    private static final int POSITION_ENRAGED = -1;
    /**
     * Center, going left.
     */
    private static final int POSITION_CENTER_L = 0;
    private static final int POSITION_LEFT = 1;
    /**
     * Center, going right.
     */
    private static final int POSITION_CENTER_R = 2;
    private static final int POSITION_RIGHT = 3;

    public static CrotaFragment newInstance() {
        return new CrotaFragment();
    }

    @Inject
    CrotaTimer timer;

    @InjectView(R.id.fragment_crota_time_elapsed)
    TextView timeElapsed;

    @InjectView(R.id.fragment_crota_time_elapsed_container)
    View timeElapsedContainer;

    @InjectView(R.id.fragment_crota_time_to_move)
    TextView timeToMove;

    @InjectView(R.id.fragment_crota_progress)
    ImageView progressView;

    @InjectView(R.id.fragment_crota_position)
    ImageView positionImage;

    @InjectView(R.id.fragment_crota_toolbar)
    Toolbar toolbar;

    private ObjectAnimator animator;
    private CircularProgressDrawable progressDrawable;
    private boolean timerRunning;
    private int currentPosition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            timerRunning = savedInstanceState.getBoolean(KEY_TIMER_RUNNING, false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.fragment_crota, container, false);
        ButterKnife.inject(this, rootView);
        Resources resources = getResources();
        progressDrawable = new CircularProgressDrawable.Builder()
                .setRingColor(resources.getColor(R.color.accent))
                .setInnerCircleScale(1f)
                .setRingWidth(resources.getDimensionPixelSize(R.dimen.fragment_crota_progress_width))
                .setCenterColor(resources.getColor(R.color.accent))
                .create();
        if (savedInstanceState == null) {
            // Could do this in XML except we're formatting a long to a timestamp from constants.
            resetLabels();
            progressDrawable.setProgress(1f);
        } else {
            progressDrawable.setProgress(savedInstanceState.getFloat(KEY_PROGRESS, 1f));
            showPosition(savedInstanceState.getInt(KEY_CURRENT_POSITION, POSITION_CENTER_L));
        }
        progressView.setImageDrawable(progressDrawable);
        ((ActionBarActivity)getActivity()).setSupportActionBar(toolbar);
        ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle("");
        if (timerRunning) {
            timeElapsedContainer.setTranslationY(0);
            progressDrawable.setCircleScale(0f);
        }
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (animator != null) {
            animator.cancel();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_TIMER_RUNNING, timerRunning);
        outState.putFloat(KEY_PROGRESS, progressDrawable.getProgress());
        outState.putInt(KEY_CURRENT_POSITION, currentPosition);
    }

    @Subscribe
    public void onTimerUpdated(CrotaUpdateEvent event) {
        if (event.millisUntilFinished == 0) {
            onEnrage();
            return;
        }
        long timeElapsedMs = CrotaTimer.TIME_TO_ENRAGE_MS - event.millisUntilFinished;
        // Can't just do timeRemaining % movement_period because what if enrage isn't a multiple of 60?
        long timeToMoveMs = CrotaTimer.MOVEMENT_PERIOD_MS - timeElapsedMs % CrotaTimer.MOVEMENT_PERIOD_MS;

        timeToMove.setText(String.valueOf((timeToMoveMs + 999) / 1000));
        timeElapsed.setText(formatMinutesFromMillis(timeElapsedMs));

        if (animator == null) {
            float progressPct = timeToMoveMs / (float) CrotaTimer.MOVEMENT_PERIOD_MS;
            // Drawable goes backwards, so we count down from 1 -> 0
            progressDrawable.setProgress(progressPct);
            animator = ObjectAnimator.ofFloat(progressDrawable, CircularProgressDrawable.PROGRESS_PROPERTY,
                    progressPct, 0f);
            animator.setDuration(timeToMoveMs);
            animator.setInterpolator(new LinearInterpolator());
            animator.addListener(new AnimationEndListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    showNextPosition();
                    resetProgressBar();
                }
            });
            animator.start();
        }
    }

    @OnClick(R.id.fragment_crota_progress)
    public void onProgressClick() {
        if (timerRunning) {
            timer.cancel();
            resetProgressBar();
            hideTimeElapsedContainer();
            showPosition(POSITION_CENTER_L);
            blowBubble();
            resetLabels();
        } else {
            timer.start();
            showTimeElapsedContainer();
            popBubble();
        }
        timerRunning = !timerRunning;
    }

    private void onEnrage() {
        resetProgressBar();
        showPosition(POSITION_ENRAGED);
        timeToMove.setText(R.string.crota_timer_action_enraged);
    }

    private String formatMinutesFromMillis(long millis) {
        return String.format("%d:%02d", TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis % (1000 * 60)));
    }

    private void showNextPosition() {
        showPosition((currentPosition + 1) % 4);
    }

    private void showPosition(int position) {
        int imageResource;
        switch (position) {
            case POSITION_ENRAGED:
            case POSITION_CENTER_L:
            case POSITION_CENTER_R:
                imageResource = R.drawable.crota_position_center;
                break;
            case POSITION_LEFT:
                imageResource = R.drawable.crota_position_left;
                break;
            case POSITION_RIGHT:
                imageResource = R.drawable.crota_position_right;
                break;
            default:
                throw new IllegalStateException(String.format("Invalid Crota position supplied (%s)", position));
        }
        positionImage.setImageResource(imageResource);
        currentPosition = position;
    }

    private void resetLabels() {
        timeToMove.setText(R.string.crota_timer_action_start);
        timeElapsed.setText(formatMinutesFromMillis(0));
    }

    private void resetProgressBar() {
        ObjectAnimator resetAnimator = ObjectAnimator.ofFloat(progressDrawable, CircularProgressDrawable.PROGRESS_PROPERTY,
                progressDrawable.getProgress(), 1f);
        resetAnimator.setDuration(RESET_ANIMATION_DURATION);
        resetAnimator.addListener(new AnimationEndListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animator = null;
            }
        });
        if (animator != null) {
            animator.cancel();
        }
        resetAnimator.start();
    }

    private void popBubble() {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(progressDrawable, CircularProgressDrawable.CIRCLE_SCALE_PROPERTY,
                1f, 0f).setDuration(RESET_ANIMATION_DURATION);
        objectAnimator.setInterpolator(new DecelerateInterpolator());
        objectAnimator.start();
    }

    private void blowBubble() {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(progressDrawable, CircularProgressDrawable.CIRCLE_SCALE_PROPERTY,
                0f, 1f).setDuration(RESET_ANIMATION_DURATION);
        objectAnimator.setInterpolator(new DecelerateInterpolator());
        objectAnimator.start();
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
    }

}
