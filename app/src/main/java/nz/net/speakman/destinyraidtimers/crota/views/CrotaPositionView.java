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
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
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

    private static final float POSITION_ALPHA_ENABLED = 1f;
    private static final float POSITION_ALPHA_DISABLED = 0.25f;
    private static final float POSITION_ALPHA_NEXT = 0.85f;

    private void showPosition(CrotaPosition position) {
        if (position == currentPosition) {
            return;
        }
        // Portrait view
        if (positionImageCurrent == null) {
            int leftPositionDrawable = R.drawable.crota_position_left_disabled;
            int centerPositionDrawable = R.drawable.crota_position_center_disabled;
            int rightPositionDrawable = R.drawable.crota_position_right_disabled;
            switch (position) {
                case ENRAGE:
                    /// TODO Show different enrage image(s)?
                case CENTER_L:
                    positionImageLeft.setAlpha(POSITION_ALPHA_NEXT);
                    positionImageCenter.setAlpha(POSITION_ALPHA_ENABLED);
                    positionImageRight.setAlpha(POSITION_ALPHA_DISABLED);
                    centerPositionDrawable = R.drawable.crota_position_center;
                    break;
                case CENTER_R:
                    positionImageLeft.setAlpha(POSITION_ALPHA_DISABLED);
                    positionImageCenter.setAlpha(POSITION_ALPHA_ENABLED);
                    positionImageRight.setAlpha(POSITION_ALPHA_NEXT);
                    centerPositionDrawable = R.drawable.crota_position_center;
                    break;
                case LEFT:
                    positionImageLeft.setAlpha(POSITION_ALPHA_ENABLED);
                    positionImageCenter.setAlpha(POSITION_ALPHA_NEXT);
                    positionImageRight.setAlpha(POSITION_ALPHA_DISABLED);
                    leftPositionDrawable = R.drawable.crota_position_left;
                    break;
                case RIGHT:
                    positionImageLeft.setAlpha(POSITION_ALPHA_DISABLED);
                    positionImageCenter.setAlpha(POSITION_ALPHA_NEXT);
                    positionImageRight.setAlpha(POSITION_ALPHA_ENABLED);
                    rightPositionDrawable = R.drawable.crota_position_right;
                    break;
                case RESET:
                    positionImageLeft.setAlpha(POSITION_ALPHA_NEXT);
                    positionImageCenter.setAlpha(POSITION_ALPHA_NEXT);
                    positionImageRight.setAlpha(POSITION_ALPHA_NEXT);
                    break;
                default:
                    throw new IllegalStateException(String.format("Invalid Crota position supplied (%s)", position));
            }
            positionImageLeft.setImageResource(leftPositionDrawable);
            positionImageCenter.setImageResource(centerPositionDrawable);
            positionImageRight.setImageResource(rightPositionDrawable);
        } else { // Landscape
            float currentPositionAlpha = POSITION_ALPHA_ENABLED;
            int currentPositionDrawable;
            int nextPositionDrawable;
            switch (position) {
                case ENRAGE:
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
                    currentPositionAlpha = POSITION_ALPHA_NEXT;
                    break;
                default:
                    throw new IllegalStateException(String.format("Invalid Crota position supplied (%s)", position));
            }
            positionImageCurrent.setAlpha(currentPositionAlpha);
            positionImageCurrent.setImageResource(currentPositionDrawable);
            positionImageNext.setImageResource(nextPositionDrawable);
        }
        currentPosition = position;
    }
}
