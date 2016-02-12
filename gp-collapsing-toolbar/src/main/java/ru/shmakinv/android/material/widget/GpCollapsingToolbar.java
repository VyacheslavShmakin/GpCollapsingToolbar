package ru.shmakinv.android.material.widget;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * GpCollapsingToolbar
 *
 * @author: Vyacheslav Shmakin
 * @version: 02.02.2016
 */
public class GpCollapsingToolbar extends FrameLayout {

    private static final int SCRIM_ANIMATION_DURATION = 600;

    private boolean mRefreshToolbar = true;
    private int mToolbarId;
    private Toolbar mToolbar;
    private View mDummyView;

    private int mExpandedMarginLeft;
    private int mExpandedMarginTop;
    private int mExpandedMarginRight;
    private int mExpandedMarginBottom;

    private final Rect mTmpRect = new Rect();
    private final CollapsingTextHelper mCollapsingTextHelper;
    private boolean mCollapsingTitleEnabled;
    private boolean mDrawCollapsingTitle;
    private boolean mGooglePlayBehaviour;

    private Drawable mContentScrim;
    private Drawable mStatusBarScrim;
    private int mScrimAlpha;
    private boolean mScrimsAreShown;
    private ValueAnimatorCompat mScrimAnimator;

    private AppBarLayout.OnOffsetChangedListener mOnOffsetChangedListener;

    private int mCurrentOffset;

    private WindowInsetsCompat mLastInsets;

    public GpCollapsingToolbar(Context context) {
        this(context, null);
    }

    public GpCollapsingToolbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressLint("PrivateResource")
    public GpCollapsingToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mCollapsingTextHelper = new CollapsingTextHelper(this);
        mCollapsingTextHelper.setTextSizeInterpolator(AnimationUtils.DECELERATE_INTERPOLATOR);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GpCollapsingToolbarLayout, defStyleAttr,
                R.style.Widget_Design_CollapsingToolbar);

        mCollapsingTextHelper.setExpandedTextGravity(a.getInt(
                R.styleable.GpCollapsingToolbarLayout_gp_expandedTitleGravity,
                GravityCompat.START | Gravity.BOTTOM));

        mCollapsingTextHelper.setCollapsedTextGravity(
                a.getInt(R.styleable.GpCollapsingToolbarLayout_gp_collapsedTitleGravity,
                        GravityCompat.START | Gravity.CENTER_VERTICAL));

        mExpandedMarginLeft = mExpandedMarginTop = mExpandedMarginRight = mExpandedMarginBottom =
                a.getDimensionPixelSize(R.styleable.GpCollapsingToolbarLayout_gp_expandedTitleMargin, 0);

        final boolean isRtl = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;
        if (a.hasValue(R.styleable.GpCollapsingToolbarLayout_gp_expandedTitleMarginStart)) {
            final int marginStart = a.getDimensionPixelSize(
                    R.styleable.GpCollapsingToolbarLayout_gp_expandedTitleMarginStart, 0);
            if (isRtl) {
                mExpandedMarginRight = marginStart;
            } else {
                mExpandedMarginLeft = marginStart;
            }
        }
        if (a.hasValue(R.styleable.GpCollapsingToolbarLayout_gp_expandedTitleMarginEnd)) {
            final int marginEnd = a.getDimensionPixelSize(
                    R.styleable.GpCollapsingToolbarLayout_gp_expandedTitleMarginEnd, 0);
            if (isRtl) {
                mExpandedMarginLeft = marginEnd;
            } else {
                mExpandedMarginRight = marginEnd;
            }
        }
        if (a.hasValue(R.styleable.GpCollapsingToolbarLayout_gp_expandedTitleMarginTop)) {
            mExpandedMarginTop = a.getDimensionPixelSize(
                    R.styleable.GpCollapsingToolbarLayout_gp_expandedTitleMarginTop, 0);
        }
        if (a.hasValue(R.styleable.GpCollapsingToolbarLayout_gp_expandedTitleMarginBottom)) {
            mExpandedMarginBottom = a.getDimensionPixelSize(
                    R.styleable.GpCollapsingToolbarLayout_gp_expandedTitleMarginBottom, 0);
        }

        mCollapsingTitleEnabled = a.getBoolean(R.styleable.GpCollapsingToolbarLayout_gp_titleEnabled, true);
        mGooglePlayBehaviour = a.getBoolean(R.styleable.GpCollapsingToolbarLayout_gp_marketStyledBehaviour, false);
        setTitle(a.getText(R.styleable.GpCollapsingToolbarLayout_gp_title));

        // First load the default text appearances
        mCollapsingTextHelper.setExpandedTextAppearance(R.style.TextAppearance_Design_CollapsingToolbar_Expanded);
        mCollapsingTextHelper.setCollapsedTextAppearance(R.style.TextAppearance_AppCompat_Widget_ActionBar_Title);

        // Now overlay any custom text appearances
        if (a.hasValue(R.styleable.GpCollapsingToolbarLayout_gp_expandedTitleTextAppearance)) {
            mCollapsingTextHelper.setExpandedTextAppearance(
                    a.getResourceId(R.styleable.GpCollapsingToolbarLayout_gp_expandedTitleTextAppearance, 0));
        }
        if (a.hasValue(R.styleable.GpCollapsingToolbarLayout_gp_collapsedTitleTextAppearance)) {
            mCollapsingTextHelper.setCollapsedTextAppearance(
                    a.getResourceId(R.styleable.GpCollapsingToolbarLayout_gp_collapsedTitleTextAppearance, 0));
        }

        setContentScrim(a.getDrawable(R.styleable.GpCollapsingToolbarLayout_gp_contentScrim));
        setStatusBarScrim(a.getDrawable(R.styleable.GpCollapsingToolbarLayout_gp_statusBarScrim));

        mToolbarId = a.getResourceId(R.styleable.GpCollapsingToolbarLayout_gp_toolbarId, -1);

        a.recycle();

        setWillNotDraw(false);

        ViewCompat.setOnApplyWindowInsetsListener(this,
                new android.support.v4.view.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(View v,
                                                                  WindowInsetsCompat insets) {
                        mLastInsets = insets;
                        requestLayout();
                        return insets.consumeSystemWindowInsets();
                    }
                });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Add an OnOffsetChangedListener if possible
        final ViewParent parent = getParent();
        if (parent instanceof AppBarLayout) {
            if (mOnOffsetChangedListener == null) {
                mOnOffsetChangedListener = new OffsetUpdateListener();
            }
            ((AppBarLayout) parent).addOnOffsetChangedListener(mOnOffsetChangedListener);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        // Remove our OnOffsetChangedListener if possible and it exists
        final ViewParent parent = getParent();
        if (mOnOffsetChangedListener != null && parent instanceof AppBarLayout) {
            ((AppBarLayout) parent).removeOnOffsetChangedListener(mOnOffsetChangedListener);
        }

        super.onDetachedFromWindow();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        // If we don't have a toolbar, the scrim will be not be drawn in drawChild() below.
        // Instead, we draw it here, before our collapsing text.
        ensureToolbar();
        if (mToolbar == null && mContentScrim != null && mScrimAlpha > 0) {
            mContentScrim.mutate().setAlpha(mScrimAlpha);
            mContentScrim.draw(canvas);
        }

        // Let the collapsing text helper draw it's text
        if (mCollapsingTitleEnabled && mDrawCollapsingTitle) {
            mCollapsingTextHelper.draw(canvas);
        }

        // Now draw the status bar scrim
        if (mStatusBarScrim != null && mScrimAlpha > 0) {
            final int topInset = mLastInsets != null ? mLastInsets.getSystemWindowInsetTop() : 0;
            if (topInset > 0) {
                mStatusBarScrim.setBounds(0, -mCurrentOffset, getWidth(),
                        topInset - mCurrentOffset);
                mStatusBarScrim.mutate().setAlpha(mScrimAlpha);
                mStatusBarScrim.draw(canvas);
            }
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        // This is a little weird. Our scrim needs to be behind the Toolbar (if it is present),
        // but in front of any other children which are behind it. To do this we intercept the
        // drawChild() call, and draw our scrim first when drawing the toolbar
        ensureToolbar();
        if (child == mToolbar && mContentScrim != null && mScrimAlpha > 0) {
            mContentScrim.mutate().setAlpha(mScrimAlpha);
            mContentScrim.draw(canvas);
        }

        // Carry on drawing the child...
        return super.drawChild(canvas, child, drawingTime);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mContentScrim != null) {
            mContentScrim.setBounds(0, 0, w, h);
        }
    }

    private void ensureToolbar() {
        if (!mRefreshToolbar) {
            return;
        }

        Toolbar fallback = null, selected = null;

        for (int i = 0, count = getChildCount(); i < count; i++) {
            final View child = getChildAt(i);
            if (child instanceof Toolbar) {
                if (mToolbarId != -1) {
                    // There's a toolbar id set so try and find it...
                    if (mToolbarId == child.getId()) {
                        // We found the primary Toolbar, use it
                        selected = (Toolbar) child;
                        break;
                    }
                    if (fallback == null) {
                        // We'll record the first Toolbar as our fallback
                        fallback = (Toolbar) child;
                    }
                } else {
                    // We don't have a id to check for so just use the first we come across
                    selected = (Toolbar) child;
                    break;
                }
            }
        }

        if (selected == null) {
            // If we didn't find a primary Toolbar, use the fallback
            selected = fallback;
        }

        mToolbar = selected;
        updateDummyView();
        mRefreshToolbar = false;
    }

    private void updateDummyView() {
        if (!mCollapsingTitleEnabled && mDummyView != null) {
            // If we have a dummy view and we have our title disabled, remove it from its parent
            final ViewParent parent = mDummyView.getParent();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(mDummyView);
            }
        }
        if (mCollapsingTitleEnabled && mToolbar != null) {
            if (mDummyView == null) {
                mDummyView = new View(getContext());
            }
            if (mDummyView.getParent() == null) {
                mToolbar.addView(mDummyView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        ensureToolbar();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // Update the collapsed bounds by getting it's transformed bounds. This needs to be done
        // before the children are offset below
        if (mCollapsingTitleEnabled && mDummyView != null) {
            // We only draw the title if the dummy view is being displayed (Toolbar removes
            // views if there is no space)
            mDrawCollapsingTitle = mDummyView.isShown();

            if (mDrawCollapsingTitle) {
                ViewGroupUtils.getDescendantRect(this, mDummyView, mTmpRect);
                mCollapsingTextHelper.setCollapsedBounds(mTmpRect.left, bottom - mTmpRect.height(),
                        mTmpRect.right, bottom);
                // Update the expanded bounds
                mCollapsingTextHelper.setExpandedBounds(
                        mExpandedMarginLeft,
                        mTmpRect.bottom + mExpandedMarginTop,
                        right - left - mExpandedMarginRight,
                        bottom - top - mExpandedMarginBottom);
                // Now recalculate using the new bounds
                mCollapsingTextHelper.recalculate();
            }
        }

        // Update our child view offset helpers
        for (int i = 0, z = getChildCount(); i < z; i++) {
            final View child = getChildAt(i);

            if (mLastInsets != null && !ViewCompat.getFitsSystemWindows(child)) {
                final int insetTop = mLastInsets.getSystemWindowInsetTop();
                if (child.getTop() < insetTop) {
                    // If the child isn't set to fit system windows but is drawing within the inset
                    // offset it down
                    child.offsetTopAndBottom(insetTop);
                }
            }

            getViewOffsetHelper(child).onViewLayout();
        }

        // Finally, set our minimum height to enable proper AppBarLayout collapsing
        if (mToolbar != null) {
            if (mCollapsingTitleEnabled && TextUtils.isEmpty(mCollapsingTextHelper.getText())) {
                // If we do not currently have a title, try and grab it from the Toolbar
                mCollapsingTextHelper.setText(mToolbar.getTitle());
            }
            setMinimumHeight(mToolbar.getHeight());
        }
    }

    private static ViewOffsetHelper getViewOffsetHelper(View view) {
        ViewOffsetHelper offsetHelper = (ViewOffsetHelper) view.getTag(R.id.view_offset_helper);
        if (offsetHelper == null) {
            offsetHelper = new ViewOffsetHelper(view);
            view.setTag(R.id.view_offset_helper, offsetHelper);
        }
        return offsetHelper;
    }

    /**
     * Sets the title to be displayed by this view, if enabled.
     *
     * ref R.styleable#GpCollapsingToolbarLayout_gpTitle
     * @param title Title fot toolbar
     * @see #setTitleEnabled(boolean)
     * @see #getTitle()
     */
    public void setTitle(@Nullable CharSequence title) {
        mCollapsingTextHelper.setText(title);
    }

    /**
     * Returns the title currently being displayed by this view. If the title is not enabled, then
     * this will return {@code null}.
     *
     * ref R.styleable#GpCollapsingToolbarLayout_gpTitle
     * @return returns current title value;
     */
    @Nullable
    public CharSequence getTitle() {
        return mCollapsingTitleEnabled ? mCollapsingTextHelper.getText() : null;
    }

    /**
     * Sets whether this view should display its own title.
     *
     * <p>The title displayed by this view will shrink and grow based on the scroll offset.</p>
     *
     * ref R.styleable#GpCollapsingToolbarLayout_gpTitleEnabled
     * @see #setTitle(CharSequence)
     * @see #isTitleEnabled()
     */
    public void setTitleEnabled(boolean enabled) {
        if (enabled != mCollapsingTitleEnabled) {
            mCollapsingTitleEnabled = enabled;
            updateDummyView();
            requestLayout();
        }
    }

    /**
     * Returns whether this view is currently displaying its own title.
     *
     * ref R.styleable#GpCollapsingToolbarLayout_gpTitleEnabled
     * @see #setTitleEnabled(boolean)
     * @return title enable status;
     */
    public boolean isTitleEnabled() {
        return mCollapsingTitleEnabled;
    }

    /**
     * Sets whether this view should have like Google Play App scroll behaviour
     *
     * <p>The title displayed by this view will hide and show based on the Google Play's scroll behaviour.</p>
     *
     * ref R.styleable#GpCollapsingToolbarLayout_gpStyledBehaviour
     * @see #isGooglePlayBehaviour()
     */
    public void setGooglePlayBehaviour(boolean enabled) {
        if (enabled != mGooglePlayBehaviour) {
            mGooglePlayBehaviour = enabled;
            updateDummyView();
            requestLayout();
        }
    }

    /**
     * Returns whether this view is currently displaying by using Google Play's app behaviour.
     *
     * ref R.styleable#GpCollapsingToolbarLayout_gpStyledBehaviour
     * @see #setGooglePlayBehaviour(boolean)
     */
    public boolean isGooglePlayBehaviour() {
        return mGooglePlayBehaviour;
    }

    /**
     * Set whether the content scrim and/or status bar scrim should be shown or not. Any change
     * in the vertical scroll may overwrite this value. Any visibility change will be animated if
     * this view has already been laid out.
     *
     * @param shown whether the scrims should be shown
     * @see #getStatusBarScrim()
     * @see #getContentScrim()
     */
    public void setScrimsShown(boolean shown) {
        setScrimsShown(shown, ViewCompat.isLaidOut(this) && !isInEditMode());
    }

    /**
     * Set whether the content scrim and/or status bar scrim should be shown or not. Any change
     * in the vertical scroll may overwrite this value.
     *
     * @param shown   whether the scrims should be shown
     * @param animate whether to animate the visibility change
     * @see #getStatusBarScrim()
     * @see #getContentScrim()
     */
    public void setScrimsShown(boolean shown, boolean animate) {
        if (mScrimsAreShown != shown) {
            if (animate) {
                animateScrim(shown ? 0xFF : 0x0);
            } else {
                setScrimAlpha(shown ? 0xFF : 0x0);
            }
            mScrimsAreShown = shown;
        }
    }

    private void animateScrim(int targetAlpha) {
        ensureToolbar();
        if (mScrimAnimator == null) {
            mScrimAnimator = ViewUtils.createAnimator();
            mScrimAnimator.setDuration(SCRIM_ANIMATION_DURATION);
            mScrimAnimator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
            mScrimAnimator.setUpdateListener(new ValueAnimatorCompat.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimatorCompat animator) {
                    setScrimAlpha(animator.getAnimatedIntValue());
                }
            });
        } else if (mScrimAnimator.isRunning()) {
            mScrimAnimator.cancel();
        }

        mScrimAnimator.setIntValues(mScrimAlpha, targetAlpha);
        mScrimAnimator.start();
    }

    private void setScrimAlpha(int alpha) {
        if (alpha != mScrimAlpha) {
            final Drawable contentScrim = mContentScrim;
            if (contentScrim != null && mToolbar != null) {
                ViewCompat.postInvalidateOnAnimation(mToolbar);
            }
            mScrimAlpha = alpha;
            ViewCompat.postInvalidateOnAnimation(GpCollapsingToolbar.this);
        }
    }

    /**
     * Set the drawable to use for the content scrim from resources. Providing null will disable
     * the scrim functionality.
     *
     * @param drawable the drawable to display
     * ref R.styleable#GpCollapsingToolbarLayout_gpContentScrim
     * @see #getContentScrim()
     */
    public void setContentScrim(@Nullable Drawable drawable) {
        if (mContentScrim != drawable) {
            if (mContentScrim != null) {
                mContentScrim.setCallback(null);
            }
            if (drawable != null) {
                mContentScrim = drawable.mutate();
                drawable.setBounds(0, 0, getWidth(), getHeight());
                drawable.setCallback(this);
                drawable.setAlpha(mScrimAlpha);
            } else {
                mContentScrim = null;
            }
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * Set the color to use for the content scrim.
     *
     * @param color the color to display
     * ref R.styleable#GpCollapsingToolbarLayout_gpContentScrim
     * @see #getContentScrim()
     */
    public void setContentScrimColor(@ColorInt int color) {
        setContentScrim(new ColorDrawable(color));
    }

    /**
     * Set the drawable to use for the content scrim from resources.
     *
     * @param resId drawable resource id
     * ref R.styleable#GpCollapsingToolbarLayout_gpContentScrim
     * @see #getContentScrim()
     */
    public void setContentScrimResource(@DrawableRes int resId) {
        setContentScrim(ContextCompat.getDrawable(getContext(), resId));
    }

    /**
     * Returns the drawable which is used for the foreground scrim.
     *
     * ref R.styleable#GpCollapsingToolbarLayout_gpContentScrim
     * @see #setContentScrim(Drawable)
     */
    public Drawable getContentScrim() {
        return mContentScrim;
    }

    /**
     * Set the drawable to use for the status bar scrim from resources.
     * Providing null will disable the scrim functionality.
     *
     * <p>This scrim is only shown when we have been given a top system inset.</p>
     *
     * @param drawable the drawable to display
     * ref R.styleable#GpCollapsingToolbarLayout_gpStatusBarScrim
     * @see #getStatusBarScrim()
     */
    public void setStatusBarScrim(@Nullable Drawable drawable) {
        if (mStatusBarScrim != drawable) {
            if (mStatusBarScrim != null) {
                mStatusBarScrim.setCallback(null);
            }

            mStatusBarScrim = drawable;
            if (drawable != null) {
                drawable.setCallback(this);
                drawable.mutate().setAlpha(mScrimAlpha);
            }
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * Set the color to use for the status bar scrim.
     *
     * <p>This scrim is only shown when we have been given a top system inset.</p>
     *
     * @param color the color to display
     * ref R.styleable#GpCollapsingToolbarLayout_gpStatusBarScrim
     * @see #getStatusBarScrim()
     */
    public void setStatusBarScrimColor(@ColorInt int color) {
        setStatusBarScrim(new ColorDrawable(color));
    }

    /**
     * Set the drawable to use for the content scrim from resources.
     *
     * @param resId drawable resource id
     * ref R.styleable#GpCollapsingToolbarLayout_gpStatusBarScrim
     * @see #getStatusBarScrim()
     */
    public void setStatusBarScrimResource(@DrawableRes int resId) {
        setStatusBarScrim(ContextCompat.getDrawable(getContext(), resId));
    }

    /**
     * Returns the drawable which is used for the status bar scrim.
     *
     * ref R.styleable#GpCollapsingToolbarLayout_gpStatusBarScrim
     * @see #setStatusBarScrim(Drawable)
     * @return the drawable which is used for the status bar scrim
     */
    public Drawable getStatusBarScrim() {
        return mStatusBarScrim;
    }

    /**
     * Sets the text color and size for the collapsed title from the specified
     * TextAppearance resource.
     *
     * ref R.styleable#GpCollapsingToolbarLayout_gpCollapsedTitleTextAppearance
     */
    public void setCollapsedTitleTextAppearance(@StyleRes int resId) {
        mCollapsingTextHelper.setCollapsedTextAppearance(resId);
    }

    /**
     * Sets the text color of the collapsed title.
     *
     * @param color The new text color in ARGB format
     */
    public void setCollapsedTitleTextColor(@ColorInt int color) {
        mCollapsingTextHelper.setCollapsedTextColor(color);
    }

    /**
     * Sets the horizontal alignment of the collapsed title and the vertical gravity that will
     * be used when there is extra space in the collapsed bounds beyond what is required for
     * the title itself.
     *
     * ref R.styleable#GpCollapsingToolbarLayout_gpCollapsedTitleGravity
     */
    public void setCollapsedTitleGravity(int gravity) {
        mCollapsingTextHelper.setCollapsedTextGravity(gravity);
    }

    /**
     * Returns the horizontal and vertical alignment for title when collapsed.
     *
     * ref R.styleable#GpCollapsingToolbarLayout_gpCollapsedTitleGravity
     */
    public int getCollapsedTitleGravity() {
        return mCollapsingTextHelper.getCollapsedTextGravity();
    }

    /**
     * Sets the text color and size for the expanded title from the specified
     * TextAppearance resource.
     *
     * ref R.styleable#GpCollapsingToolbarLayout_gpExpandedTitleTextAppearance
     */
    public void setExpandedTitleTextAppearance(@StyleRes int resId) {
        mCollapsingTextHelper.setExpandedTextAppearance(resId);
    }

    /**
     * Sets the text color of the expanded title.
     *
     * @param color The new text color in ARGB format
     */
    public void setExpandedTitleColor(@ColorInt int color) {
        mCollapsingTextHelper.setExpandedTextColor(color);
    }

    /**
     * Sets the horizontal alignment of the expanded title and the vertical gravity that will
     * be used when there is extra space in the expanded bounds beyond what is required for
     * the title itself.
     *
     * ref R.styleable#GpCollapsingToolbarLayout_gpExpandedTitleGravity
     */
    public void setExpandedTitleGravity(int gravity) {
        mCollapsingTextHelper.setExpandedTextGravity(gravity);
    }

    /**
     * Returns the horizontal and vertical alignment for title when expanded.
     *
     * ref R.styleable#GpCollapsingToolbarLayout_gpExpandedTitleGravity
     */
    public int getExpandedTitleGravity() {
        return mCollapsingTextHelper.getExpandedTextGravity();
    }

    /**
     * Set the typeface to use for the collapsed title.
     *
     * @param typeface typeface to use, or {@code null} to use the default.
     */
    public void setCollapsedTitleTypeface(@Nullable Typeface typeface) {
        mCollapsingTextHelper.setCollapsedTypeface(typeface);
    }

    /**
     * Returns the typeface used for the collapsed title.
     */
    @NonNull
    public Typeface getCollapsedTitleTypeface() {
        return mCollapsingTextHelper.getCollapsedTypeface();
    }

    /**
     * Set the typeface to use for the expanded title.
     *
     * @param typeface typeface to use, or {@code null} to use the default.
     */
    public void setExpandedTitleTypeface(@Nullable Typeface typeface) {
        mCollapsingTextHelper.setExpandedTypeface(typeface);
    }

    /**
     * Returns the typeface used for the expanded title.
     */
    @NonNull
    public Typeface getExpandedTitleTypeface() {
        return mCollapsingTextHelper.getExpandedTypeface();
    }

    /**
     * The additional offset used to define when to trigger the scrim visibility change.
     */
    final int getScrimTriggerOffset() {
        return 2 * ViewCompat.getMinimumHeight(this);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(super.generateDefaultLayoutParams());
    }

    @Override
    public FrameLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected FrameLayout.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    public static class LayoutParams extends FrameLayout.LayoutParams {

        private static final float DEFAULT_PARALLAX_MULTIPLIER = 0.5f;

        /**
         * @hide
         */
        @IntDef({
                COLLAPSE_MODE_OFF,
                COLLAPSE_MODE_PIN,
                COLLAPSE_MODE_PARALLAX
        })
        @Retention(RetentionPolicy.SOURCE)
        @interface CollapseMode {
        }

        /**
         * The view will act as normal with no collapsing behavior.
         */
        public static final int COLLAPSE_MODE_OFF = 0;

        /**
         * The view will pin in place until it reaches the bottom of the
         * {@link GpCollapsingToolbar}.
         */
        public static final int COLLAPSE_MODE_PIN = 1;

        /**
         * The view will scroll in a parallax fashion. See {@link #setParallaxMultiplier(float)}
         * to change the multiplier used.
         */
        public static final int COLLAPSE_MODE_PARALLAX = 2;

        int mCollapseMode = COLLAPSE_MODE_OFF;
        float mParallaxMult = DEFAULT_PARALLAX_MULTIPLIER;

        @SuppressLint("PrivateResource")
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            TypedArray a = c.obtainStyledAttributes(attrs,
                    R.styleable.CollapsingAppBarLayout_LayoutParams);
            mCollapseMode = a.getInt(R.styleable.CollapsingAppBarLayout_LayoutParams_layout_collapseMode,
                    COLLAPSE_MODE_OFF);

            setParallaxMultiplier(a.getFloat(
                    R.styleable.CollapsingAppBarLayout_LayoutParams_layout_collapseParallaxMultiplier,
                    DEFAULT_PARALLAX_MULTIPLIER));

            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, int gravity) {
            super(width, height, gravity);
        }

        public LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        public LayoutParams(FrameLayout.LayoutParams source) {
            super(source);
        }

        /**
         * Set the collapse mode.
         *
         * @param collapseMode one of {@link #COLLAPSE_MODE_OFF}, {@link #COLLAPSE_MODE_PIN}
         *                     or {@link #COLLAPSE_MODE_PARALLAX}.
         */
        public void setCollapseMode(@CollapseMode int collapseMode) {
            mCollapseMode = collapseMode;
        }

        /**
         * Returns the requested collapse mode.
         *
         * @return the current mode. One of {@link #COLLAPSE_MODE_OFF}, {@link #COLLAPSE_MODE_PIN}
         * or {@link #COLLAPSE_MODE_PARALLAX}.
         */
        @CollapseMode
        public int getCollapseMode() {
            return mCollapseMode;
        }

        /**
         * Set the parallax scroll multiplier used in conjunction with
         * {@link #COLLAPSE_MODE_PARALLAX}. A value of {@code 0.0} indicates no movement at all,
         * {@code 1.0f} indicates normal scroll movement.
         *
         * @param multiplier the multiplier.
         * @see #getParallaxMultiplier()
         */
        public void setParallaxMultiplier(float multiplier) {
            mParallaxMult = multiplier;
        }

        /**
         * Returns the parallax scroll multiplier used in conjunction with
         * {@link #COLLAPSE_MODE_PARALLAX}.
         *
         * @see #setParallaxMultiplier(float)
         */
        public float getParallaxMultiplier() {
            return mParallaxMult;
        }
    }

    private class OffsetUpdateListener implements AppBarLayout.OnOffsetChangedListener {

        private static final int STATE_UNKNOWN = -1;
        private static final int STATE_HIDDEN = 0;
        private static final int STATE_SHOWN = 1;
        private final Drawable mTransparentDrawable = ContextCompat.getDrawable(getContext(), android.R.color.transparent);
        private Drawable mPreviousContentScrim = null;
        private CharSequence mPreviousTitle;
        private int mState = STATE_UNKNOWN;
        private int mPreviousOffset = 0;
        private boolean mGotIt = false;

        @Override
        public void onOffsetChanged(AppBarLayout layout, int verticalOffset) {
            mCurrentOffset = verticalOffset;

            final int insetTop = mLastInsets != null ? mLastInsets.getSystemWindowInsetTop() : 0;
            final int scrollRange = layout.getTotalScrollRange();

            int height = 0;
            for (int i = 0, z = getChildCount(); i < z; i++) {
                final View child = getChildAt(i);
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                final ViewOffsetHelper offsetHelper = getViewOffsetHelper(child);
                if (child instanceof Toolbar) {
                    height = child.getHeight();
                }

                switch (lp.mCollapseMode) {
                    case LayoutParams.COLLAPSE_MODE_PIN:
                        if (getHeight() - insetTop + verticalOffset >= child.getHeight()) {
                            offsetHelper.setTopAndBottomOffset(-verticalOffset);
                        }
                        break;
                    case LayoutParams.COLLAPSE_MODE_PARALLAX:
                        offsetHelper.setTopAndBottomOffset(
                                Math.round(-verticalOffset * lp.mParallaxMult));
                        break;
                }
            }

            // Show or hide the scrims if needed
            if (mContentScrim != null || mStatusBarScrim != null) {
                if (mGooglePlayBehaviour) {
                    processLikeGooglePlayBehaviour(height, verticalOffset, scrollRange);
                } else {
                    setScrimsShown(getHeight() + verticalOffset < getScrimTriggerOffset() + insetTop);
                }
            }

            if (mStatusBarScrim != null && insetTop > 0) {
                ViewCompat.postInvalidateOnAnimation(GpCollapsingToolbar.this);
            }

            // Update the collapsing text's fraction
            final int expandRange = getHeight() - ViewCompat.getMinimumHeight(GpCollapsingToolbar.this) - insetTop;
            /*Log.e("LOG", "offset = " + verticalOffset + "; height = " + height + "; isTransparent = " + scrollRange
                    + "; scrollRange = " + scrollRange);// + "; expandRange = " + expandRange);*/
            mCollapsingTextHelper.setExpansionFraction(Math.abs(verticalOffset) / (float) expandRange);

            if (Math.abs(verticalOffset) == scrollRange) {
                // If we have some pinned children, and we're offset to only show those views,
                // we want to be elevate
                ViewCompat.setElevation(layout, layout.getTargetElevation());
            } else {
                // Otherwise, we're inline with the content
                ViewCompat.setElevation(layout, 0f);
            }
        }

        private void processLikeGooglePlayBehaviour(int toolbarHeight, int verticalOffset, int scrollRange) {
            int absOffset = Math.abs(verticalOffset);
            boolean scrollDown, scrollUp, allowSwitch = false;
            boolean shown = absOffset >= scrollRange - toolbarHeight;

            if (mPreviousOffset < absOffset) {
                //Log.e("LOG", "scroll up");
                scrollUp = true;
                scrollDown = false;
            } else if (mPreviousOffset > absOffset) {
                //Log.e("LOG", "scroll down");
                scrollUp = false;
                scrollDown = true;
            } else {
                scrollUp = false;
                scrollDown = false;
            }

            //if scrollUp and scrim not shown - then
            // 1. Save previous ContentScrim
            // 2. Set contentScrim = (transparent);
            // If scrollDown && shown && current content scrim == transparent => return previous ContentScrim
            if (scrollUp && !shown && !mGotIt) {
                mPreviousContentScrim = getContentScrim();
                setContentScrim(mTransparentDrawable);
                mGotIt = true;
                //Log.e("LOG", "Content scrim saved");
            }

            if ((absOffset == scrollRange || mPreviousOffset == scrollRange)) {
                allowSwitch = true;
            }

            boolean transparent = getContentScrim().equals(mTransparentDrawable);

            if (scrollDown && shown && transparent && mGotIt && allowSwitch) {
                setContentScrim(mPreviousContentScrim);
                mGotIt = false;
                //Log.e("LOG", "Content scrim restored");
            }

            if (shown && mState == STATE_HIDDEN && !transparent) {
                setTitle(mPreviousTitle);
                mState = STATE_SHOWN;
            } else if (mState == STATE_UNKNOWN || (!shown && mState == STATE_SHOWN)) {
                mPreviousTitle = getTitle();
                setTitle("");
                mState = STATE_HIDDEN;
            }

            mPreviousOffset = absOffset;
            setScrimsShown(shown);
        }
    }
}

