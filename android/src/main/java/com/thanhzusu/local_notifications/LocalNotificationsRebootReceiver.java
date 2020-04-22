package com.accupass.gma.local_notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/*
 * This is invoked when the phone restarts to ensure that all notifications are rescheduled
 * correctly, as Android removes all scheduled alarms when the phone shuts down.
 */
public class LocalNotificationsRebootReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    Log.i("LocalNotificationsRebootReceiver", "Received reboot event");
    new LocalNotificationsManager(context).rescheduleNotifications();
  }
}
