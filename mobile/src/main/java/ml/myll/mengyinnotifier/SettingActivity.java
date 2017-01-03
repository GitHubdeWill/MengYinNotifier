package ml.myll.mengyinnotifier;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.ArraySet;
import android.util.Log;

import java.util.Arrays;
import java.util.Set;

import co.mobiwise.materialintro.prefs.PreferencesManager;

/**
 * Created by William on 2016/11/26.
 */

public class SettingActivity extends PreferenceActivity {

    private static final String TAG = "Setting";
    public static final String PREFS_NAME = "Settings";
    public static final String SHORTCUT = "Settings";


    SharedPreferences.OnSharedPreferenceChangeListener listener;

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

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
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("MY Time")
                .setContentText("Expand to check Time")
                .setSmallIcon(R.drawable.scaledicon)
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, AboutUsActivity.class), 0));
// Start of a loop that processes data and then notifies the user
        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();
        String[] events = CommonUtils.getNamesFromItems();
// Sets a title for the Inbox in expanded layout
        inboxStyle.setBigContentTitle("Time spending details:");
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
            inboxStyle.addLine(events[i]+": "+(CommonUtils.getTotalTime()[i]/1000/3600+" Hours"));
        }
// Moves the expanded layout object into the notification object.
        mBuilder.setStyle(inboxStyle);
        mNotificationManager.notify(
                notifyID,
                mBuilder.build());
    }

}
