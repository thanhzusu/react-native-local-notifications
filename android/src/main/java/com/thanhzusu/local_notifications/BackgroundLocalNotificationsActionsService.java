package com.accupass.gma.local_notification;

import android.content.Intent;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;

import javax.annotation.Nullable;

import static com.accupass.gma.local_notification.BackgroundLocalNotificationsActionReceiver.isBackgroundNotificationIntent;
import static com.accupass.gma.local_notification.BackgroundLocalNotificationsActionReceiver.toNotificationOpenMap;


public class BackgroundLocalNotificationsActionsService extends HeadlessJsTaskService {
  @Override
  protected @Nullable
  HeadlessJsTaskConfig getTaskConfig(Intent intent) {
    if (isBackgroundNotificationIntent(intent)) {
      WritableMap notificationOpenMap = toNotificationOpenMap(intent);

      return new HeadlessJsTaskConfig(
        "RNFirebaseBackgroundNotificationAction",
        notificationOpenMap,
        60000,
        true
      );
    }
    return null;
  }
}
