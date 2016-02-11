package ru.shmakinv.android.material.widget;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.view.ViewOutlineProvider;

/**
 * ViewUtilsLollipop
 */
class ViewUtilsLollipop {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setBoundsViewOutlineProvider(View view) {
        view.setOutlineProvider(ViewOutlineProvider.BOUNDS);
    }

}
