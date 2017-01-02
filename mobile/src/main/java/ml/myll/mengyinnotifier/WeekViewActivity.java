package ml.myll.mengyinnotifier;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.android.colorpicker.ColorPickerDialog;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class WeekViewActivity extends WeekBaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    final static String TAG = "WVA";

    final static String FILENAME = "file";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_week_view_nav);

        //Set Toolbar as Action Bar
        this.setSupportActionBar((Toolbar)findViewById(R.id.toolbar2));

        (findViewById(R.id.toolbar2)).setBackgroundColor(getResources().getColor(R.color.black_overlay));

        firstCheck();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view2);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, (Toolbar)findViewById(R.id.toolbar2), R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(CommonUtils.currEvent).setChecked(true);

        mWeekView = ((WeekView)findViewById(R.id.weekView));
        // The week view
        mWeekView.setMonthChangeListener(this);
    }
//
//    @Override
//    public boolean onNavigationItemSelected(MenuItem item) {
//        // Handle navigation view item clicks here.
//        int id = item.getItemId();
//        switch (id) {
//            case R.id.sleep:
//                CommonUtils.newEvent(0);
//                renewCurEvent(0);
//                Log.i(TAG, "Event changed to 0");
//                item.setChecked(true);
//                break;
//            case R.id.work:
//                CommonUtils.newEvent(1);
//                renewCurEvent(1);
//                Log.i(TAG, "Event changed to 1");
//                item.setChecked(true);
//                break;
//            case R.id.study:
//                CommonUtils.newEvent(2);
//                renewCurEvent(2);
//                Log.i(TAG, "Event changed to 2");
//                item.setChecked(true);
//                break;
//            case R.id.recreation:
//                CommonUtils.newEvent(3);
//                renewCurEvent(3);
//                Log.i(TAG, "Event changed to 3");
//                item.setChecked(true);
//                break;
//            case R.id.sustain:
//                CommonUtils.newEvent(4);
//                renewCurEvent(4);
//                Log.i(TAG, "Event changed to 4");
//                item.setChecked(true);
//                break;
//            case R.id.other:
//                CommonUtils.newEvent(5);
//                renewCurEvent(5);
//                Log.i(TAG, "Event changed to 5");
//                item.setChecked(true);
//                break;
//            case R.id.nav_setting:
//                Intent intent = new Intent(this, SettingActivity.class);
//                startActivity(intent);
//                ((DrawerLayout)findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
//                return true;
//            default:
//                break;
//        }
//        mWeekView.refreshDrawableState();
//        return true;
//    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        if (item.isChecked()) return true;
        int id = item.getItemId();

        if(R.id.nav_setting == id) {
//                Intent intent = new Intent(this, SettingActivity.class);
//                startActivity(intent);
            int[] colorChoices = null;
//                try {
//                    Field[] fields = Class.forName(getPackageName() + ".R$color").getDeclaredFields();
//                    colorChoices = new int[fields.length];
//                    int i = 0;
//                    for (Field field : fields) {
//                        String colorName = field.getName();
//                        int colorId = field.getInt(null);
//                        int color = getResources().getColor(colorId);
//                        colorChoices[i++] = color;
//                        Log.i(TAG, "Added " + colorName + " => " + colorId + " => " + color);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
            if (colorChoices == null) colorChoices = CommonUtils.getColorsFromItems();
            ColorPickerDialog colorPickerDialog = new ColorPickerDialog();
            colorPickerDialog.initialize(
                    R.string.color_picker, colorChoices, colorChoices[0], 3, colorChoices.length);
            colorPickerDialog.show(getFragmentManager(), TAG);
            ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
            return true;
        }

        for (int i = 0; i < CommonUtils.drawerItemsIds.size(); i++) {
            if (id == CommonUtils.drawerItemsIds.get(i)) {
                CommonUtils.newEvent(i);
                renewCurEvent(i);
                Log.i(TAG, "Event changed to " + i);
                item.setChecked(true);
            }
        }
        return true;
    }

    @Override
    public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
        // Populate the week view with some events.
        List<WeekViewEvent> events = CommonUtils.getEvents(newYear, newMonth);
        Log.i(TAG, "Getting events, size is "+events.size());
        return events;
    }

    //Check internal FILE and renew CommonUtil.curEvent to that value. Default 5
    private void firstCheck () {
        try{
            FileInputStream fis = openFileInput(FILENAME);
            Scanner scanner = new Scanner(fis);
            int c = scanner.nextInt();
            Log.i(TAG, "onCreate Read internal: "+c);
            if (c<CommonUtils.items.size()) CommonUtils.currEvent = c;
            Log.i(TAG, "onCreate Prev event is: "+CommonUtils.currEvent);
            scanner.close();
        } catch (Exception e) {
            try {
                Log.i(TAG, "onCreate File not found, creating event 5 "+CommonUtils.items.get(5).getName());
                FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
                fos.write((5+"").getBytes());
                fos.flush();fos.close();
            } catch (IOException e1) {
                e.printStackTrace();
            }
        }
    }

    //Update internal FILE and renew CommonUtil.curEvent to <cur>.
    private void renewCurEvent (int cur){
        CommonUtils.currEvent = cur;
        FileOutputStream fos = null;
        try {
            fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (fos == null) return;
        try {
            fos.write((cur + "").getBytes());
            fos.flush();
            fos.close();
        } catch (IOException e){
            e.printStackTrace();
        }
        Toast.makeText(this, getString(R.string.please_open_again), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        setupDateTimeInterpreter(id == R.id.weekView);
        switch (id){
            case R.id.action_today:
                mWeekView.goToToday();
                return true;
            case R.id.action_day_view:
                if (mWeekViewType != TYPE_DAY_VIEW) {
                    item.setChecked(!item.isChecked());
                    mWeekViewType = TYPE_DAY_VIEW;
                    mWeekView.setNumberOfVisibleDays(1);

                    // Lets change some dimensions to best fit the view.
                    mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
                    mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 9, getResources().getDisplayMetrics()));
                    mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 9, getResources().getDisplayMetrics()));
                }
                mWeekView.refreshDrawableState();
                return true;
            case R.id.action_three_day_view:
                if (mWeekViewType != TYPE_THREE_DAY_VIEW) {
                    item.setChecked(!item.isChecked());
                    mWeekViewType = TYPE_THREE_DAY_VIEW;
                    mWeekView.setNumberOfVisibleDays(3);

                    // Lets change some dimensions to best fit the view.
                    mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()));
                    mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 9, getResources().getDisplayMetrics()));
                    mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 9, getResources().getDisplayMetrics()));
                }
                mWeekView.refreshDrawableState();
                return true;
            case R.id.action_week_view:
                if (mWeekViewType != TYPE_WEEK_VIEW) {
                    item.setChecked(!item.isChecked());
                    mWeekViewType = TYPE_WEEK_VIEW;
                    mWeekView.setNumberOfVisibleDays(7);

                    // Lets change some dimensions to best fit the view.
                    mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
                    mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 9, getResources().getDisplayMetrics()));
                    mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 8, getResources().getDisplayMetrics()));
                }
                mWeekView.refreshDrawableState();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
