import Foundation

/// Runs [body] with the Objective-C exceptions XCTest may raise caught (see `TIObjCCatch`): answers `true` when it ran to
/// the end and `false` after writing the exception to the driver log as the reason of the refusal.
func guarded(_ what: String, log: DriverLog, _ body: () -> Void) -> Bool {
    var reason: NSString?
    if TIObjCCatch(body, &reason) { return true }
    log.refuse("[driver] \(what): XCTest raised an exception and the call was abandoned: \(reason ?? "unknown")")
    return false
}
