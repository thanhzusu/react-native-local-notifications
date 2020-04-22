#ifndef LocalNotifications_h
#define LocalNotifications_h

#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface LocalNotifications : RCTEventEmitter<RCTBridgeModule>

+ (void)configure;
+ (_Nonnull instancetype)instance;

@end

#else
@interface LocalNotifications : NSObject
@end
#endif


