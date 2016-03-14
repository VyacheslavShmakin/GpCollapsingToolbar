package ru.shmakinv.android.sample.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.widget.NestedScrollView;
import android.view.View;

/**
 * ScrollController
 *
 * @author Vyacheslav Shmakin
 * @version 08.03.2016
 */
public class ScrollController {

    private static final String KEY_VERTICAL_OFFSET = "appbarlayout-vertical-offset";
    private static final String KEY_SCROLL_POSITION_MULTIPLIER = "scroll-position-multiplier";
    private OnTotalScrollChangeListener mOnTotalScrollChangeListener;
    private Double mScrollPositionMultiplier;
    private Integer mPreviousVerticalOffset;
    private Integer mNestedScrollRange;
    private Integer mAppBarScrollRange;
    private int mAppBarScrollPosition = 0;
    private int mNestedScrollPosition = 0;
    private int mOldScrollPosition = 0;

    public void onScrollChange(NestedScrollView v, int scrollY, int oldScrollY) {
        if (v != null) {
            int scrollRange = getScrollRange(v);
            if (mNestedScrollRange == null || scrollRange != mNestedScrollRange) {
                mNestedScrollRange = getScrollRange(v);
            }
        }
        calculateDistance(v, scrollY, oldScrollY);
    }

    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if (appBarLayout != null) {
            int scrollRange = appBarLayout.getTotalScrollRange();
            if (mAppBarScrollRange == null || mAppBarScrollRange != scrollRange) {
                mAppBarScrollRange = scrollRange;
            }
        }

        int absOffset = Math.abs(verticalOffset);
        StringBuilder sb = new StringBuilder();
        if (mPreviousVerticalOffset == null) {
            mPreviousVerticalOffset = absOffset;
            sb.append("Starting offset change detection\r\n");
        } else if (absOffset < mPreviousVerticalOffset) {
            sb.append("Scroll UP!");
        } else if (absOffset > mPreviousVerticalOffset) {
            sb.append("Scroll DOWN!");
        }
        calculateDistance(appBarLayout, absOffset, mPreviousVerticalOffset);
        mPreviousVerticalOffset = absOffset;
    }

    private void calculateDistance(View view, int scrollY, int oldScrollY) {
        if (mAppBarScrollRange == null || mNestedScrollRange == null) {
            return;
        }

        int totalScrollRange = mAppBarScrollRange + mNestedScrollRange;

        if (view instanceof AppBarLayout) {
            mAppBarScrollPosition = Math.abs(-scrollY);
        } else if (view instanceof NestedScrollView) {
            mNestedScrollPosition = view.getScrollY();
        }

        int scrollPosition = mAppBarScrollPosition + mNestedScrollPosition;
        mScrollPositionMultiplier = (double) (scrollPosition / totalScrollRange);
        if (mOnTotalScrollChangeListener != null) {
            mOnTotalScrollChangeListener.onTotalScrollChanged(view, scrollPosition, mOldScrollPosition, totalScrollRange);
        }
        mOldScrollPosition = scrollPosition;
    }

    public boolean hasDefinedScrollRange() {
        return mAppBarScrollRange != null && mNestedScrollRange != null
                && mAppBarScrollRange > 0 && mNestedScrollRange > 0;
    }

    public int getTotalScrollRange() {
        return mAppBarScrollRange + mNestedScrollRange;
    }

    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        mPreviousVerticalOffset = savedInstanceState.getInt(KEY_VERTICAL_OFFSET);
        mScrollPositionMultiplier = savedInstanceState.getDouble(KEY_SCROLL_POSITION_MULTIPLIER);
    }

    public void onSavedInstanceState(Bundle bundle) {
        if (bundle == null) {
            return;
        }
        bundle.putInt(KEY_VERTICAL_OFFSET, mPreviousVerticalOffset);
        bundle.putDouble(KEY_SCROLL_POSITION_MULTIPLIER, mScrollPositionMultiplier);
    }

    public void onResume(OnTotalScrollChangeListener listener,
                         @NonNull AppBarLayout appBar,
                         @NonNull NestedScrollView nestedScroll) {

        mOnTotalScrollChangeListener = listener;
        mAppBarScrollRange = appBar.getTotalScrollRange();
        mNestedScrollRange = getScrollRange(nestedScroll);
    }

    public void onPause() {
        mOnTotalScrollChangeListener = null;
    }

    public interface OnTotalScrollChangeListener {
        void onTotalScrollChanged(View view, int scrollY, int oldScrollY, int totalScrollRange);
    }

    private int getScrollRange(NestedScrollView view) {
        if (view == null) {
            return -1;
        }

        int scrollRange = 0;
        if (view.getChildCount() > 0) {
            View child = view.getChildAt(0);
            scrollRange = Math.max(0,
                    child.getHeight() - (view.getHeight() - view.getPaddingBottom() - view.getPaddingTop()));
        }
        return scrollRange;
    }
}
