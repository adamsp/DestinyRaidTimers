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

package nz.net.speakman.destinyraidtimers.crota.views;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sefford.circularprogressdrawable.CircularProgressDrawable;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import nz.net.speakman.destinyraidtimers.AnimatorEndListener;
import nz.net.speakman.destinyraidtimers.R;
import nz.net.speakman.destinyraidtimers.RaidApplication;
import nz.net.speakman.destinyraidtimers.crota.CrotaEnrageTimerUpdateEvent;
import nz.net.speakman.destinyraidtimers.crota.CrotaMovementTimer;
import nz.net.speakman.destinyraidtimers.crota.CrotaMovementTimerUpdateEvent;

/**
 * Created by Adam on 15-02-21.
 */
public class CrotaMovementCountdownView extends RelativeLayout {

    private static final String KEY_SUPER_STATE = "nz.net.speakman.destinyraidtimers.crota.views.CrotaMovementCountdownView.KEY_SUPER_STATE";
    private static final String KEY_MOVEMENT_PROGRESS = "nz.net.speakman.destinyraidtimers.crota.views.CrotaMovementCountdownView.KEY_MOVEMENT_PROGRESS";

    // TODO Put in resources?...
    private static final int RESET_ANIMATION_DURATION = 750;

    @InjectView(R.id.crota_movement_countdown_label)
    TextView countdown;

    @InjectView(R.id.crota_movement_countdown_image)
    ImageView progressView;

    @Inject
    Bus bus;

    private ObjectAnimator animator;
    private CircularProgressDrawable progressDrawable;

    public CrotaMovementCountdownView(Context context) {
        super(context);
        init(context);
    }

    public CrotaMovementCountdownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CrotaMovementCountdownView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CrotaMovementCountdownView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context ctx) {
        inflate(ctx, R.layout.crota_movement_coundown, this);
        ButterKnife.inject(this);
        RaidApplication.getApplication().inject(this);
        Resources resources = ctx.getResources();
        progressDrawable = new CircularProgressDrawable.Builder()
                .setRingColor(resources.getColor(R.color.crota_accent))
                .setRingWidth(resources.getDimensionPixelSize(R.dimen.crota_progress_width))
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
        outState.putFloat(KEY_MOVEMENT_PROGRESS, progressDrawable.getProgress());
        return outState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            progressDrawable.setProgress(((Bundle) state).getFloat(KEY_MOVEMENT_PROGRESS, 1f));
            state = ((Bundle) state).getParcelable(KEY_SUPER_STATE);
        }
        super.onRestoreInstanceState(state);
    }

    @Subscribe
    public void onCrotaMovementUpdate(CrotaMovementTimerUpdateEvent event) {
        long timeToMoveMs = event.getMillisUntilMove();
        countdown.setText(String.valueOf((timeToMoveMs + 999) / 1000));
        if (timeToMoveMs == 0) {
            resetProgressBar();
        }
        if (animator == null) {
            float progressPct = timeToMoveMs / (float) CrotaMovementTimer.MOVEMENT_PERIOD_MS;
            // Drawable goes backwards, so we count down from 1 -> 0
            progressDrawable.setProgress(progressPct);
            animator = ObjectAnimator.ofFloat(progressDrawable, CircularProgressDrawable.PROGRESS_PROPERTY,
                    progressPct, 0f);
            animator.setDuration(timeToMoveMs);
            animator.setInterpolator(new LinearInterpolator());
            animator.start();
        }
    }

    @Subscribe
    public void onEnrageUpdate(CrotaEnrageTimerUpdateEvent event) {
        if (event.isEnraged()) {
            onEnrage();
        }
    }

    public void onEnrage() {
        resetProgressBar();
        countdown.setText("");
    }

    public void reset() {
        resetProgressBar();
        countdown.setText("");
    }

    private void resetProgressBar() {
        ObjectAnimator resetAnimator = ObjectAnimator.ofFloat(progressDrawable, CircularProgressDrawable.PROGRESS_PROPERTY,
                progressDrawable.getProgress(), 1f);
        resetAnimator.setDuration(RESET_ANIMATION_DURATION);
        resetAnimator.addListener(new AnimatorEndListener() {
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
}
