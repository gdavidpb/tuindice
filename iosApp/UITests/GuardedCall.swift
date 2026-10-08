import Foundation

/// Runs [body] with the Objective-C exceptions XCTest may raise caught (see `TIObjCCatch`): answers `true` when it ran to
/// the end and `false` after writing the exception to the driver log as the refusal of [primitive] (the primitive the driver
/// was performing; [what] says which part of it raised).
func guarded(_ primitive: String, _ what: String, log: DriverLog, _ body: () -> Void) -> Bool {
    var reason: NSString?
    if TIObjCCatch(body, &reason) { return true }
    log.refuse(primitive, "\(what): XCTest raised an exception and the call was abandoned: \(reason ?? "unknown")")
    return false
}
