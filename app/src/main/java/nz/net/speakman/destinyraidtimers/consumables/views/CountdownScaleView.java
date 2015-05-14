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
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import butterknife.ButterKnife;
import butterknife.InjectView;
import nz.net.speakman.destinyraidtimers.R;

/**
 * Created by Adam on 15-05-12.
 */
public class CountdownScaleView extends FrameLayout {

    final float MIN_WEIGHT = 0.2f;
    final float MAX_WEIGHT = 0.4f;

    @InjectView(R.id.countdown_label_container)
    FrameLayout countdownLabelContainer;

    @InjectView(R.id.countdown_label)
    AutoResizeTextView countdownLabel;

    @InjectView(R.id.countdown_icon)
    ImageView countdownIcon;

    private AnimatorSet scaleUpTextAnimation;
    private AnimatorSet scaleDownTextAnimation;

    public CountdownScaleView(Context context) {
        super(context);
        init(context);
    }

    public CountdownScaleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CountdownScaleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CountdownScaleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context ctx) {
        inflate(ctx, R.layout.countdown_scale, this);
        ButterKnife.inject(this);
        initAnimations();
        countdownLabel.setEnableSizeCache(true);
        addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                // We only run this once - we need to size the text after the views have been weighted.
                removeOnLayoutChangeListener(this);
                resizeText();
            }
        });
        updateWeights(MIN_WEIGHT, MAX_WEIGHT);
    }

    private void initAnimations() {
        scaleUpTextAnimation = new AnimatorSet().setDuration(500);
        scaleDownTextAnimation = new AnimatorSet().setDuration(500);

        DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();

        ValueAnimator.AnimatorUpdateListener textScaleUpdater = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float val = (Float) animation.getAnimatedValue();
                float textWeight = val;
                float iconWeight = MAX_WEIGHT - (textWeight - MIN_WEIGHT);
                updateWeights(textWeight, iconWeight);
                resizeText();
            }
        };

        ValueAnimator scaleUpTextAnimator = ValueAnimator.ofFloat(MIN_WEIGHT, MAX_WEIGHT);
        scaleUpTextAnimator.addUpdateListener(textScaleUpdater);
        scaleUpTextAnimator.setInterpolator(decelerateInterpolator);
        ValueAnimator scaleDownTextAnimator = ValueAnimator.ofFloat(MAX_WEIGHT, MIN_WEIGHT);
        scaleDownTextAnimator.addUpdateListener(textScaleUpdater);
        scaleDownTextAnimator.setInterpolator(decelerateInterpolator);

        scaleUpTextAnimation.play(scaleUpTextAnimator);
        scaleDownTextAnimation.play(scaleDownTextAnimator);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minSide = Math.min(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(minSide, minSide);
    }

    private void updateWeights(float textWeight, float iconWeight) {
        LinearLayout.LayoutParams textLP = (LinearLayout.LayoutParams) countdownLabelContainer.getLayoutParams();
        textLP.weight = textWeight;
        countdownLabelContainer.setLayoutParams(textLP);

        LinearLayout.LayoutParams iconLP = (LinearLayout.LayoutParams) countdownIcon.getLayoutParams();
        iconLP.weight = iconWeight;
        countdownIcon.setLayoutParams(iconLP);
    }

    private void resizeText() {
        int maxHeight = countdownLabelContainer.getHeight();
        countdownLabel.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, maxHeight, getResources().getDisplayMetrics()));
    }

    public void setText(CharSequence text) {
        countdownLabel.setText(text);
        resizeText();
    }

    public void scaleUpText() {
        scaleDownTextAnimation.cancel();
        scaleUpTextAnimation.start();
    }

    public void scaleDownText() {
        scaleUpTextAnimation.cancel();;
        scaleDownTextAnimation.start();
    }

    public void addScaleUpAnimationLister(Animator.AnimatorListener listener) {
        scaleUpTextAnimation.addListener(listener);
    }

    public void addScaleDownAnimationListener(Animator.AnimatorListener listener) {
        scaleDownTextAnimation.addListener(listener);
    }
}
