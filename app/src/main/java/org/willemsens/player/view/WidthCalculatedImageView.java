package org.willemsens.player.view;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

public class WidthCalculatedImageView extends AppCompatImageView {
    public WidthCalculatedImageView(Context context) {
        super(context);
    }

    public WidthCalculatedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WidthCalculatedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, heightMeasureSpec);
    }
}
