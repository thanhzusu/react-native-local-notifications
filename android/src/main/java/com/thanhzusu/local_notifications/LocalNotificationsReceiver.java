package com.accupass.gma.local_notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/*
 * This is invoked by the Alarm Manager when it is time to display a scheduled notification.
 */
public class LocalNotificationsReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    new LocalNotificationsManager(context).displayScheduledNotification(intent.getExtras());
  }
}
