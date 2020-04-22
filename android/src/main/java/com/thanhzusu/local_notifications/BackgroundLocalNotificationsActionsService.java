package com.thanhzusu.local_notifications;

import android.content.Intent;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;

import javax.annotation.Nullable;

import static com.thanhzusu.local_notifications.BackgroundLocalNotificationsActionReceiver.isBackgroundNotificationIntent;
import static com.thanhzusu.local_notifications.BackgroundLocalNotificationsActionReceiver.toNotificationOpenMap;


public class BackgroundLocalNotificationsActionsService extends HeadlessJsTaskService {
  @Override
  protected @Nullable
  HeadlessJsTaskConfig getTaskConfig(Intent intent) {
    if (isBackgroundNotificationIntent(intent)) {
      WritableMap notificationOpenMap = toNotificationOpenMap(intent);

      return new HeadlessJsTaskConfig(
        "BackgroundLocalNotificationsActionsService",
        notificationOpenMap,
        60000,
        true
      );
    }
    return null;
  }
}
