import XCTest
import ScenarioKit

/// The driver's clock for every deadline: it does not jump when the wall clock is adjusted.
enum Monotonic {
    static var now: Double { Double(DispatchTime.now().uptimeNanoseconds) / 1_000_000_000 }
}

/// An element found for a query, with the application whose coordinate space its frame uses.
struct ResolvedElement {
    let owner: XCUIApplication
    let element: XCUIElement
}

/// What a snapshot of one element says, read once so it cannot change between reads.
struct ElementFacts {
    let type: XCUIElement.ElementType
    let frame: CGRect
    let isEnabled: Bool
    let value: String?
    let label: String
    /// The `selected` trait: how a toggle without a value (a Compose switch or checkbox) says it is on.
    let isSelected: Bool

    var isTextInput: Bool {
        type == .textField || type == .secureTextField || type == .textView || type == .searchField
    }

    /// What the user typed: the element's value, empty when it has none. An empty Compose field has no
    /// value (`placeholderValue` is nil too): its label then reads "USBID, 12-34567", the field's own
    /// label and placeholder, which is not text anyone typed.
    var typedText: String { value ?? "" }
}

/// How watching an element's frame ended.
enum FrameVerdict: Equatable {
    /// The frame read the same on [SettleWatch.requiredEqualReads] reads in a row.
    case settled
    /// The element was gone at a read.
    case vanished
    /// The reads or the time ran out first.
    case moving
}

/// Decides, read by read, whether a frame has stopped moving. It is pure: the caller feeds the frames it reads (`nil`
/// when the element could not be read any more) with the time of each read, and stops at the first verdict.
///
/// The frame must read the same [requiredEqualReads] times in a row: the accessibility tree of a list that is still
/// decelerating repeats a frame once or twice before the next one arrives, so two equal reads were seen to hold a tap on a
/// button that was still moving (the tap landed on the place it had left). The wait is bounded by the number of reads
/// and by time, whichever comes first; a slow machine does not shorten it to fewer reads than the rule needs, and an
/// element that never stops is a verdict, not a position to use.
struct SettleWatch {
    let requiredEqualReads: Int
    let maxReads: Int
    let timeLimit: Double
    private(set) var reads = 0
    private var equalReads = 0
    private var last: CGRect?
    private var firstReadAt: Double?

    init(requiredEqualReads: Int = 3, maxReads: Int = 20, timeLimit: Double = 8) {
        self.requiredEqualReads = requiredEqualReads
        self.maxReads = maxReads
        self.timeLimit = timeLimit
    }

    /// The verdict after the read at [time], or nil when more reads are needed.
    mutating func feed(_ frame: CGRect?, at time: Double) -> FrameVerdict? {
        guard let frame else { return .vanished }
        firstReadAt = firstReadAt ?? time
        reads += 1
        equalReads = last == frame ? equalReads + 1 : 1
        last = frame
        if equalReads >= requiredEqualReads { return .settled }
        if reads >= maxReads || time - (firstReadAt ?? time) >= timeLimit { return .moving }
        return nil
    }
}

/// How the checked state of a toggle is read from what its snapshot says (K-3/ZB-9). It is pure.
enum ToggleState {
    /// The element types that can hold a checked state: a switch, a checkbox, a toggle and, because that is how Compose publishes
    /// a `toggleable(role = Checkbox)`, a button.
    static func holdsState(_ type: XCUIElement.ElementType) -> Bool {
        type == .switch || type == .checkBox || type == .toggle || type == .button
    }

    /// A toggle that reports a value says it with "1"/"0"; the Compose ones report none and use the `Selected` trait. A value that
    /// is present and is none of the known ones (a localized one, say) is not read as "unchecked": it answers nil, so an
    /// assertion on it fails instead of passing by default.
    static func checked(value: String?, isSelected: Bool) -> Bool? {
        guard let value, !value.isEmpty else { return isSelected }
        switch value.lowercased() {
        case "1", "true", "on", "checked": return true
        case "0", "false", "off", "unchecked": return false
        default: return nil
        }
    }
}

/// Decides whether the app is in front from the three things the probe saw (YB-1). It is pure: the state before the wait, whether the wait
/// for the background state was answered "yes" and the state after it. An app that was in front, did not go to the background and is in
/// front again is in front; an app that was suspended while it was waited for (a state that is not "background") is not.
enum FrontCheck {
    static func isInFront(
        before: XCUIApplication.State,
        wentToBackground: Bool,
        after: XCUIApplication.State
    ) -> Bool {
        before == .runningForeground && !wentToBackground && after == .runningForeground
    }
}

/// What the state of the application allows (YB-5). It is pure. A suspended app is alive: `activate()` resumes it without a cold start.
enum AppLife {
    /// Whether `foreground()` may ask the system to bring the app back: it is alive in any way, suspended included. The state XCTest
    /// cannot read is not a death (`isRunning()` says alive) and is not brought back either: nothing is known about the process.
    static func canBeBroughtBack(_ state: XCUIApplication.State) -> Bool {
        state != .notRunning && state != .unknown
    }

    /// What is said of the app in the refusal: "not running" only for a process that is gone.
    static func describe(_ state: XCUIApplication.State) -> String {
        switch state {
        case .notRunning: return "not running"
        case .unknown: return "in a state XCTest cannot read"
        default: return "running"
        }
    }
}

/// What one look for an element found, reduced to what deciding that it is gone needs.
enum Sighting: Equatable {
    /// An element that meets the screen.
    case onScreen
    /// An element that exists but has an empty frame or one off the screen.
    case offScreen
    /// No element matched.
    case nothing
    /// An element exists and its snapshot could not be read.
    case unreadable
}

/// Decides whether an element is gone, with positive evidence (B-1/ΔB-9). It is pure: the caller says what it saw and hands
/// over the two reads that only a miss needs, so they are not paid for otherwise. It assumes the app was in the foreground when
/// the look began; [stillInForeground] is the check after the tree was read.
enum AbsenceRule {
    static func isAbsent(_ sighting: Sighting, treeRead: () -> Bool, stillInForeground: () -> Bool) -> Bool {
        switch sighting {
        case .onScreen, .unreadable:
            return false
        case .offScreen:
            return true
        case .nothing:
            // A miss proves nothing by itself: the tree must have been readable in this round and the app still in front.
            return treeRead() && stillInForeground()
        }
    }
}

/// What the keyboard guard knows about the on-screen keyboard after one read.
enum KeyboardReading {
    /// No keyboard is on screen, or the app is not in front.
    case none
    /// The keyboard is on screen with this frame.
    case frame(CGRect)
    /// The keyboard exists but its snapshot failed: whether a touch would land on it is not known.
    case unreadable
}

/// An element whose frame was watched until it was still, or the reason it was not.
enum Placement {
    case settled(ResolvedElement, ElementFacts)
    case moving(ElementFacts, reads: Int, seconds: Double)
    case gone(reads: Int)
}

/// Turns a ScenarioKit `Query` into an XCUITest element. Nothing here can raise an XCTest failure:
/// every lookup is guarded by the application state, and reads go through snapshots that throw
/// instead of asserting.
final class ElementResolver {
    static let springboardId = "com.apple.springboard"
    private static let settlePoll = 0.1

    let app: XCUIApplication
    let springboard = XCUIApplication(bundleIdentifier: ElementResolver.springboardId)
    private let log: DriverLog

    init(app: XCUIApplication, log: DriverLog) {
        self.app = app
        self.log = log
    }

    var screen: CGRect { UIScreen.main.bounds }

    var isAppRunning: Bool {
        app.state == .runningForeground || app.state == .runningBackground
    }

    /// Why a gesture is refused while [isAppRunning] is false: the app is not in front or behind another app. A suspended app
    /// is alive and is not "not running" (YB-5), so the reason says the state.
    var notRunningReason: String { "the app is not in front or behind another app (state \(app.state.rawValue))" }

    var isAppInForeground: Bool { app.state == .runningForeground }

    /// The first element matching [q] that exists right now, with what its snapshot says; nil when there is none or when it
    /// could not be read. `exists` comes before the snapshot on purpose: the snapshot of a missing element throws only after
    /// about 2.2 s (measured), `exists` answers a miss in about 60 ms, and a miss is what every poll for an element that is not
    /// there yet or not there any more does. A tag or a
    /// text counts only while the app is in the foreground (its tree stays readable behind Safari or a system sheet, and
    /// then it is not what the user sees); a system query may belong to SpringBoard.
    func lookup(_ q: Query) -> (ResolvedElement, ElementFacts)? {
        if case let .found(resolved, facts) = lookupOutcome(q) { return (resolved, facts) }
        return nil
    }

    /// What one look for [q] found: the element, nothing, or an element that exists but could not be read.
    enum LookupOutcome {
        case found(ResolvedElement, ElementFacts)
        case absent
        case unreadable
    }

    func lookupOutcome(_ q: Query) -> LookupOutcome {
        if let tag = q as? QueryTag {
            guard isAppInForeground else { return .absent }
            return firstReadable(in: [app]) { $0.descendants(matching: .any).matching(identifier: tag.value).firstMatch }
        }
        if let text = q as? QueryText {
            guard isAppInForeground else { return .absent }
            let operatorName = text.contains ? "CONTAINS" : "=="
            let predicate = NSPredicate(
                format: "label \(operatorName) %@ OR value \(operatorName) %@ OR title \(operatorName) %@",
                text.value, text.value, text.value
            )
            return firstReadable(in: [app]) { $0.descendants(matching: .any).matching(predicate).firstMatch }
        }
        if let system = q as? QuerySystem {
            let predicate = NSPredicate(format: "label == %@ OR identifier == %@", system.value, system.value)
            return firstReadable(in: [app, springboard]) { $0.descendants(matching: .any).matching(predicate).firstMatch }
        }
        return .absent
    }

    /// On screen: it exists, has a non-empty frame and that frame meets the screen. The foreground it needs is the cached state of
    /// the app, and that is all a lookup asks (waits, reads and gestures alike). The limit, measured (YB-1, E4): after the app leaves
    /// the front, the cached state keeps saying `runningForeground` for about 2.7 s (2.56 to 2.89 s, 6 runs), and a lookup that finds
    /// the element in its tree in that window answers "on screen" with Safari or a system sheet already in front. A scenario that
    /// leaves the app on purpose waits for it with `waitBackgrounded` (which asks [isAppFrontNow]) before it asserts anything.
    func visibleFacts(_ q: Query) -> (ResolvedElement, ElementFacts)? {
        guard let (resolved, facts) = lookup(q) else { return nil }
        guard !facts.frame.isEmpty, screen.intersects(facts.frame) else { return nil }
        return (resolved, facts)
    }

    /// How long [isAppFrontNow] gives XCTest to say that the app went to the background (ZB-13). Measured on the simulator (YB-1)
    /// with Safari opened over the app by `simctl openurl`, against the state of Safari itself sampled every 0.03 s and screenshots
    /// (the screen showed Safari 0.5 s after its state said foreground, and the app before): Safari is in front when the open returns;
    /// the cached state of the app and the probe say "not in front" only 2.6 to 2.9 s later, the probe within 0.03 s of the cached state
    /// (5 runs). The probe sees the change when the cached state does, so it costs 0.3 s per hit and covers nothing the cached state
    /// does not: lookups do not pay it any more (E4); `isForeground()`, `waitBackgrounded` and `foreground()` do, because the polling
    /// there converges.
    static let freshProbe = 0.3

    /// Whether the app is in front now, not as XCTest last cached it: the state, a wait for the background state that makes XCTest ask
    /// the system, and the state again (YB-1: an app that was suspended while it was waited for answers "not in background" and is not
    /// in front). It costs [freshProbe] when the app is in front, so it is asked once, when an answer is about to be given. This is the
    /// only implementation: `isForeground()` of the driver uses it, and so does the proof of an absence ([isAbsent]).
    var isAppFrontNow: Bool {
        let before = app.state
        guard before == .runningForeground else { return false }
        let wentToBackground = app.wait(for: .runningBackground, timeout: Self.freshProbe)
        return FrontCheck.isInFront(before: before, wentToBackground: wentToBackground, after: app.state)
    }

    /// Whether [q] is shown nowhere, with positive evidence: the app is in the foreground and its tree was read in this very
    /// round. An app that is not in the foreground, a tree that cannot be read or an element that exists but cannot be read
    /// prove nothing, and the caller keeps polling.
    func isAbsent(_ q: Query) -> Bool {
        guard isAppInForeground else { return false }
        let sighting: Sighting
        switch lookupOutcome(q) {
        case let .found(_, facts): sighting = facts.frame.isEmpty || !screen.intersects(facts.frame) ? .offScreen : .onScreen
        case .unreadable: sighting = .unreadable
        case .absent: sighting = .nothing
        }
        return AbsenceRule.isAbsent(sighting, treeRead: { (try? app.snapshot()) != nil }, stillInForeground: { isAppFrontNow })
    }

    /// The element's facts once its frame is still, so a gesture lands on the element and not on whatever is passing over its
    /// old place (a sheet sliding in, the form the keyboard pushes up). See [SettleWatch] for the rule. A frame that does not
    /// settle, or an element that disappears while it is watched, is a [Placement] that says so: the caller refuses the gesture.
    func settle(_ q: Query) -> Placement {
        guard let first = visibleFacts(q) else { return .gone(reads: 0) }
        var watch = SettleWatch()
        let began = Monotonic.now
        var last = first
        var verdict = watch.feed(first.1.frame, at: began)
        while verdict == nil {
            Thread.sleep(forTimeInterval: Self.settlePoll)
            let current = visibleFacts(q)
            verdict = watch.feed(current?.1.frame, at: Monotonic.now)
            if let current { last = current }
        }
        switch verdict! {
        case .settled: return .settled(last.0, last.1)
        case .vanished: return .gone(reads: watch.reads)
        case .moving: return .moving(last.1, reads: watch.reads, seconds: Monotonic.now - began)
        }
    }

    /// The settled element for [primitive], or nil after writing why that primitive is refused to the driver log.
    func placed(_ q: Query, primitive: String) -> (ResolvedElement, ElementFacts)? {
        switch settle(q) {
        case let .settled(resolved, facts):
            return (resolved, facts)
        case let .moving(facts, reads, seconds):
            log.refuse(primitive, "\(q): the frame was still moving after \(reads) reads in \(String(format: "%.1f", seconds)) s (last \(facts.frame)); gesture refused")
            return nil
        case let .gone(reads):
            log.refuse(primitive, "\(q): the element was \(reads == 0 ? "not visible" : "gone after \(reads) reads of its frame"); gesture refused")
            return nil
        }
    }

    /// What the on-screen keyboard looks like now: a frame, nothing, or a keyboard that exists and could not be read.
    var keyboard: KeyboardReading {
        let element = app.keyboards.firstMatch
        guard isAppInForeground, element.exists else { return .none }
        guard let snapshot = try? element.snapshot() else { return .unreadable }
        return snapshot.frame.isEmpty ? .none : .frame(snapshot.frame)
    }

    /// The part of [frame] that is on screen.
    func visiblePart(of frame: CGRect) -> CGRect { screen.intersection(frame) }

    func coordinate(at point: CGPoint, in resolved: ResolvedElement?) -> XCUICoordinate {
        let owner = resolved?.owner ?? app
        return owner.coordinate(withNormalizedOffset: .zero).withOffset(CGVector(dx: point.x, dy: point.y))
    }

    private func firstReadable(
        in owners: [XCUIApplication],
        _ query: (XCUIApplication) -> XCUIElement
    ) -> LookupOutcome {
        var unreadable = false
        for owner in owners {
            guard owner.state == .runningForeground || owner.state == .runningBackground else { continue }
            let element = query(owner)
            guard element.exists else { continue }
            guard let snapshot = try? element.snapshot() else {
                unreadable = true
                continue
            }
            let facts = ElementFacts(
                type: snapshot.elementType,
                frame: snapshot.frame,
                isEnabled: snapshot.isEnabled,
                value: snapshot.value as? String,
                label: snapshot.label,
                isSelected: snapshot.isSelected
            )
            return .found(ResolvedElement(owner: owner, element: element), facts)
        }
        return unreadable ? .unreadable : .absent
    }
}
