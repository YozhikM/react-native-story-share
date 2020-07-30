#import "RNStoryShare.h"
#import <Photos/Photos.h>

@implementation RNStoryShare

- (dispatch_queue_t)methodQueue
{
  return dispatch_get_main_queue();
}

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(shareToInstagram:(RCTPromiseResolveBlock)resolve
                  rejecter: (RCTPromiseRejectBlock)reject) {
   PHFetchOptions* fetchOptions = [PHFetchOptions new];
    fetchOptions.sortDescriptors = @[[NSSortDescriptor sortDescriptorWithKey:@"creationDate" ascending:NO],];
    PHFetchResult* fetchResult = [PHAsset fetchAssetsWithOptions:fetchOptions];
    PHAsset* assetToShare = fetchResult.firstObject;
    NSString *path = [@"instagram://library?AssetPath=" stringByAppendingString:assetToShare.localIdentifier];

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

