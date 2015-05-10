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

package nz.net.speakman.destinyraidtimers.consumables.views;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sefford.circularprogressdrawable.CircularProgressDrawable;
import com.squareup.otto.Bus;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import nz.net.speakman.destinyraidtimers.R;
import nz.net.speakman.destinyraidtimers.RaidApplication;
import nz.net.speakman.destinyraidtimers.consumables.ConsumablesTimer;

/**
 * Created by Adam on 15-03-28.
 */
public abstract class ConsumablesCountdownView extends RelativeLayout {

    private static final String KEY_SUPER_STATE = "nz.net.speakman.destinyraidtimers.consumables.views.ConsumablesCountdownView.KEY_SUPER_STATE";
    private static final String KEY_COUNTDOWN_PROGRESS = "nz.net.speakman.destinyraidtimers.consumables.views.ConsumablesCountdownView.KEY_COUNTDOWN_PROGRESS";
    private static final String KEY_COUNTDOWN_LABEL = "nz.net.speakman.destinyraidtimers.consumables.views.ConsumablesCountdownView.KEY_COUNTDOWN_LABEL";

    private static final float ICON_MIN_SCALE = 0.5f;
    private static final float ICON_MAX_SCALE = 1.0f;
    private static final float TEXT_MIN_SCALE = 1.0f;
    private static final float TEXT_MAX_SCALE = 3.5f;

    protected abstract static class AnimationEndListener implements Animator.AnimatorListener {

        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }
    }

    protected abstract static class AnimationStartListener implements Animator.AnimatorListener {

        @Override
        public void onAnimationEnd(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }
    }

    // TODO Put in resources?...
    protected static final int RESET_ANIMATION_DURATION = 750;

    public static String formatMinutesFromMillis(long millis) {
        return String.format("%d:%02d", TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis % (1000 * 60)));
    }

    @InjectView(R.id.consumables_countdown_label)
    TextView countdownText;

    @InjectView(R.id.consumables_countdown_image)
    ImageView progressView;

    @InjectView(R.id.consumables_countdown_icon)
    ImageView consumableIcon;

    @InjectView(R.id.consumables_countdown_timer_reset)
    View resetButton;

    @Inject
    Bus bus;

    ObjectAnimator progressAnimator;
    CircularProgressDrawable progressDrawable;

    public ConsumablesCountdownView(Context context) {
        super(context);
        init(context);
    }

    public ConsumablesCountdownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ConsumablesCountdownView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ConsumablesCountdownView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    protected void init(Context ctx) {
        inflate(ctx, R.layout.consumables_countdown, this);
        ButterKnife.inject(this);
        RaidApplication.getApplication().inject(this);
        Resources resources = ctx.getResources();
        progressDrawable = new CircularProgressDrawable.Builder()
                .setRingColor(resources.getColor(R.color.consumables_primary_light))
                .setRingWidth(resources.getDimensionPixelSize(R.dimen.consumables_progress_width))
                .create();
        progressDrawable.setProgress(1f);
        progressView.setImageDrawable(progressDrawable);
        consumableIcon.setImageResource(getConsumableIconResource());
        countdownText.setText(getDefaultText());
        if (getTimer().isRunning()) {
            resetButton.setVisibility(View.VISIBLE);
            // Unfortunately this animates things into place, but I couldn't figure out how to scale
            // it explicitly while still using the correct pivot points. Weird.
            scaleDownIcon();
        }
        bus.register(this);
    }

    // We only save & restore state to prevent brief 'blips' of the initial view, until the timer refreshes.
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        Bundle outState = new Bundle();
        outState.putParcelable(KEY_SUPER_STATE, superState);
        outState.putFloat(KEY_COUNTDOWN_PROGRESS, progressDrawable.getProgress());
        outState.putString(KEY_COUNTDOWN_LABEL, countdownText.getText().toString());
        return outState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            if (getTimer().isRunning()) {
                float progress = ((Bundle) state).getFloat(KEY_COUNTDOWN_PROGRESS, 1f);
                progressDrawable.setProgress(progress);
                String text = ((Bundle)state).getString(KEY_COUNTDOWN_LABEL, getDefaultText());
                countdownText.setText(text);
                resetButton.setVisibility(View.VISIBLE);
            }
            state = ((Bundle) state).getParcelable(KEY_SUPER_STATE);
        }
        super.onRestoreInstanceState(state);
    }

    public void reset() {
        getTimer().reset();
        hideResetButton();
        resetProgressBar();
        scaleUpIcon();
    }

    protected void onTimerUpdated(long timeRemainingMs, long totalTimeMs) {
        if (timeRemainingMs == 0) {
            reset();
        } else {
            countdownText.setText(formatMinutesFromMillis(timeRemainingMs));
        }
        if (progressAnimator == null) {
            float progressPct = timeRemainingMs / (float) totalTimeMs;
            startAnimator(progressPct, timeRemainingMs);
        }
    }

    protected void resetProgressBar() {
        if (progressAnimator != null) {
            progressAnimator.cancel();
        }
        ObjectAnimator resetAnimator = ObjectAnimator.ofFloat(progressDrawable, CircularProgressDrawable.PROGRESS_PROPERTY,
                progressDrawable.getProgress(), 1f);
        resetAnimator.setDuration(RESET_ANIMATION_DURATION);
        resetAnimator.addListener(new AnimationEndListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progressAnimator = null;
            }
        });
        resetAnimator.start();
    }

    protected void startAnimator(float progressPct, long duration) {
        // Drawable goes backwards, so we count down from 1 -> 0
        progressDrawable.setProgress(progressPct);
        progressAnimator = ObjectAnimator.ofFloat(progressDrawable, CircularProgressDrawable.PROGRESS_PROPERTY,
                progressPct, 0f);
        progressAnimator.setDuration(duration);
        progressAnimator.setInterpolator(new LinearInterpolator());
        progressAnimator.start();
    }

    private void showResetButton() {
        ObjectAnimator showAnimator;
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            showAnimator = ObjectAnimator.ofFloat(resetButton, "translationY",
                    resetButton.getMeasuredHeight(), 0f);
        } else {
            showAnimator = ObjectAnimator.ofFloat(resetButton, "translationX",
                    resetButton.getMeasuredWidth(), 0f);
        }
        showAnimator.setDuration(500);
        showAnimator.setInterpolator(new DecelerateInterpolator());
        showAnimator.addListener(new AnimationStartListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                resetButton.setVisibility(View.VISIBLE);
            }
        });
        showAnimator.start();
    }

    private void hideResetButton() {
        ObjectAnimator hideAnimator;
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideAnimator = ObjectAnimator.ofFloat(resetButton, "translationY",
                    0, resetButton.getMeasuredHeight());
        } else {
            hideAnimator = ObjectAnimator.ofFloat(resetButton, "translationX",
                    0, resetButton.getMeasuredWidth());
        }
        hideAnimator.setDuration(500);
        hideAnimator.setInterpolator(new AccelerateInterpolator());
        hideAnimator.addListener(new AnimationEndListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                resetButton.setVisibility(View.INVISIBLE);
            }
        });
        hideAnimator.start();
    }

    private void scaleUpIcon() {
        ScaleAnimation scaleIcon = new ScaleAnimation(ICON_MIN_SCALE, ICON_MAX_SCALE, ICON_MIN_SCALE, ICON_MAX_SCALE,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0f);
        scaleIcon.setDuration(500);
        scaleIcon.setFillAfter(true);
        scaleIcon.setInterpolator(new DecelerateInterpolator());
        consumableIcon.startAnimation(scaleIcon);
        ScaleAnimation scaleText = new ScaleAnimation(TEXT_MAX_SCALE, TEXT_MIN_SCALE, TEXT_MAX_SCALE, TEXT_MIN_SCALE,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1f);
        scaleText.setDuration(500);
        scaleText.setFillAfter(true);
        scaleText.setInterpolator(new DecelerateInterpolator());
        // This is not an Animator listener, but an Animation listener. Obviously.
        scaleText.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                countdownText.setText(getDefaultText());
            }

            @Override public void onAnimationRepeat(Animation animation) { }
        });
        countdownText.startAnimation(scaleText);
    }

    private void scaleDownIcon() {
        ScaleAnimation scaleIcon = new ScaleAnimation(ICON_MAX_SCALE, ICON_MIN_SCALE, ICON_MAX_SCALE, ICON_MIN_SCALE,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0f);
        scaleIcon.setDuration(500);
        scaleIcon.setFillAfter(true);
        scaleIcon.setInterpolator(new DecelerateInterpolator());
        consumableIcon.startAnimation(scaleIcon);
        ScaleAnimation scaleText = new ScaleAnimation(TEXT_MIN_SCALE, TEXT_MAX_SCALE, TEXT_MIN_SCALE, TEXT_MAX_SCALE,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1f);
        scaleText.setDuration(500);
        scaleText.setFillAfter(true);
        scaleText.setInterpolator(new DecelerateInterpolator());
        countdownText.startAnimation(scaleText);
    }

    @OnClick(R.id.consumables_countdown_image)
    public void onCountdownClick() {
        if (getTimer().isRunning()) {
            return;
        }
        countdownText.setText(getDefaultText());
        showResetButton();
        scaleDownIcon();
        getTimer().start();
    }

    @OnClick(R.id.consumables_countdown_timer_reset)
    public void onResetClick() {
        reset();
    }

    protected abstract String getDefaultText();

    protected abstract ConsumablesTimer getTimer();

    @DrawableRes
    protected abstract int getConsumableIconResource();
}
