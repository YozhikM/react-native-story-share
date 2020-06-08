#import "RNStoryShare.h"

@implementation RNStoryShare

- (dispatch_queue_t)methodQueue
{
  return dispatch_get_main_queue();
}

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(shareToInstagram:(NSDictionary *)config
                  resolver: (RCTPromiseResolveBlock)resolve
                  rejecter: (RCTPromiseRejectBlock)reject) {
    NSURL *scheme = [NSURL URLWithString:@"instagram-stories://share"];

    NSURL *backgroundAsset =  [NSURL URLWithString:config[@"backgroundAsset"]];
    
    NSData *backgroundVideo = [[NSFileManager defaultManager] contentsAtPath:backgroundAsset];
    NSArray *pasteboardItems = @[@{@"com.instagram.sharedSticker.backgroundVideo": backgroundVideo }];
    
    NSDictionary *pasteboardOptions = @{UIPasteboardOptionExpirationDate : [[NSDate date] dateByAddingTimeInterval:60 * 5]};
    
    [[UIPasteboard generalPasteboard] setItems:pasteboardItems options:pasteboardOptions];
    [[UIApplication sharedApplication] openURL:scheme options:@{} completionHandler:nil];
    
}

RCT_EXPORT_METHOD(isInstagramAvailable: (RCTPromiseResolveBlock)resolve
                  rejecter: (RCTPromiseRejectBlock)reject) {
    NSURL *scheme = [NSURL URLWithString:@"instagram-stories://share"];
    NSString *result = [[UIApplication sharedApplication] canOpenURL:scheme] == true ? @"true" : @"false";
    resolve(result);
}

@end

