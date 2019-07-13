package com.tripmee.findmee.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompletedReceiver extends BroadcastReceiver {

    private Context _Context;
    @Override
    public void onReceive(Context context, Intent intent) {

        // This is where you start your service

        _Context = context;

        //LaunchApp(context);


        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {

            //Log.d("TestBootCompleted", "Boot Completed");

            //JobUtility.scheduleJob(context);
        }

    }

    private void LaunchApp(Context context){
        Intent i = new Intent();
        i.setClassName("com.paranoidcompany.sam.findmee", "com.paranoidcompany.sam.findmee.MainActivity");
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);

    }

    private void ToDelete(){

        //_Context.startService(new Intent(_Context, BackgroundLocationService.class));
        //_Context.startService(new Intent(_Context, ExternalEventReceiver.class));

        //Intent intent = new Intent(context, LoginActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        //_Context.startActivity(intent);


        //Intent pushIntent = new Intent(context, ScreenOnOffBackgroundService.class);
        //context.startService(pushIntent);

        //Intent pushIntent = new Intent(context, ScreenOnOffBackgroundService.class);
        //context.startForegroundService(pushIntent);

        //Intent pushIntent = new Intent(context, BackgroundLocationService.class);
        //_Context.startService(pushIntent);
        //JobService.enqueueWork(_Context, new Intent());
        //RegisterOnOffReceiver();

        //Intent backgroundService = new Intent(_Context, ScreenOnOffBackgroundService.class);
        //_Context.startService(backgroundService);

        //notificationCall("Open Safie");

    }

}