package ml.myll.mengyinnotifier;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewPropertyAnimator;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ObservableScrollViewCallbacks {


    private static final float MAX_TEXT_SCALE_DELTA = 0.3f;
    private static final String TAG = "Main";
    public static final String PREFS_NAME = "Settings";

    //Tool Bar
    Toolbar toolbar;

    //Main Act Pic
    private ImageView mImageView;
    private View mOverlayView;
    private ObservableScrollView mScrollView;
    private TextView mTitleView;
    private View mFab;
    private FloatingActionButton fab;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private RelativeLayout statusLayout;
    private ProgressBar progressBar;
    private TextView time_text;

    private int mActionBarSize;
    private int mFlexibleSpaceShowFabOffset;
    private int mFlexibleSpaceImageHeight;
    private int mFabMargin;
    private boolean mFabIsShown;
    private Point screenSize;
    private int timePercent;

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll,
                                boolean dragging) {
            Log.e("MY", "Y:"+scrollY);

        // Translate overlay and image
        float flexibleRange = mFlexibleSpaceImageHeight - mActionBarSize;
            Log.e("MY", "Y:"+mFlexibleSpaceImageHeight);
        int minOverlayTransitionY = mActionBarSize - mOverlayView.getHeight();
        mOverlayView.setTranslationY(ScrollUtils.getFloat(-scrollY, minOverlayTransitionY, 0));
        mImageView.setTranslationY(ScrollUtils.getFloat(-scrollY / 2, minOverlayTransitionY, 0));

        // Change alpha of overlay
        mOverlayView.setAlpha(ScrollUtils.getFloat((float) scrollY / flexibleRange, 0, 1));

        // Scale title text
        float scale = 1 + ScrollUtils.getFloat((flexibleRange - scrollY) / flexibleRange, 0, MAX_TEXT_SCALE_DELTA);
        mTitleView.setPivotX(0);
        mTitleView.setPivotY(0);
        mTitleView.setScaleX(scale);
        mTitleView.setScaleY(scale);

        // Translate title text
        int maxTitleTranslationY = (int) (mFlexibleSpaceImageHeight - mTitleView.getHeight() * scale);
        int titleTranslationY = maxTitleTranslationY - scrollY;
        mTitleView.setTranslationY(titleTranslationY);

        // Translate FAB
        int maxFabTranslationY = mFlexibleSpaceImageHeight - mFab.getHeight() / 2;
        float fabTranslationY = ScrollUtils.getFloat(
                -scrollY + mFlexibleSpaceImageHeight - mFab.getHeight() / 2,
                mActionBarSize - mFab.getHeight() / 2,
                maxFabTranslationY);
        mFab.setTranslationX(mOverlayView.getWidth() - mFabMargin - mFab.getWidth());
        mFab.setTranslationY(fabTranslationY);

        // Show/hide FAB
        if (fabTranslationY < mFlexibleSpaceShowFabOffset) {
            hideFab();
        } else {
            showFab();
        }
    }

    private void showFab() {
        if (!mFabIsShown) {
            ViewPropertyAnimator.animate(mFab).cancel();
            ViewPropertyAnimator.animate(mFab).scaleX(1).scaleY(1).setDuration(200).start();
            mFab.setClickable(true);
            mFabIsShown = true;
        }
    }

    private void hideFab() {
        if (mFabIsShown) {
            ViewPropertyAnimator.animate(mFab).cancel();
            ViewPropertyAnimator.animate(mFab).scaleX(0).scaleY(0).setDuration(200).start();
            mFab.setClickable(false);
            mFabIsShown = false;
        }
    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        if (scrollState == ScrollState.UP) {
            if (toolbarIsShown()) {
                hideToolbar();
            }
        } else if (scrollState == ScrollState.DOWN) {
            if (toolbarIsHidden()) {
                showToolbar();
            }
        }
    }

    //For onUpOrCancelMotionEvent
    private boolean toolbarIsShown() {
        return toolbar.getTranslationY() == 0;
    }

    private boolean toolbarIsHidden() {
        return toolbar.getTranslationY() == -toolbar.getHeight();
    }

    private void showToolbar() {
        moveToolbar(0);
    }

    private void hideToolbar() {
        moveToolbar(-toolbar.getHeight());
    }

    private void moveToolbar(float toTranslationY) {
        ValueAnimator animator = ValueAnimator.ofFloat(toolbar.getTranslationY(), toTranslationY).setDuration(200);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float translationY = (float) animation.getAnimatedValue();
                toolbar.setTranslationY(translationY);
            }
        });
        animator.start();
    }

    //Called when Scroll Back is clicked
    private void scrollBack() {
        onUpOrCancelMotionEvent(ScrollState.DOWN);
        ValueAnimator animator = ValueAnimator.ofFloat(mScrollView.getCurrentScrollY(), 0).setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float translationY = (float) animation.getAnimatedValue();
                mScrollView.scrollTo(0,(int)translationY);
            }
        });
        animator.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set Toolbar as Action Bar
        this.setSupportActionBar(toolbar);

        setAttributes();

        findViews();

        initViews();

        ScrollUtils.addOnGlobalLayoutListener(mScrollView, new Runnable() {
            @Override
            public void run() {
                onScrollChanged(0, false, false);
            }
        });
    }

    private void setAttributes() {
        mFlexibleSpaceImageHeight = getResources().getDimensionPixelSize(R.dimen.flexible_space_image_height);
        mFlexibleSpaceShowFabOffset = getResources().getDimensionPixelSize(R.dimen.flexible_space_show_fab_offset);

        //Get Action Bar Size...
        final TypedArray styledAttributes = this.getTheme().obtainStyledAttributes(
                new int[] { android.R.attr.actionBarSize });
        mActionBarSize = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        Log.i(TAG, "ABSize:" + mActionBarSize);

        //Get Display Size
        Display display = getWindowManager().getDefaultDisplay();
        screenSize = new Point();
        display.getSize(screenSize);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        int years = settings.getInt("year", 1900);
        timePercent = (100 + years - Calendar.getInstance().get(Calendar.YEAR));
    }

    private void findViews () {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mImageView = (ImageView) findViewById(R.id.image);
        mOverlayView = findViewById(R.id.overlay);
        mScrollView = (ObservableScrollView) findViewById(R.id.scroll);
        mTitleView = (TextView) findViewById(R.id.title);
        mFab = findViewById(R.id.fab);
        fab = (FloatingActionButton) findViewById(R.id.fab2);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        statusLayout = (RelativeLayout) findViewById(R.id.statusView);
        progressBar = (ProgressBar) findViewById(R.id.timeBar);
        time_text = (TextView) findViewById(R.id.intro_time);
    }

    private void initViews () {
        this.setSupportActionBar(toolbar);

        mImageView.setImageBitmap(
                CommonUtils.decodeSampledBitmapFromResource(getResources(),
                        R.drawable.bg, screenSize.x, R.dimen.flexible_space_image_height));
        mScrollView.setScrollViewCallbacks(this);
        mTitleView.setText(getTitle());
        setTitle(null);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Please set your Birthday", Toast.LENGTH_SHORT).show();
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });
        mFab.setClickable(false);
        mFabMargin = getResources().getDimensionPixelSize(R.dimen.margin_standard);
        mFab.setScaleX(0);
        mFab.setScaleY(0);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Back to the Top", Snackbar.LENGTH_LONG).show();
                scrollBack();
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        LinearLayout.LayoutParams rel_btn = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, screenSize.y);
        statusLayout.setLayoutParams(rel_btn);

        updateProgress();
    }

    public void expandFAB(int mode) {
        if (mode == 1) {
            ValueAnimator animator = ValueAnimator.ofFloat(1, 30).setDuration(1000);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float translationY = (float) animation.getAnimatedValue();
                    mFab.setScaleX(translationY);
                    mFab.setScaleY(translationY);
                }
            });
            animator.start();
        } else if (mode == 0) {
            ValueAnimator animator = ValueAnimator.ofFloat(30, 1).setDuration(1000);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float translationY = (float) animation.getAnimatedValue();
                    mFab.setScaleX(translationY);
                    mFab.setScaleY(translationY);
                }
            });
            animator.start();
        }
    }

    public void updateProgress () {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        int years = settings.getInt("year", 1900);
        timePercent = (100 + years - Calendar.getInstance().get(Calendar.YEAR));
        progressBar.setScaleY(15f);
        ValueAnimator animator = ValueAnimator.ofInt(100, timePercent).setDuration(5000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int percent = (int) animation.getAnimatedValue();
                progressBar.setProgress(100-percent);
                time_text.setText("You still have "+percent+ " years until 100 years old.");
                if (percent < 20) progressBar.getProgressDrawable().setColorFilter(
                        Color.rgb(255, 0, 0), android.graphics.PorterDuff.Mode.SRC_IN);
                else if (percent < 40) progressBar.getProgressDrawable().setColorFilter(
                        Color.rgb(255, 127, 0), android.graphics.PorterDuff.Mode.SRC_IN);
                else if (percent < 60) progressBar.getProgressDrawable().setColorFilter(
                        Color.rgb(255, 255, 0), android.graphics.PorterDuff.Mode.SRC_IN);
                else if (percent < 80) progressBar.getProgressDrawable().setColorFilter(
                        Color.rgb(127, 255, 0), android.graphics.PorterDuff.Mode.SRC_IN);
                else if (percent <= 100) progressBar.getProgressDrawable().setColorFilter(
                        Color.rgb(0, 255, 0), android.graphics.PorterDuff.Mode.SRC_IN);
            }
        });
        animator.start();
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.expandFAB(1);
            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, 2000, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            SharedPreferences preferences = getActivity().getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("year", year);
            editor.putInt("month", month);
            editor.putInt("day", day);
            editor.apply();
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.expandFAB(0);
            mainActivity.updateProgress();
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.expandFAB(0);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        scrollBack();
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
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
