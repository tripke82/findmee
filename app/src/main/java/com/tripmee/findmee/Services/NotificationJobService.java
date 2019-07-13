package com.tripmee.findmee.Services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.util.Log;

import com.tripmee.findmee.Utilities.JobUtility;

/**
 * JobService to be scheduled by the JobScheduler.
 * start another service
 */
public class NotificationJobService extends JobService {
    private static final String TAG = "SyncService";

    @Override
    public boolean onStartJob(JobParameters params) {
        Intent service = new Intent(getApplicationContext(), NotificationService.class);

        Log.e("230182","NotificationJobService onStartJob");

        getApplicationContext().startForegroundService(service);

        JobUtility.scheduleJob(getApplicationContext()); // reschedule the job

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

}