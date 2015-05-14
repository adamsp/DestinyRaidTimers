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
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.sefford.circularprogressdrawable.CircularProgressDrawable;
import com.squareup.otto.Bus;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import nz.net.speakman.destinyraidtimers.R;
import nz.net.speakman.destinyraidtimers.RaidApplication;
import nz.net.speakman.destinyraidtimers.consumables.timers.ConsumablesTimer;

/**
 * Created by Adam on 15-03-28.
 */
public abstract class ConsumablesCountdownView extends RelativeLayout {

    private static final String KEY_SUPER_STATE = "nz.net.speakman.destinyraidtimers.consumables.views.ConsumablesCountdownView.KEY_SUPER_STATE";
    private static final String KEY_COUNTDOWN_PROGRESS = "nz.net.speakman.destinyraidtimers.consumables.views.ConsumablesCountdownView.KEY_COUNTDOWN_PROGRESS";
    private static final String KEY_COUNTDOWN_LABEL = "nz.net.speakman.destinyraidtimers.consumables.views.ConsumablesCountdownView.KEY_COUNTDOWN_LABEL";

    private AnimatorSet startAnimation;
    private AnimatorSet resetAnimation;

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

    // Private class pulled from the FAB library (already included in licenses file): http://bit.ly/1Iu9Hgw
    private static class RotatingDrawable extends LayerDrawable {
        public RotatingDrawable(Drawable drawable) {
            super(new Drawable[] { drawable });
        }

        private float mRotation;

        @SuppressWarnings("UnusedDeclaration")
        public float getRotation() {
            return mRotation;
        }

        @SuppressWarnings("UnusedDeclaration")
        public void setRotation(float rotation) {
            mRotation = rotation;
            invalidateSelf();
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.save();
            canvas.rotate(mRotation, getBounds().centerX(), getBounds().centerY());
            super.draw(canvas);
            canvas.restore();
        }
    }

    // TODO Put in resources?...
    protected static final int RESET_ANIMATION_DURATION = 750;

    public static String formatMinutesFromMillis(long millis) {
        return String.format("%d:%02d", TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis % (1000 * 60)));
    }

    @InjectView(R.id.consumables_countdown_scale_view)
    CountdownScaleView countdownScaleView;

    @InjectView(R.id.consumables_countdown_image)
    ImageView progressView;

    @InjectView(R.id.consumables_countdown_timer_reset)
    FloatingActionButton resetButton;

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
        countdownScaleView.addScaleUpAnimationLister(new AnimationEndListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ConsumablesTimer timer = getTimer();
                if (!timer.isRunning()) {
                    timer.start();
                }
            }
        });
        countdownScaleView.addScaleDownAnimationListener(new AnimationEndListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                countdownScaleView.setText(getDefaultText());
            }
        });
        countdownScaleView.setText(getDefaultText());
        initAnimationsIfNeeded();
        bus.register(this);
    }

    private void initAnimationsIfNeeded() {
        if (startAnimation != null && resetAnimation != null) return;

        startAnimation = new AnimatorSet().setDuration(500);
        resetAnimation = new AnimatorSet().setDuration(500);

        OvershootInterpolator overshootInterpolator = new OvershootInterpolator();
        DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
        AccelerateInterpolator accelerateInterpolator = new AccelerateInterpolator();

        // Reset button
        RotatingDrawable rotatingDrawable = new RotatingDrawable(getResources().getDrawable(R.drawable.timer_button_reset));
        ObjectAnimator resetShowRotationAnimator = ObjectAnimator.ofFloat(rotatingDrawable, "rotation", 180f, 0f);
        ObjectAnimator resetHideRotationAnimator = ObjectAnimator.ofFloat(rotatingDrawable, "rotation", 0, -180f);
        resetShowRotationAnimator.setInterpolator(overshootInterpolator);
        resetHideRotationAnimator.setInterpolator(overshootInterpolator);
        resetButton.setIconDrawable(rotatingDrawable);

        ObjectAnimator resetHideTransitionAnimator;
        // TODO The animation direction should be configured via the layout file.
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            resetHideTransitionAnimator = ObjectAnimator.ofFloat(resetButton, "translationY",
                    0, resetButton.getMeasuredHeight());
        } else {
            resetHideTransitionAnimator = ObjectAnimator.ofFloat(resetButton, "translationX",
                    0, resetButton.getMeasuredWidth());
        }
        resetHideTransitionAnimator.setInterpolator(accelerateInterpolator);
        resetHideTransitionAnimator.addListener(new AnimationEndListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                resetButton.setVisibility(View.INVISIBLE);
            }
        });

        ObjectAnimator resetShowTransitionAnimator;
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            resetShowTransitionAnimator = ObjectAnimator.ofFloat(resetButton, "translationY",
                    resetButton.getMeasuredHeight(), 0f);
        } else {
            resetShowTransitionAnimator = ObjectAnimator.ofFloat(resetButton, "translationX",
                    resetButton.getMeasuredWidth(), 0f);
        }
        resetShowTransitionAnimator.setInterpolator(decelerateInterpolator);
        resetShowTransitionAnimator.addListener(new AnimationStartListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                resetButton.setVisibility(View.VISIBLE);
            }
        });

        startAnimation.play(resetShowRotationAnimator).with(resetShowTransitionAnimator);
        resetAnimation.play(resetHideRotationAnimator).with(resetHideTransitionAnimator);

        // If we've already got a timer running, we should put everything in its "timer running" state/placement.
        if (getTimer().isRunning()) {
            resetButton.setVisibility(View.VISIBLE);
            countdownScaleView.scaleUpText();
        }
    }

    // We only save & restore state to prevent brief 'blips' of the initial view, until the timer refreshes.
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        Bundle outState = new Bundle();
        outState.putParcelable(KEY_SUPER_STATE, superState);
        outState.putFloat(KEY_COUNTDOWN_PROGRESS, progressDrawable.getProgress());
        return outState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            if (getTimer().isRunning()) {
                float progress = ((Bundle) state).getFloat(KEY_COUNTDOWN_PROGRESS, 1f);
                progressDrawable.setProgress(progress);
                String text = ((Bundle)state).getString(KEY_COUNTDOWN_LABEL, getDefaultText());
                countdownScaleView.setText(text);
                resetButton.setVisibility(View.VISIBLE);
            }
            state = ((Bundle) state).getParcelable(KEY_SUPER_STATE);
        }
        super.onRestoreInstanceState(state);
    }

    public void reset() {
        getTimer().reset();
        resetProgressBar();
        countdownScaleView.scaleDownText();
        startAnimation.cancel();
        resetAnimation.start();
    }

    protected void onTimerUpdated(long timeRemainingMs, long totalTimeMs) {
        if (timeRemainingMs == 0) {
            reset();
        } else {
            countdownScaleView.setText(formatMinutesFromMillis(timeRemainingMs));
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

    @OnClick(R.id.consumables_countdown_image)
    public void onCountdownClick() {
        if (getTimer().isRunning()) {
            return;
        }
        countdownScaleView.scaleUpText();
        resetAnimation.cancel();
        startAnimation.start();
    }

    @OnClick(R.id.consumables_countdown_timer_reset)
    public void onResetClick() {
        reset();
    }

    protected abstract String getDefaultText();

    protected abstract ConsumablesTimer getTimer();
}
