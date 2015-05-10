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
import android.animation.PropertyValuesHolder;
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
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

    private static final float ICON_MIN_SCALE = 0.5f;
    private static final float ICON_MAX_SCALE = 1.0f;
    private static final float TEXT_MIN_SCALE = 1.0f;
    private static final float TEXT_MAX_SCALE = 3.5f;

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

    @InjectView(R.id.consumables_countdown_label)
    TextView countdownText;

    @InjectView(R.id.consumables_countdown_image)
    ImageView progressView;

    @InjectView(R.id.consumables_countdown_icon)
    ImageView consumableIcon;

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
        consumableIcon.setImageResource(getConsumableIconResource());
        countdownText.setText(getDefaultText());
        bus.register(this);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        // Have to set animations up after first layout pass, since we need to figure out the
        // pivot points for the icon & text.
        initAnimationsIfNeeded();
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

        consumableIcon.setPivotX(consumableIcon.getWidth() / 2);
        consumableIcon.setPivotY(0);
        ObjectAnimator scaleUpIconAnimator = ObjectAnimator.ofPropertyValuesHolder(consumableIcon,
                PropertyValuesHolder.ofFloat("scaleX", ICON_MIN_SCALE, ICON_MAX_SCALE),
                PropertyValuesHolder.ofFloat("scaleY", ICON_MIN_SCALE, ICON_MAX_SCALE));
        scaleUpIconAnimator.setInterpolator(decelerateInterpolator);
        scaleUpIconAnimator.addListener(new AnimationEndListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                countdownText.setText(getDefaultText());
            }
        });
        ObjectAnimator scaleDownIconAnimator = ObjectAnimator.ofPropertyValuesHolder(consumableIcon,
                PropertyValuesHolder.ofFloat("scaleX", ICON_MAX_SCALE, ICON_MIN_SCALE),
                PropertyValuesHolder.ofFloat("scaleY", ICON_MAX_SCALE, ICON_MIN_SCALE));
        scaleDownIconAnimator.setInterpolator(decelerateInterpolator);


        countdownText.setPivotX(countdownText.getWidth() / 2);
        countdownText.setPivotY(countdownText.getHeight());
        ObjectAnimator scaleUpTextAnimator = ObjectAnimator.ofPropertyValuesHolder(countdownText,
                PropertyValuesHolder.ofFloat("scaleX", TEXT_MIN_SCALE, TEXT_MAX_SCALE),
                PropertyValuesHolder.ofFloat("scaleY", TEXT_MIN_SCALE, TEXT_MAX_SCALE));
        scaleUpTextAnimator.setInterpolator(decelerateInterpolator);
        scaleUpTextAnimator.addListener(new AnimationEndListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ConsumablesTimer timer = getTimer();
                if (!timer.isRunning()) {
                    timer.start();
                }
            }
        });
        ObjectAnimator scaleDownTextAnimator = ObjectAnimator.ofPropertyValuesHolder(countdownText,
                PropertyValuesHolder.ofFloat("scaleX", TEXT_MAX_SCALE, TEXT_MIN_SCALE),
                PropertyValuesHolder.ofFloat("scaleY", TEXT_MAX_SCALE, TEXT_MIN_SCALE));
        scaleDownTextAnimator.setInterpolator(decelerateInterpolator);

        startAnimation.play(resetShowRotationAnimator).with(resetShowTransitionAnimator)
                .with(scaleUpTextAnimator)
                .with(scaleDownIconAnimator);
        resetAnimation.play(resetHideRotationAnimator).with(resetHideTransitionAnimator)
                .with(scaleDownTextAnimator)
                .with(scaleUpIconAnimator);

        // If we've already got a timer running, we should put everything in its "timer running" state/placement.
        if (getTimer().isRunning()) {
            resetButton.setVisibility(View.VISIBLE);
            consumableIcon.setScaleX(ICON_MIN_SCALE);
            consumableIcon.setScaleY(ICON_MIN_SCALE);
            countdownText.setScaleX(TEXT_MAX_SCALE);
            countdownText.setScaleY(TEXT_MAX_SCALE);
        }
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
        resetProgressBar();
        startAnimation.cancel();
        resetAnimation.start();
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

    @OnClick(R.id.consumables_countdown_image)
    public void onCountdownClick() {
        if (getTimer().isRunning()) {
            return;
        }
        resetAnimation.cancel();
        startAnimation.start();
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
