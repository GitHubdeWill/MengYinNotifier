package ml.myll.mengyinnotifier;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.ArraySet;
import android.util.Log;

import java.util.Arrays;
import java.util.Set;

/**
 * Created by William on 2016/11/26.
 */

public class SettingActivity extends PreferenceActivity {

    private static final String TAG = "Setting";
    public static final String PREFS_NAME = "Settings";


    SharedPreferences.OnSharedPreferenceChangeListener listener;

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            Thread.setDefaultUncaughtExceptionHandler(new MExceptionHandler(
                    CommonUtils.local_file));
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.settings_preferences);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
//        addPreferencesFromResource(R.xml.settings_preferences);
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                        // listener implementation
                        Log.i(TAG, "Pref changed "+key+" "+prefs.toString());
                        if (key.equals("shortcut_events")) {
                            Set<String> selections = new ArraySet<>();
                            selections = prefs.getStringSet(key, selections);
                            String[] selected = selections.toArray(new String[] {});
                            for (int i = 0; i < (selected.length<CommonUtils.shortcuts.length?
                                    selected.length:CommonUtils.shortcuts.length); i++) {
                                Log.i(TAG, "Shortcut "+i+" is "+selected[i]);
                                CommonUtils.shortcuts[i] = Integer.parseInt(selected[i]);
                            }
                            updateNotification();
                        } else if (key.equals("notification_dismiss")){
                            CommonUtils.stickyNotification = prefs.getBoolean(key, true);
                            updateNotification();
                        } else if (key.equals("color_change")){
                            CommonUtils.colorDynamic = prefs.getBoolean(key, true);
                            updateNotification();
                        } else if (key.equals("linechartdays")){
                            String str = prefs.getString(key, "7");
                            int days = Integer.parseInt(str);
                            if (days < 7)
                                CommonUtils.days = 7;
                            else if (days > 100)
                                CommonUtils.days = 100;
                            else
                                CommonUtils.days = days;
                        }
                    }
                };
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(listener);
    }

    public void updateNotification () {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// Sets an ID for the notification, so it can be updated
        int notifyID = MainActivity.notificationId;
        Intent window = new Intent(getApplicationContext(), FloatingActivity.class);
        window.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.click_to_open))
                .setSmallIcon(R.drawable.scaledicon)
                .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, window, 0));
// Start of a loop that processes data and then notifies the user
        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();
        String[] events = CommonUtils.getNamesFromItems();
// Sets a title for the Inbox in expanded layout
        inboxStyle.setBigContentTitle(getString(R.string.time_spending));
// Moves events into the expanded layout
        for (int i=0; i < events.length; i++) {
            PendingIntent pendingIntent;
            Intent intent = new Intent();
            intent.setClass(this, NotificationReceiver.class);
            intent.putExtra("event", i);
            intent.setAction("action1");
            intent.addCategory("category1");
            pendingIntent =  PendingIntent.getBroadcast(this, i, intent, 0);
            if (Arrays.asList(CommonUtils.shortcuts).contains(i)) {
                NotificationCompat.Action action = new NotificationCompat.Action(R.color.lime, events[i], pendingIntent);
                mBuilder.addAction(action);
            }
            inboxStyle.addLine(events[i]+": "+(CommonUtils.getTotalTime()[i]/1000/3600+"小时"));
        }
        inboxStyle.addLine("---------");
        inboxStyle.addLine("快捷切换：");
// Moves the expanded layout object into the notification object.
        mBuilder.setStyle(inboxStyle);
        mBuilder.setOngoing(CommonUtils.stickyNotification);
        mNotificationManager.notify(
                notifyID,
                mBuilder.build());
    }

}
