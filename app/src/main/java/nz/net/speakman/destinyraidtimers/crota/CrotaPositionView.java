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

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import nz.net.speakman.destinyraidtimers.R;

/**
 * Created by Adam on 15-02-19.
 */
public class CrotaPositionView extends LinearLayout {

    private static final String KEY_CURRENT_POSITION = "nz.net.speakman.destinyraidtimers.crota.CrotaPositionView.KEY_CURRENT_POSITION";
    private static final String KEY_SUPER_STATE = "nz.net.speakman.destinyraidtimers.crota.CrotaPositionView.KEY_SUPER_STATE";

    public static final int POSITION_ENRAGED = -2;
    public static final int POSITION_RESET = -1;
    /**
     * Center, going left.
     */
    public static final int POSITION_CENTER_L = 0;
    public static final int POSITION_LEFT = 1;
    /**
     * Center, going right.
     */
    public static final int POSITION_CENTER_R = 2;
    public static final int POSITION_RIGHT = 3;

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

    private int currentPosition = POSITION_RESET;

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
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        Bundle outState = new Bundle();
        outState.putParcelable(KEY_SUPER_STATE, superState);
        outState.putInt(KEY_CURRENT_POSITION, currentPosition);
        return outState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            int position = ((Bundle)state).getInt(KEY_CURRENT_POSITION, POSITION_CENTER_L);
            showPosition(position);
            state = ((Bundle) state).getParcelable(KEY_SUPER_STATE);
        }
        super.onRestoreInstanceState(state);
    }

    public void showNextPosition() {
        showPosition((currentPosition + 1) % 4);
    }

    public void showEnraged() {
        showPosition(POSITION_ENRAGED);
    }

    public void reset() {
        showPosition(POSITION_RESET);
    }

    private static final float POSITION_ALPHA_ENABLED = 1f;
    private static final float POSITION_ALPHA_DISABLED = 0.25f;
    private static final float POSITION_ALPHA_NEXT = 0.85f;

    private void showPosition(int position) {
        // Portrait view
        if (positionImageCurrent == null) {
            int leftPositionDrawable = R.drawable.crota_position_left_disabled;
            int centerPositionDrawable = R.drawable.crota_position_center_disabled;
            int rightPositionDrawable = R.drawable.crota_position_right_disabled;
            switch (position) {
                case POSITION_ENRAGED:
                    /// TODO Show different enrage image(s)?
                case POSITION_CENTER_L:
                    positionImageLeft.setAlpha(POSITION_ALPHA_NEXT);
                    positionImageCenter.setAlpha(POSITION_ALPHA_ENABLED);
                    positionImageRight.setAlpha(POSITION_ALPHA_DISABLED);
                    centerPositionDrawable = R.drawable.crota_position_center;
                    break;
                case POSITION_CENTER_R:
                    positionImageLeft.setAlpha(POSITION_ALPHA_DISABLED);
                    positionImageCenter.setAlpha(POSITION_ALPHA_ENABLED);
                    positionImageRight.setAlpha(POSITION_ALPHA_NEXT);
                    centerPositionDrawable = R.drawable.crota_position_center;
                    break;
                case POSITION_LEFT:
                    positionImageLeft.setAlpha(POSITION_ALPHA_ENABLED);
                    positionImageCenter.setAlpha(POSITION_ALPHA_NEXT);
                    positionImageRight.setAlpha(POSITION_ALPHA_DISABLED);
                    leftPositionDrawable = R.drawable.crota_position_left;
                    break;
                case POSITION_RIGHT:
                    positionImageLeft.setAlpha(POSITION_ALPHA_DISABLED);
                    positionImageCenter.setAlpha(POSITION_ALPHA_NEXT);
                    positionImageRight.setAlpha(POSITION_ALPHA_ENABLED);
                    rightPositionDrawable = R.drawable.crota_position_right;
                    break;
                case POSITION_RESET:
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
                case POSITION_ENRAGED:
                case POSITION_CENTER_L:
                    currentPositionDrawable = R.drawable.crota_position_center;
                    nextPositionDrawable = R.drawable.crota_position_left_disabled;
                    break;
                case POSITION_CENTER_R:
                    currentPositionDrawable = R.drawable.crota_position_center;
                    nextPositionDrawable = R.drawable.crota_position_right_disabled;
                    break;
                case POSITION_LEFT:
                    currentPositionDrawable = R.drawable.crota_position_left;
                    nextPositionDrawable = R.drawable.crota_position_center_disabled;
                    break;
                case POSITION_RIGHT:
                    currentPositionDrawable = R.drawable.crota_position_right;
                    nextPositionDrawable = R.drawable.crota_position_center_disabled;
                    break;
                case POSITION_RESET:
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
