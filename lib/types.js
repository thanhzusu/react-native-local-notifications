export const BadgeIconType = {
  Large: 2,
  None: 0,
  Small: 1,
};
export const Category = {
  Alarm: 'alarm',
  Call: 'call',
  Email: 'email',
  Error: 'err',
  Event: 'event',
  Message: 'msg',
  Progress: 'progress',
  Promo: 'promo',
  Recommendation: 'recommendation',
  Reminder: 'reminder',
  Service: 'service',
  Social: 'social',
  Status: 'status',
  System: 'system',
  Transport: 'transport',
};
export const Defaults = {
  All: -1,
  Lights: 4,
  Sound: 1,
  Vibrate: 2,
};
export const GroupAlert = {
  All: 0,
  Children: 2,
  Summary: 1,
};
export const Importance = {
  Default: 3,
  High: 4,
  Low: 2,
  Max: 5,
  Min: 1,
  None: 0,
  Unspecified: -1000,
};
export const Priority = {
  Default: 0,
  High: 1,
  Low: -1,
  Max: 2,
  Min: -2,
};
export const SemanticAction = {
  Archive: 5,
  Call: 10,
  Delete: 4,
  MarkAsRead: 2,
  MarkAsUnread: 3,
  Mute: 6,
  None: 0,
  Reply: 1,
  ThumbsDown: 9,
  ThumbsUp: 8,
  Unmute: 7,
};
export const Visibility = {
  Private: 0,
  Public: 1,
  Secret: -1,
};

const PUSH_CHARS = '-0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz';
let lastPushTime = 0; // we generate 72-bits of randomness which get turned into 12 characters and appended to the
// timestamp to prevent collisions with other clients.  We store the last characters we
// generated because in the event of a collision, we'll use those same characters except
// "incremented" by one.
const lastRandChars = [];
/**
 * Generate a firebase id - for use with ref().push(val, cb) - e.g. -KXMr7k2tXUFQqiaZRY4'
 * @param serverTimeOffset - pass in server time offset from native side
 * @returns {string}
 */

export function generatePushID(serverTimeOffset = 0) {
  const timeStampChars = new Array(8);
  let now = new Date().getTime() + serverTimeOffset;
  const duplicateTime = now === lastPushTime;
  lastPushTime = now;

  for (let i = 7; i >= 0; i -= 1) {
    timeStampChars[i] = PUSH_CHARS.charAt(now % 64);
    now = Math.floor(now / 64);
  }

  if (now !== 0) throw new Error('We should have converted the entire timestamp.');
  let id = timeStampChars.join('');

  if (!duplicateTime) {
    for (let i = 0; i < 12; i += 1) {
      lastRandChars[i] = Math.floor(Math.random() * 64);
    }
  } else {
    // if the timestamp hasn't changed since last push,
    // use the same random number, but increment it by 1.
    let i;

    for (i = 11; i >= 0 && lastRandChars[i] === 63; i -= 1) {
      lastRandChars[i] = 0;
    }

    lastRandChars[i] += 1;
  }

  for (let i = 0; i < 12; i += 1) {
    id += PUSH_CHARS.charAt(lastRandChars[i]);
  }

  if (id.length !== 20) throw new Error('Length should be 20.');
  return id;
}