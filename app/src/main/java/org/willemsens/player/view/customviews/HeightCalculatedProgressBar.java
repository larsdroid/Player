package org.willemsens.player.view.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;

public class HeightCalculatedProgressBar extends ProgressBar {
    public HeightCalculatedProgressBar(Context context) {
        super(context);
    }

    public HeightCalculatedProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HeightCalculatedProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public HeightCalculatedProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
