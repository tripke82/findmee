package com.tripmee.findmee.Utilities;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import com.tripmee.findmee.Services.NotificationJobService;

public class JobUtility {


    //private static long intervalMillis = 10000; //60 seconds

    public static void scheduleJob(final Context context) {

        Log.e("230182","JobUtility scheduleJob");

        RunJob(context);

        /*final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //RunJob(context);
            }
        }, 3000);*/

    }

    private static void RunJob(Context context){

        ComponentName serviceComponent = new ComponentName(context, NotificationJobService.class);
        //ComponentName serviceComponent = new ComponentName(context,NotificationJobService.class.getName());
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        //builder.setPeriodic(intervalMillis, intervalMillis);
        //builder.setRequiresDeviceIdle(true);

        builder.setMinimumLatency(3 * 1000); // wait at least 3 sec
        builder.setOverrideDeadline(5 * 1000); // maximum delay 10 sec
        //builder.setPersisted(true);
        //builder.setRequiresDeviceIdle(true);
        //builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // require unmetered network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        //builder.setRequiresCharging(false); // we don't care if the device is charging or not

        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        jobScheduler.schedule(builder.build());

    }
}
