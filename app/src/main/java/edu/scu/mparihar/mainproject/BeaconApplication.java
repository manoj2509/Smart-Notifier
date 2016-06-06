package edu.scu.mparihar.mainproject;

import android.annotation.TargetApi;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

/**
 * Created by Mj on 05-Jun-16.
 */
public class BeaconApplication extends Application implements BootstrapNotifier {
    private static final String TAG = "BeaconApp";
    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;
    private boolean haveDetectedBeaconsSinceBoot = false;
    private MainActivity mainActivity = null;
    private static Context context;


    public void onCreate() {
        super.onCreate();
        BeaconManager beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(getApplicationContext());
        // By default the AndroidBeaconLibrary will only find AltBeacons.  If you wish to make it
        // find a different type of beacon, you must specify the byte layout for that beacon's
        // advertisement with a line like below.  The example shows how to find a beacon with the
        // same byte layout as AltBeacon but with a beaconTypeCode of 0xaabb.  To find the proper
        // layout expression for other beacon types, do a web search for "setBeaconLayout"
        // including the quotes.
        //
        context = getApplicationContext();
        beaconManager.getBeaconParsers().clear();
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

//        beaconManager.getBeaconParsers().
//                add(new BeaconParser().
//                        setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        Log.d(TAG, "setting up background monitoring for beacons and power saving");
        // wake up the app when a beacon is seen
        Region region = new Region("backgroundRegion",
                null, null, null);
        regionBootstrap = new RegionBootstrap(this, region);

        // simply constructing this class and holding a reference to it in your custom Application
        // class will automatically cause the BeaconLibrary to save battery whenever the application
        // is not visible.  This reduces bluetooth power usage by about 60%
        backgroundPowerSaver = new BackgroundPowerSaver(this);

        // If you wish to test beacon detection in the Android Emulator, you can use code like this:
//         BeaconManager.setBeaconSimulator(new TimedBeaconSimulator() );
//         ((TimedBeaconSimulator) BeaconManager.getBeaconSimulator()).createTimedSimulatedBeacons();
    }

    @Override
    public void didEnterRegion(Region arg0) {
        // In this example, this class sends a notification to the user whenever a Beacon
        // matching a Region (defined above) are first seen.
        Log.d(TAG, "did enter region. " + arg0.getUniqueId());
        if (!haveDetectedBeaconsSinceBoot) {
            Log.d(TAG, "auto launching MainActivity");

            startService(new Intent(context, MyBeaconService.class).
                    setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

            // The very first time since boot that we detect a beacon, we launch the
            // MainActivity
//            Intent intent = new Intent(this, MyBeaconService.class);
//            intent.setAction("edu.scu.mparihar.BEACON_ENTER_INTENT");
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            sendBroadcast(intent);
            // Important:  make sure to add android:launchMode="singleInstance" in the manifest
            // to keep multiple copies of this activity from getting created if the user has
            // already manually launched the app.
//            this.startActivity(intent);
            haveDetectedBeaconsSinceBoot = true;
        } else {
            if (mainActivity != null) {
                // If the Monitoring Activity is visible, we log info about the beacons we have
                // seen on its display
//                mainActivity.logToDisplay("I see a beacon again" );
            } else {
                // If we have already seen beacons before, but the monitoring activity is not in
                // the foreground, we send a notification to the user on subsequent detections.
                Log.d(TAG, "Sending notification.");
                startService(new Intent(context, MyBeaconService.class).
                        setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
//                Intent intent = new Intent(this, MyBeaconService.class);
//                intent.setAction("edu.scu.mparihar.BEACON_ENTER_INTENT");
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                sendBroadcast(intent);
//                sendNotification();
            }
        }


    }

    @Override
    public void didExitRegion(Region region) {
        if (mainActivity != null) {
//            mainActivity.logToDisplay("I no longer see a beacon.");
            Log.d(TAG, "did exit region. " + region.getId1());
        }
    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        if (mainActivity != null) {
//            mainActivity.logToDisplay("I have just switched from seeing/not seeing beacons: " + state);
            Log.d(TAG, "I have just switched from seeing/not seeing beacons " + region.getId1());
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void sendNotification() {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setContentTitle("Smart Notifier!")
                        .setContentText("A beacon is nearby.")
                        .setSmallIcon(R.drawable.icon_outline);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(new Intent(this, MainActivity.class));
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    public void setMainActivity(MainActivity activity) {
        this.mainActivity = activity;
    }

}