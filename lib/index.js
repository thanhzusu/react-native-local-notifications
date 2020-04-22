/* eslint-disable class-methods-use-this */
/**
 *
 * LocalNotifications representation wrapper
 */
import EventEmitter from 'react-native/Libraries/vendor/emitter/EventEmitter';
import {
  Platform,
  NativeModules,
  NativeEventEmitter,
  DeviceEventEmitter,
} from 'react-native';
import _ from 'lodash';
import AndroidAction from './AndroidAction';
import AndroidChannel from './AndroidChannel';
import AndroidChannelGroup from './AndroidChannelGroup';
import AndroidNotifications from './AndroidNotifications';
import IOSNotifications from './IOSNotifications';
import AndroidRemoteInput from './AndroidRemoteInput';
import Notification from './Notification';

import {
  BadgeIconType,
  Category,
  Defaults,
  GroupAlert,
  Importance,
  Priority,
  SemanticAction,
  Visibility,
} from './types';

// fireDate: Date;
// timeZone: TimeZone;
// repeatInterval: NSCalendar.Unit;
// repeatCalendar: Calendar;
// region: CLRegion;
// regionTriggersOnce: boolean;
// iOS 10 scheduling
// TODO
// Android scheduling
// TODO

const NativeEventManager = Platform.select({
  ios: new NativeEventEmitter(NativeModules.LocalNotifications),
  android: DeviceEventEmitter,
});

const RNEventManager = new EventEmitter();

/**
 * @class LocalNotifications
 */

class LocalNotifications {
  constructor() {
    this.nativeModule = NativeModules.LocalNotifications;
    this._android = new AndroidNotifications(this);
    this._ios = new IOSNotifications(this);
    NativeEventManager.addListener(
      // sub to internal native event - this fans out to
      // public event name: onNotificationDisplayed
      'local_notifications_displayed',
      notification => {
        RNEventManager.emit(
          'onNotificationDisplayed',
          new Notification(notification, this),
        );
      },
    );
    NativeEventManager.addListener(
      // sub to internal native event - this fans out to
      // public event name: onNotificationOpened
      'local_notifications_opened',
      notificationOpen => {
        RNEventManager.emit('onNotificationOpened', {
          action: notificationOpen.action,
          notification: new Notification(notificationOpen.notification, this),
          results: notificationOpen.results,
        });
      },
    );
    NativeEventManager.addListener(
      // sub to internal native event - this fans out to
      // public event name: onNotification
      'local_notifications_received',
      notification => {
        RNEventManager.emit(
          'onNotification',
          new Notification(notification, this),
        );
      },
    ); // Tell the native module that we're ready to receive events

    if (Platform.OS === 'ios') {
      this.nativeModule.jsInitialised();
    }
  }

  get android() {
    return this._android;
  }

  get ios() {
    return this._ios;
  }
  /**
   * Cancel all notifications
   */

  cancelAllNotifications() {
    return this.nativeModule.cancelAllNotifications();
  }
  /**
   * Cancel a notification by id.
   * @param notificationId
   */

  cancelNotification(notificationId) {
    if (!notificationId) {
      return Promise.reject(
        new Error(
          'LocalNotifications: cancelNotification expects a `notificationId`',
        ),
      );
    }

    return this.nativeModule.cancelNotification(notificationId);
  }
  /**
   * Display a notification
   * @param notification
   * @returns {*}
   */

  displayNotification(notification) {
    if (!(notification instanceof Notification)) {
      return Promise.reject(
        new Error(
          `LocalNotifications:displayNotification expects a 'Notification' but got type ${typeof notification}`,
        ),
      );
    }

    try {
      return this.nativeModule.displayNotification(notification.build());
    } catch (error) {
      return Promise.reject(error);
    }
  }

  getBadge() {
    return this.nativeModule.getBadge();
  }

  getInitialNotification() {
    return this.nativeModule.getInitialNotification().then(notificationOpen => {
      if (notificationOpen) {
        return {
          action: notificationOpen.action,
          notification: new Notification(notificationOpen.notification, this),
          results: notificationOpen.results,
        };
      }

      return null;
    });
  }
  /**
   * Returns an array of all scheduled notifications
   * @returns {Promise.<Array>}
   */

  getScheduledNotifications() {
    return this.nativeModule.getScheduledNotifications();
  }

  onNotification(nextOrObserver) {
    let listener;

    if (_.isFunction(nextOrObserver)) {
      listener = nextOrObserver;
    } else if (
      _.isObject(nextOrObserver) &&
      _.isFunction(nextOrObserver.next)
    ) {
      listener = nextOrObserver.next;
    } else {
      throw new Error(
        'LocalNotifications.onNotification failed: First argument must be a function or observer object with a `next` function.',
      );
    }

    console.log('Creating onNotification listener');
    RNEventManager.addListener('onNotification', listener);
    return () => {
      console.log('Removing onNotification listener');
      RNEventManager.removeListener('onNotification', listener);
    };
  }

  onNotificationDisplayed(nextOrObserver) {
    let listener;

    if (_.isFunction(nextOrObserver)) {
      listener = nextOrObserver;
    } else if (
      _.isObject(nextOrObserver) &&
      _.isFunction(nextOrObserver.next)
    ) {
      listener = nextOrObserver.next;
    } else {
      throw new Error(
        'LocalNotifications.onNotificationDisplayed failed: First argument must be a function or observer object with a `next` function.',
      );
    }

    console.log('Creating onNotificationDisplayed listener');
    RNEventManager.addListener('onNotificationDisplayed', listener);
    return () => {
      console.log('Removing onNotificationDisplayed listener');
      RNEventManager.removeListener('onNotificationDisplayed', listener);
    };
  }

  onNotificationOpened(nextOrObserver) {
    let listener;

    if (_.isFunction(nextOrObserver)) {
      listener = nextOrObserver;
    } else if (
      _.isObject(nextOrObserver) &&
      _.isFunction(nextOrObserver.next)
    ) {
      listener = nextOrObserver.next;
    } else {
      throw new Error(
        'LocalNotifications.onNotificationOpened failed: First argument must be a function or observer object with a `next` function.',
      );
    }

    console.log('Creating onNotificationOpened listener');
    RNEventManager.addListener('onNotificationOpened', listener);
    return () => {
      console.log('Removing onNotificationOpened listener');
      RNEventManager.removeListener('onNotificationOpened', listener);
    };
  }
  /**
   * Remove all delivered notifications.
   */

  removeAllDeliveredNotifications() {
    return this.nativeModule.removeAllDeliveredNotifications();
  }
  /**
   * Remove a delivered notification.
   * @param notificationId
   */

  removeDeliveredNotification(notificationId) {
    if (!notificationId) {
      return Promise.reject(
        new Error(
          'LocalNotifications: removeDeliveredNotification expects a `notificationId`',
        ),
      );
    }

    return this.nativeModule.removeDeliveredNotification(notificationId);
  }
  /**
   * Schedule a notification
   * @param notification
   * @returns {*}
   */

  scheduleNotification(notification, schedule) {
    if (!(notification instanceof Notification)) {
      return Promise.reject(
        new Error(
          `LocalNotifications:scheduleNotification expects a 'Notification' but got type ${typeof notification}`,
        ),
      );
    }

    try {
      const nativeNotification = notification.build();
      nativeNotification.schedule = schedule;
      return this.nativeModule.scheduleNotification(nativeNotification);
    } catch (error) {
      return Promise.reject(error);
    }
  }

  setBadge(badge) {
    return this.nativeModule.setBadge(badge);
  }

  isLocalNotification(notification) {
    let local = false;
    if (notification.notificationId) {
      local = true;
    }
    return local;
  }
}
const localNotifications = new LocalNotifications();
export default localNotifications;
export const NotificationStatics = {
  Android: {
    Action: AndroidAction,
    BadgeIconType,
    Category,
    Channel: AndroidChannel,
    ChannelGroup: AndroidChannelGroup,
    Defaults,
    GroupAlert,
    Importance,
    Priority,
    RemoteInput: AndroidRemoteInput,
    SemanticAction,
    Visibility,
  },
  Notification,
};
