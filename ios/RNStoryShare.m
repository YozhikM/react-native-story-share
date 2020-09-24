#import "RNStoryShare.h"
#import <Photos/Photos.h>

@implementation RNStoryShare

- (dispatch_queue_t)methodQueue
{
  return dispatch_get_main_queue();
}

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(shareToInstagram:(NSString *) localId
                  resolver: (RCTPromiseResolveBlock)resolve
                  rejecter: (RCTPromiseRejectBlock)reject) {
    NSString *path = [@"instagram://library?LocalIdentifier=" stringByAppendingString:localId];
    NSURL *scheme = [NSURL URLWithString: path];
    [[UIApplication sharedApplication] openURL:scheme options:@{} completionHandler:nil];
}

RCT_EXPORT_METHOD(isInstagramAvailable: (RCTPromiseResolveBlock)resolve
                  rejecter: (RCTPromiseRejectBlock)reject) {
    NSURL *scheme = [NSURL URLWithString:@"instagram://app"];
    NSString *result = [[UIApplication sharedApplication] canOpenURL:scheme] == true ? @"true" : @"false";
    resolve(result);
}

@end

