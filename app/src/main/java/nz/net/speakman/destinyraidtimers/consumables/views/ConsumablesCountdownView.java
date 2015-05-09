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
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
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

    // TODO Put in resources?...
    protected static final int RESET_ANIMATION_DURATION = 750;

    public static String formatMinutesFromMillis(long millis) {
        return String.format("%d:%02d", TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis % (1000 * 60)));
    }

    @InjectView(R.id.consumables_countdown_label)
    TextView countdown;

    @InjectView(R.id.consumables_countdown_image)
    ImageView progressView;

    @Inject
    Bus bus;

    ObjectAnimator animator;
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
        View target = this;
        View source = inflate(ctx, R.layout.consumables_countdown, this);
        ButterKnife.inject(target, source);
        RaidApplication.getApplication().inject(this);
        Resources resources = ctx.getResources();
        progressDrawable = new CircularProgressDrawable.Builder()
                .setRingColor(resources.getColor(R.color.consumables_accent))
                .setRingWidth(resources.getDimensionPixelSize(R.dimen.consumables_progress_width))
                .create();
        progressDrawable.setProgress(1f);
        progressView.setImageDrawable(progressDrawable);
        bus.register(this);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        Bundle outState = new Bundle();
        outState.putParcelable(KEY_SUPER_STATE, superState);
        outState.putFloat(KEY_COUNTDOWN_PROGRESS, progressDrawable.getProgress());
        outState.putString(KEY_COUNTDOWN_LABEL, countdown.getText().toString());
        return outState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            float progress = ((Bundle) state).getFloat(KEY_COUNTDOWN_PROGRESS, 1f);
            progressDrawable.setProgress(progress);
            String text = ((Bundle)state).getString(KEY_COUNTDOWN_LABEL, getDefaultText());
            countdown.setText(text);
            state = ((Bundle) state).getParcelable(KEY_SUPER_STATE);
        }
        super.onRestoreInstanceState(state);
    }

    public void reset() {
        resetProgressBar();
        countdown.setText(getDefaultText());
    }

    protected void onTimerUpdated(long timeRemainingMs, long totalTimeMs) {
        if (timeRemainingMs == 0) {
            countdown.setText(getDefaultText());
            resetProgressBar();
        } else {
            countdown.setText(formatMinutesFromMillis(timeRemainingMs));
        }
        if (animator == null) {
            float progressPct = timeRemainingMs / (float) totalTimeMs;
            startAnimator(progressPct, timeRemainingMs);
        }
    }

    protected void resetProgressBar() {
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

    protected void startAnimator(float progressPct, long duration) {
        // Drawable goes backwards, so we count down from 1 -> 0
        progressDrawable.setProgress(progressPct);
        animator = ObjectAnimator.ofFloat(progressDrawable, CircularProgressDrawable.PROGRESS_PROPERTY,
                progressPct, 0f);
        animator.setDuration(duration);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }

    @OnClick(R.id.consumables_countdown_image)
    public void onCountdownClick() {
        ConsumablesTimer timer = getTimer();
        if (timer.isRunning()) {
            timer.reset();
            this.reset();
        } else {
            timer.start();
        }
    }

    protected abstract String getDefaultText();

    protected abstract ConsumablesTimer getTimer();
}
