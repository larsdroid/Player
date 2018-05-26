package org.willemsens.player.view.customviews;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

public class ClickableImageButton extends AppCompatImageButton implements View.OnTouchListener {
    private static final float DEFAULT_ELEVATION = 2.0f;
    private static final float PRESSED_ELEVATION = 6.0f;

    public ClickableImageButton(Context context) {
        super(context);
        this.setOnTouchListener(this);
        setDefaults(null);
    }

    public ClickableImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOnTouchListener(this);
        setDefaults(attrs);
    }

    public ClickableImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setOnTouchListener(this);
        setDefaults(attrs);
    }

    private void setDefaults(AttributeSet attrs) {
        if (attrs == null || attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "elevation") == null) {
            this.setElevation(convertDpToPixel(DEFAULT_ELEVATION));
        }
    }

    public static float convertDpToPixel(float dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                ClickableImageButton view = (ClickableImageButton) v;
                view.getBackground().setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                view.setElevation(convertDpToPixel(PRESSED_ELEVATION));
                view.invalidate();
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                ClickableImageButton view = (ClickableImageButton) v;
                view.getBackground().clearColorFilter();
                view.setElevation(convertDpToPixel(DEFAULT_ELEVATION));
                view.invalidate();
                break;
            }
        }

        return false;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled) {
            this.getDrawable().clearColorFilter();
            this.getBackground().clearColorFilter();
        } else {
            this.getDrawable().setColorFilter(0xC0CCCCCC, PorterDuff.Mode.SRC_ATOP);
            this.getBackground().setColorFilter(0xC0CCCCCC, PorterDuff.Mode.SRC_ATOP);
        }

        super.setEnabled(enabled);
    }
}
