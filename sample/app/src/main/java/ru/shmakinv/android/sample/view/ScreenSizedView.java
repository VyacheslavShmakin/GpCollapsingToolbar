package ru.shmakinv.android.sample.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.FrameLayout;

/**
 * ScreenSizedView
 *
 * @author Vyacheslav Shmakin
 * @version 12.03.2016
 */
public class ScreenSizedView extends FrameLayout {
    public ScreenSizedView(Context context) {
        super(context);
    }

    public ScreenSizedView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScreenSizedView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ScreenSizedView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setupDimensions();
    }

    private void setupDimensions() {
        Resources resources = getResources();
        if (resources != null) {
            DisplayMetrics metrics = resources.getDisplayMetrics();
            if (metrics != null) {
                int width = metrics.widthPixels;
                int height = metrics.heightPixels;
                setMeasuredDimension(width, height);
            }
        }
    }
}
