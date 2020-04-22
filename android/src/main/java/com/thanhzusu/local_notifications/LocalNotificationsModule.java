package com.thanhzusu.local_notifications;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import androidx.core.app.RemoteInput;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import java.util.ArrayList;
import me.leolin.shortcutbadger.ShortcutBadger;

import static com.thanhzusu.local_notifications.LocalNotificationsUtils.LOCAL_NOTIFICATIONS_OPENED_EVENT;
import static com.thanhzusu.local_notifications.LocalNotificationsUtils.LOCAL_NOTIFICATIONS_RECEIVED_EVENT;


public class LocalNotificationsModule extends ReactContextBaseJavaModule implements ActivityEventListener {
  private static final String BADGE_FILE = "BadgeCountFile";
  private static final String BADGE_KEY = "BadgeCount";
  private static final String TAG = "NotificationsModule";

  private SharedPreferences sharedPreferences;

  private LocalNotificationsManager localNotificationManager;

  LocalNotificationsModule(ReactApplicationContext context) {
    super(context);
    context.addActivityEventListener(this);
    localNotificationManager = new LocalNotificationsManager(context);
    sharedPreferences = context.getSharedPreferences(BADGE_FILE, Context.MODE_PRIVATE);

    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);

    // Subscribe to scheduled notification events
    localBroadcastManager.registerReceiver(
            new ScheduledNotificationReceiver(),
            new IntentFilter(LocalNotificationsManager.SCHEDULED_NOTIFICATION_EVENT)
    );
  }

  @Override
  public String getName() {
    return "LocalNotifications";
  }

  @ReactMethod
  public void cancelAllNotifications(Promise promise) {
    localNotificationManager.cancelAllNotifications(promise);
  }

  @ReactMethod
  public void cancelNotification(String notificationId, Promise promise) {
    localNotificationManager.cancelNotification(notificationId, promise);
  }

  @ReactMethod
  public void displayNotification(ReadableMap notification, Promise promise) {
    localNotificationManager.displayNotification(notification, promise);
  }

  @ReactMethod
  public void getBadge(Promise promise) {
    int badge = sharedPreferences.getInt(BADGE_KEY, 0);
    Log.d(TAG, "Got badge count: " + badge);
    promise.resolve(badge);
  }

  @ReactMethod
  public void getInitialNotification(Promise promise) {
    WritableMap notificationOpenMap = null;
    if (getCurrentActivity() != null) {
      notificationOpenMap = parseIntentForNotification(getCurrentActivity().getIntent());
    }
    promise.resolve(notificationOpenMap);
  }

  @ReactMethod
  public void getScheduledNotifications(Promise promise) {
    ArrayList<Bundle> bundles = localNotificationManager.getScheduledNotifications();
    WritableArray array = Arguments.createArray();
    for (Bundle bundle : bundles) {
      array.pushMap(parseNotificationBundle(bundle));
    }
    promise.resolve(array);
  }

  @ReactMethod
  public void removeAllDeliveredNotifications(Promise promise) {
    localNotificationManager.removeAllDeliveredNotifications(promise);
  }

  @ReactMethod
  public void removeDeliveredNotification(String notificationId, Promise promise) {
    localNotificationManager.removeDeliveredNotification(notificationId, promise);
  }

  @ReactMethod
  public void removeDeliveredNotificationsByTag(String tag, Promise promise) {
    localNotificationManager.removeDeliveredNotificationsByTag(tag, promise);
  }

  @ReactMethod
  public void setBadge(int badge, Promise promise) {
    // Store the badge count for later retrieval
    sharedPreferences
      .edit()
      .putInt(BADGE_KEY, badge)
      .apply();
    if (badge == 0) {
      Log.d(TAG, "Remove badge count");
      ShortcutBadger.removeCount(this.getReactApplicationContext());
    } else {
      Log.d(TAG, "Apply badge count: " + badge);
      ShortcutBadger.applyCount(this.getReactApplicationContext(), badge);
    }
    promise.resolve(null);
  }

  @ReactMethod
  public void scheduleNotification(ReadableMap notification, Promise promise) {
    localNotificationManager.scheduleNotification(notification, promise);
  }

  //////////////////////////////////////////////////////////////////////
  // Start Android specific methods
  //////////////////////////////////////////////////////////////////////
  @ReactMethod
  public void createChannel(ReadableMap channelMap, Promise promise) {
    try {
      localNotificationManager.createChannel(channelMap);
    } catch (Throwable t) {
      // do nothing - most likely a NoSuchMethodError for < v4 support lib
    }
    promise.resolve(null);
  }

  @ReactMethod
  public void createChannelGroup(ReadableMap channelGroupMap, Promise promise) {
    try {
      localNotificationManager.createChannelGroup(channelGroupMap);
    } catch (Throwable t) {
      // do nothing - most likely a NoSuchMethodError for < v4 support lib
    }
    promise.resolve(null);
  }

  @ReactMethod
  public void createChannelGroups(ReadableArray channelGroupsArray, Promise promise) {
    try {
      localNotificationManager.createChannelGroups(channelGroupsArray);
    } catch (Throwable t) {
      // do nothing - most likely a NoSuchMethodError for < v4 support lib
    }
    promise.resolve(null);
  }

  @ReactMethod
  public void createChannels(ReadableArray channelsArray, Promise promise) {
    try {
      localNotificationManager.createChannels(channelsArray);
    } catch (Throwable t) {
      // do nothing - most likely a NoSuchMethodError for < v4 support lib
    }
    promise.resolve(null);
  }

  @ReactMethod
  public void deleteChannelGroup(String channelId, Promise promise) {
    try {
      localNotificationManager.deleteChannelGroup(channelId);
      promise.resolve(null);
    } catch (NullPointerException e) {
      promise.reject(
        "notifications/channel-group-not-found",
        "The requested NotificationChannelGroup does not exist, have you created it?"
      );
    }
  }

  @ReactMethod
  public void deleteChannel(String channelId, Promise promise) {
    try {
      localNotificationManager.deleteChannel(channelId);
    } catch (Throwable t) {
      // do nothing - most likely a NoSuchMethodError for < v4 support lib
    }
    promise.resolve(null);
  }
  //////////////////////////////////////////////////////////////////////
  // End Android specific methods
  //////////////////////////////////////////////////////////////////////

  //////////////////////////////////////////////////////////////////////
  // Start ActivityEventListener methods
  //////////////////////////////////////////////////////////////////////
  @Override
  public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
    // FCM functionality does not need this function
  }

  @Override
  public void onNewIntent(Intent intent) {
    WritableMap notificationOpenMap = parseIntentForNotification(intent);
    if (notificationOpenMap != null) {
      LocalNotificationsUtils.sendEvent(
        getReactApplicationContext(),
              LOCAL_NOTIFICATIONS_OPENED_EVENT,
        notificationOpenMap
      );
    }
  }

  //////////////////////////////////////////////////////////////////////
  // End ActivityEventListener methods
  //////////////////////////////////////////////////////////////////////

  private WritableMap parseIntentForNotification(Intent intent) {
    WritableMap notificationOpenMap = parseIntentForRemoteNotification(intent);
    if (notificationOpenMap == null) {
      notificationOpenMap = parseIntentForLocalNotifications(intent);
    }
    return notificationOpenMap;
  }

  private WritableMap parseIntentForLocalNotifications(Intent intent) {
    if (intent.getExtras() == null || !intent.hasExtra("notificationId")) {
      return null;
    }

    WritableMap notificationMap = Arguments.makeNativeMap(intent.getExtras());
    WritableMap notificationOpenMap = Arguments.createMap();
    notificationOpenMap.putString("action", intent.getAction());
    notificationOpenMap.putMap("notification", notificationMap);

    // Check for remote input results
    Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
    if (remoteInput != null) {
      notificationOpenMap.putMap("results", Arguments.makeNativeMap(remoteInput));
    }

    return notificationOpenMap;
  }

  private WritableMap parseIntentForRemoteNotification(Intent intent) {
    // Check if FCM data exists
    if (intent.getExtras() == null || !intent.hasExtra("google.message_id")) {
      return null;
    }

    Bundle extras = intent.getExtras();

    WritableMap notificationMap = Arguments.createMap();
    WritableMap dataMap = Arguments.createMap();

    for (String key : extras.keySet()) {
      if (key.equals("google.message_id")) {
        notificationMap.putString("notificationId", extras.getString(key));
      } else if (key.equals("collapse_key")
        || key.equals("from")
        || key.equals("google.sent_time")
        || key.equals("google.ttl")
        || key.equals("_fbSourceApplicationHasBeenSet")) {
        // ignore known unneeded fields
      } else {
        dataMap.putString(key, extras.getString(key));
      }
    }
    notificationMap.putMap("data", dataMap);

    WritableMap notificationOpenMap = Arguments.createMap();
    notificationOpenMap.putString("action", intent.getAction());
    notificationOpenMap.putMap("notification", notificationMap);

    return notificationOpenMap;
  }

  private WritableMap parseNotificationBundle(Bundle notification) {
    return Arguments.makeNativeMap(notification);
  }

  private class ScheduledNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (getReactApplicationContext().hasActiveCatalystInstance()) {
        Log.d(TAG, "Received new scheduled notification");

        Bundle notification = intent.getBundleExtra("notification");
        WritableMap messageMap = parseNotificationBundle(notification);

        LocalNotificationsUtils.sendEvent(
                getReactApplicationContext(),
                LOCAL_NOTIFICATIONS_RECEIVED_EVENT,
                messageMap
        );
      }
    }
  }
}
