#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/// Runs [block]; answers YES when it returns. When it raises an Objective-C exception (XCTest raises one to interrupt a
/// test after it records an issue, for example when a tap is dispatched to an app that has just died), the exception is
/// caught here, YES is not returned and [reason] says what it was. Without this, such an exception unwinds through the
/// Kotlin frames the interpreter is running on, which Kotlin/Native turns into an abort of the whole runner.
BOOL TIObjCCatch(NS_NOESCAPE void (^block)(void), NSString *_Nullable *_Nullable reason);

NS_ASSUME_NONNULL_END
