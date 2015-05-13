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
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import nz.net.speakman.destinyraidtimers.R;

/**
 * Created by Adam on 15-05-12.
 */
public class CountdownScaleView extends FrameLayout {

    @InjectView(R.id.countdown_label)
    TextView countdownText;

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

    protected void init(Context ctx) {
        inflate(ctx, R.layout.countdown_scale, this);
        ButterKnife.inject(this);
        initAnimationsIfNeeded();
    }
    private void initAnimationsIfNeeded() {
        if (scaleUpTextAnimation != null && scaleDownTextAnimation != null) return;

        // TODO This should be in the XML
        float ICON_MIN_SCALE = 0.2f;
        float ICON_MAX_SCALE = 0.4f;
        float TEXT_MIN_SCALE = 0.2f;
        float TEXT_MAX_SCALE = 0.4f;

        scaleUpTextAnimation = new AnimatorSet().setDuration(500);
        scaleDownTextAnimation = new AnimatorSet().setDuration(500);

        DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();

        ValueAnimator.AnimatorUpdateListener iconScaleUpdater = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float val = (Float) animation.getAnimatedValue();
                float weight = val.floatValue();
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) countdownIcon.getLayoutParams();
                lp.weight = weight;
                countdownIcon.setLayoutParams(lp);
            }
        };
        ValueAnimator scaleUpIconAnimator = ValueAnimator.ofFloat(ICON_MIN_SCALE, ICON_MAX_SCALE);
        scaleUpIconAnimator.addUpdateListener(iconScaleUpdater);
        scaleUpIconAnimator.setInterpolator(decelerateInterpolator);
        ValueAnimator scaleDownIconAnimator = ValueAnimator.ofFloat(ICON_MAX_SCALE, ICON_MIN_SCALE);
        scaleDownIconAnimator.addUpdateListener(iconScaleUpdater);
        scaleDownIconAnimator.setInterpolator(decelerateInterpolator);



        ValueAnimator.AnimatorUpdateListener textScaleUpdater = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float val = (Float) animation.getAnimatedValue();
                float weight = val.floatValue();
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) countdownText.getLayoutParams();
                lp.weight = weight;
                countdownText.setLayoutParams(lp);
            }
        };

        ValueAnimator scaleUpTextAnimator = ValueAnimator.ofFloat(TEXT_MIN_SCALE, TEXT_MAX_SCALE);
        scaleUpTextAnimator.addUpdateListener(textScaleUpdater);
        scaleUpTextAnimator.setInterpolator(decelerateInterpolator);
        ValueAnimator scaleDownTextAnimator = ValueAnimator.ofFloat(TEXT_MAX_SCALE, TEXT_MIN_SCALE);
        scaleDownTextAnimator.addUpdateListener(textScaleUpdater);
        scaleDownTextAnimator.setInterpolator(decelerateInterpolator);

        scaleUpTextAnimation.play(scaleUpTextAnimator).with(scaleDownIconAnimator);
        scaleDownTextAnimation.play(scaleDownTextAnimator).with(scaleUpIconAnimator);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minSide = Math.min(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(minSide, minSide);
    }

    public void setText(CharSequence text) {
        countdownText.setText(text);
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
