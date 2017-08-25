package org.willemsens.player.view.customviews;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

public class HeightCalculatedImageView extends AppCompatImageView {
    public HeightCalculatedImageView(Context context) {
        super(context);
    }

    public HeightCalculatedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HeightCalculatedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
