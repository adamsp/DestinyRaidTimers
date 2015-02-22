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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import nz.net.speakman.destinyraidtimers.R;
import nz.net.speakman.destinyraidtimers.RaidApplication;
import nz.net.speakman.destinyraidtimers.crota.CrotaEnrageTimerUpdateEvent;
import nz.net.speakman.destinyraidtimers.crota.CrotaMovementTimerUpdateEvent;
import nz.net.speakman.destinyraidtimers.crota.CrotaPosition;

/**
 * Created by Adam on 15-02-19.
 */
public class CrotaPositionView extends LinearLayout {

    private static final String KEY_CURRENT_POSITION = "nz.net.speakman.destinyraidtimers.crota.views.CrotaPositionView.KEY_CURRENT_POSITION";
    private static final String KEY_SUPER_STATE = "nz.net.speakman.destinyraidtimers.crota.views.CrotaPositionView.KEY_SUPER_STATE";

    @Optional
    @InjectView(R.id.crota_position_current)
    ImageView positionImageCurrent;

    @Optional
    @InjectView(R.id.crota_position_next)
    ImageView positionImageNext;

    @Optional
    @InjectView(R.id.crota_position_left)
    ImageView positionImageLeft;

    @Optional
    @InjectView(R.id.crota_position_center)
    ImageView positionImageCenter;

    @Optional
    @InjectView(R.id.crota_position_right)
    ImageView positionImageRight;

    @Inject
    Bus bus;

    private ColorFilter enrageFilter;
    private ColorFilter currentPositionFilter;
    private ColorFilter disabledPositionFilter;
    private float positionAlphaEnabled;
    private float positionAlphaDisabled;
    private float positionAlphaNext;

    private CrotaPosition currentPosition = CrotaPosition.RESET;

    public CrotaPositionView(Context context) {
        super(context);
        init(context);
    }

    public CrotaPositionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CrotaPositionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CrotaPositionView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context ctx) {
        inflate(ctx, R.layout.crota_position, this);
        ButterKnife.inject(this);
        RaidApplication.getApplication().inject(this);
        bus.register(this);
        Resources resources = getResources();
        enrageFilter = new PorterDuffColorFilter(resources.getColor(R.color.crota_position_enrage), PorterDuff.Mode.MULTIPLY);
        currentPositionFilter = new PorterDuffColorFilter(resources.getColor(R.color.crota_position_current), PorterDuff.Mode.MULTIPLY);
        disabledPositionFilter = new PorterDuffColorFilter(resources.getColor(R.color.crota_position_disabled), PorterDuff.Mode.MULTIPLY);
        /*
        See http://stackoverflow.com/a/8780360/1217087 and
        http://blog.danlew.net/2015/01/06/handling-android-resources-with-non-standard-formats/
        for an explanation on why this is necessary.
         */
        TypedValue outValue = new TypedValue();
        resources.getValue(R.dimen.crota_position_alpha_enabled, outValue, true);
        positionAlphaEnabled = outValue.getFloat();
        resources.getValue(R.dimen.crota_position_alpha_disabled, outValue, true);
        positionAlphaDisabled = outValue.getFloat();
        resources.getValue(R.dimen.crota_position_alpha_next, outValue, true);
        positionAlphaNext = outValue.getFloat();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        Bundle outState = new Bundle();
        outState.putParcelable(KEY_SUPER_STATE, superState);
        outState.putInt(KEY_CURRENT_POSITION, currentPosition.getCode());
        return outState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            int position = ((Bundle)state).getInt(KEY_CURRENT_POSITION, CrotaPosition.RESET.getCode());
            showPosition(CrotaPosition.get(position));
            state = ((Bundle) state).getParcelable(KEY_SUPER_STATE);
        }
        super.onRestoreInstanceState(state);
    }

    @Subscribe
    public void onCrotaMovementUpdate(CrotaMovementTimerUpdateEvent event) {
        showPosition(event.getPosition());
    }

    @Subscribe
    public void onEnrageUpdate(CrotaEnrageTimerUpdateEvent event) {
        if (event.isEnraged()) {
            onEnrage();
        }
    }

    public void onEnrage() {
        showPosition(CrotaPosition.ENRAGE);
    }

    public void reset() {
        showPosition(CrotaPosition.RESET);
    }



    private void showPosition(CrotaPosition position) {
        if (position == currentPosition) {
            return;
        }

        // Portrait view
        if (positionImageCurrent == null) {
            ColorFilter leftPositionFilterColor = disabledPositionFilter;
            ColorFilter centerPositionFilterColor = disabledPositionFilter;
            ColorFilter rightPositionFilterColor = disabledPositionFilter;
            int leftPositionDrawable = R.drawable.crota_position_left_disabled;
            int centerPositionDrawable = R.drawable.crota_position_center_disabled;
            int rightPositionDrawable = R.drawable.crota_position_right_disabled;
            switch (position) {
                case ENRAGE:
                    positionImageLeft.setAlpha(positionAlphaEnabled);
                    positionImageCenter.setAlpha(positionAlphaEnabled);
                    positionImageRight.setAlpha(positionAlphaEnabled);
                    leftPositionFilterColor = centerPositionFilterColor = rightPositionFilterColor = enrageFilter;
                    leftPositionDrawable = R.drawable.crota_position_left;
                    centerPositionDrawable = R.drawable.crota_position_center;
                    rightPositionDrawable = R.drawable.crota_position_right;
                    break;
                case CENTER_L:
                    positionImageLeft.setAlpha(positionAlphaNext);
                    positionImageCenter.setAlpha(positionAlphaEnabled);
                    positionImageRight.setAlpha(positionAlphaDisabled);
                    centerPositionDrawable = R.drawable.crota_position_center;
                    centerPositionFilterColor = currentPositionFilter;
                    break;
                case CENTER_R:
                    positionImageLeft.setAlpha(positionAlphaDisabled);
                    positionImageCenter.setAlpha(positionAlphaEnabled);
                    positionImageRight.setAlpha(positionAlphaNext);
                    centerPositionDrawable = R.drawable.crota_position_center;
                    centerPositionFilterColor = currentPositionFilter;
                    break;
                case LEFT:
                    positionImageLeft.setAlpha(positionAlphaEnabled);
                    positionImageCenter.setAlpha(positionAlphaNext);
                    positionImageRight.setAlpha(positionAlphaDisabled);
                    leftPositionDrawable = R.drawable.crota_position_left;
                    leftPositionFilterColor = currentPositionFilter;
                    break;
                case RIGHT:
                    positionImageLeft.setAlpha(positionAlphaDisabled);
                    positionImageCenter.setAlpha(positionAlphaNext);
                    positionImageRight.setAlpha(positionAlphaEnabled);
                    rightPositionDrawable = R.drawable.crota_position_right;
                    rightPositionFilterColor = currentPositionFilter;
                    break;
                case RESET:
                    positionImageLeft.setAlpha(positionAlphaDisabled);
                    positionImageCenter.setAlpha(positionAlphaDisabled);
                    positionImageRight.setAlpha(positionAlphaDisabled);
                    break;
                default:
                    throw new IllegalStateException(String.format("Invalid Crota position supplied (%s)", position));
            }
            positionImageLeft.setColorFilter(leftPositionFilterColor);
            positionImageLeft.setImageResource(leftPositionDrawable);
            positionImageCenter.setColorFilter(centerPositionFilterColor);
            positionImageCenter.setImageResource(centerPositionDrawable);
            positionImageRight.setColorFilter(rightPositionFilterColor);
            positionImageRight.setImageResource(rightPositionDrawable);
        } else { // Landscape
            float currentPositionAlpha = positionAlphaEnabled;
            float nextPositionAlpha = positionAlphaNext;
            int currentPositionDrawable;
            int nextPositionDrawable;
            ColorFilter currentPositionFilter = this.currentPositionFilter;
            ColorFilter nextPositionFilter = disabledPositionFilter;
            switch (position) {
                case ENRAGE:
                    currentPositionDrawable = R.drawable.crota_position_center;
                    nextPositionDrawable = R.drawable.crota_position_center;
                    currentPositionFilter = enrageFilter;
                    nextPositionFilter = enrageFilter;
                    nextPositionAlpha = positionAlphaEnabled;
                    break;
                case CENTER_L:
                    currentPositionDrawable = R.drawable.crota_position_center;
                    nextPositionDrawable = R.drawable.crota_position_left_disabled;
                    break;
                case CENTER_R:
                    currentPositionDrawable = R.drawable.crota_position_center;
                    nextPositionDrawable = R.drawable.crota_position_right_disabled;
                    break;
                case LEFT:
                    currentPositionDrawable = R.drawable.crota_position_left;
                    nextPositionDrawable = R.drawable.crota_position_center_disabled;
                    break;
                case RIGHT:
                    currentPositionDrawable = R.drawable.crota_position_right;
                    nextPositionDrawable = R.drawable.crota_position_center_disabled;
                    break;
                case RESET:
                    currentPositionDrawable = R.drawable.crota_position_center_disabled;
                    nextPositionDrawable = R.drawable.crota_position_left_disabled;
                    currentPositionFilter = disabledPositionFilter;
                    currentPositionAlpha = positionAlphaDisabled;
                    nextPositionAlpha = positionAlphaDisabled;
                    break;
                default:
                    throw new IllegalStateException(String.format("Invalid Crota position supplied (%s)", position));
            }
            positionImageCurrent.setAlpha(currentPositionAlpha);
            positionImageCurrent.setColorFilter(currentPositionFilter);
            positionImageCurrent.setImageResource(currentPositionDrawable);
            positionImageNext.setAlpha(nextPositionAlpha);
            positionImageNext.setColorFilter(nextPositionFilter);
            positionImageNext.setImageResource(nextPositionDrawable);
        }
        currentPosition = position;
    }
}
