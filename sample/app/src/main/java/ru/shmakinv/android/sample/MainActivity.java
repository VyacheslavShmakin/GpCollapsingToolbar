package ru.shmakinv.android.sample;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ScrollView;

import com.squareup.picasso.Picasso;

import ru.shmakinv.android.sample.view.ScrollController;

/**
 * MainActivity
 *
 * @author Vyacheslav Shmakin
 * @version 14.03.2016
 */
public class MainActivity extends AppCompatActivity implements
        NestedScrollView.OnScrollChangeListener,
        ScrollController.OnTotalScrollChangeListener,
        View.OnClickListener,
        AppBarLayout.OnOffsetChangedListener {

    private FloatingActionButton mFab;
    private NestedScrollView mNestedScroll;
    private AppBarLayout mAppBarLayout;
    private ScrollView mParallaxView;

    private ScrollController mScrollController = new ScrollController();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ImageView imageView = (ImageView) findViewById(R.id.image);
        mNestedScroll = (NestedScrollView) findViewById(R.id.nestedScroll);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        mParallaxView = (ScrollView) findViewById(R.id.parallax);
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        setSupportActionBar(toolbar);

        setupToolbarAndStatusBar();
        Picasso.with(this).load("https://i.ytimg.com/vi/Un5SEJ8MyPc/maxresdefault.jpg")
                .into(imageView);
        mScrollController.onCreate(savedInstanceState);
    }

    private void setupToolbarAndStatusBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }

        Window window = getWindow();
        if (window != null) {
            View view = window.getDecorView();
            if (view != null) {
                view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFab.setOnClickListener(this);
        mNestedScroll.setOnScrollChangeListener(this);
        mAppBarLayout.addOnOffsetChangedListener(this);
        mScrollController.onResume(this, mAppBarLayout, mNestedScroll);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFab.setOnClickListener(null);
        mNestedScroll.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) null);
        mAppBarLayout.removeOnOffsetChangedListener(this);
        mScrollController.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mScrollController.onSavedInstanceState(outState);
    }

    @Override
    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        mScrollController.onScrollChange(v, scrollY, oldScrollY);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        mScrollController.onOffsetChanged(appBarLayout, verticalOffset);
    }

    @Override
    public void onTotalScrollChanged(View view, int scrollY, int oldScrollY, int totalScrollRange) {
        scrollY = (int) (scrollY * 0.5);
        mParallaxView.scrollTo(0, scrollY);
    }

    @Override
    public void onClick(View v) {
        Snackbar.make(v, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }
}
