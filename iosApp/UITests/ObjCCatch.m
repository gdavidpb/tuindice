#import "ObjCCatch.h"

BOOL TIObjCCatch(NS_NOESCAPE void (^block)(void), NSString *_Nullable *_Nullable reason) {
    @try {
        block();
        return YES;
    } @catch (NSException *exception) {
        if (reason != NULL) {
            *reason = [NSString stringWithFormat:@"%@: %@", exception.name, exception.reason ?: @"(no reason)"];
        }
        return NO;
    }
}
