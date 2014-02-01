package com.iwobanas.screenrecorder;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;

public class RecorderOverlay extends AbstractScreenOverlay {
    private static final String TAG = "scr_RecorderOverlay";
    private static final String RECORDER_OVERLAY_POSITION_X = "RECORDER_OVERLAY_POSITION_X";
    private static final String RECORDER_OVERLAY_POSITION_Y = "RECORDER_OVERLAY_POSITION_Y";
    private static final String RECORDER_OVERLAY_GRAVITY = "RECORDER_OVERLAY_GRAVITY";
    private static final String SCR_UI_PREFERENCES = "scr_ui";
    private IRecorderService mService;

    private ImageButton mSettingsButton;
    private WindowManager.LayoutParams layoutParams;

    public RecorderOverlay(Context context, IRecorderService service) {
        super(context);
        mService = service;
    }

    public void highlightPosition() {
        int x = layoutParams.x;
        float delta = Utils.dipToPixels(getContext(), 20f);
        ValueAnimator animator = ValueAnimator.ofInt(x, x + (int)delta, x - (int) (delta * 0.6f), x + (int) (delta * 0.3f), x);
        animator.setDuration(750);
        animator.setInterpolator(new DecelerateInterpolator(2f));
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (Integer) animation.getAnimatedValue();
                layoutParams.x = value;
                updateLayoutParams();
            }
        });
        animator.start();
    }

    @Override
    protected View createView() {
        View view = getLayoutInflater().inflate(R.layout.recorder, null);

        ImageButton startButton = (ImageButton) view.findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.startRecording();
            }
        });

        WindowDragListener dragListener = new WindowDragListener(getLayoutParams());
        dragListener.setDragStartListener(new WindowDragListener.OnWindowDragStartListener() {
            @Override
            public void onDragStart() {
                getView().setBackgroundResource(R.drawable.bg_h);
            }
        });
        dragListener.setDragEndListener(new WindowDragListener.OnWindowDragEndListener() {
            @Override
            public void onDragEnd() {
                getView().setBackgroundResource(R.drawable.bg);
            }
        });
        view.setOnTouchListener(dragListener);

        mSettingsButton = (ImageButton) view.findViewById(R.id.settings_button);
        mSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mService.showSettings();
            }
        });

        ImageButton closeButton = (ImageButton) view.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mService.close();
            }
        });
        return view;
    }

    @Override
    protected WindowManager.LayoutParams getLayoutParams() {
        if (layoutParams == null) {
            layoutParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            );
            layoutParams.format = PixelFormat.TRANSLUCENT;
            layoutParams.setTitle(getContext().getString(R.string.app_name));
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

            SharedPreferences preferences = getContext().getSharedPreferences(SCR_UI_PREFERENCES, Context.MODE_PRIVATE);
            layoutParams.x = preferences.getInt(RECORDER_OVERLAY_POSITION_X, 0);
            layoutParams.y = preferences.getInt(RECORDER_OVERLAY_POSITION_Y, 0);
            layoutParams.gravity = preferences.getInt(RECORDER_OVERLAY_GRAVITY, Gravity.CENTER);
            Log.v(TAG, "Initializing window position to " + layoutParams.x + ":" + layoutParams.y);
        }
        return layoutParams;
    }

    @Override
    public void onDestroy() {
        if (layoutParams != null) {
            SharedPreferences preferences = getContext().getSharedPreferences(SCR_UI_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(RECORDER_OVERLAY_POSITION_X, layoutParams.x);
            editor.putInt(RECORDER_OVERLAY_POSITION_Y, layoutParams.y);
            editor.putInt(RECORDER_OVERLAY_GRAVITY, layoutParams.gravity);
            editor.commit();
        }
    }

    @Override
    public void show() {
        if (!isVisible()) {
            getLayoutParams().windowAnimations = 0;
        }
        super.show();
    }

    public void animateShow() {
        if (!isVisible()) {
            getLayoutParams().windowAnimations = android.R.style.Animation_Translucent;
        }
        super.show();
    }

    @Override
    public void hide() {
        setHideAnimation(0);
        super.hide();
    }

    public void animateHide() {
        setHideAnimation(android.R.style.Animation_Translucent);
        super.hide();
    }

    private void setHideAnimation(int animation) {
        if (isVisible() && getLayoutParams().windowAnimations != animation) {
            getLayoutParams().windowAnimations = animation;
            updateLayoutParams();
        }
    }
}
