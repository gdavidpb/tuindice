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

    /// On screen: it exists, has a non-empty frame and that frame meets the screen.
    func visibleFacts(_ q: Query) -> (ResolvedElement, ElementFacts)? {
        guard let (resolved, facts) = lookup(q) else { return nil }
        guard !facts.frame.isEmpty, screen.intersects(facts.frame) else { return nil }
        return (resolved, facts)
    }

    /// Whether [q] is shown nowhere, with positive evidence: the app is in the foreground and its tree was read in this very
    /// round. An app that is not in the foreground, a tree that cannot be read or an element that exists but cannot be read
    /// prove nothing, and the caller keeps polling.
    func isAbsent(_ q: Query) -> Bool {
        guard isAppInForeground else { return false }
        switch lookupOutcome(q) {
        case let .found(_, facts):
            return facts.frame.isEmpty || !screen.intersects(facts.frame)
        case .unreadable:
            return false
        case .absent:
            guard (try? app.snapshot()) != nil else { return false }
            return isAppInForeground
        }
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

    /// The settled element for [gesture], or nil after writing why the gesture is refused to the driver log.
    func placed(_ q: Query, for gesture: String) -> (ResolvedElement, ElementFacts)? {
        switch settle(q) {
        case let .settled(resolved, facts):
            return (resolved, facts)
        case let .moving(facts, reads, seconds):
            log.refuse("[driver] \(gesture) \(q): the frame was still moving after \(reads) reads in \(String(format: "%.1f", seconds)) s (last \(facts.frame)); gesture refused")
            return nil
        case let .gone(reads):
            log.refuse("[driver] \(gesture) \(q): the element was \(reads == 0 ? "not visible" : "gone after \(reads) reads of its frame"); gesture refused")
            return nil
        }
    }

    /// The frame of the on-screen keyboard, nil when there is none.
    var keyboardFrame: CGRect? {
        let keyboard = app.keyboards.firstMatch
        guard isAppInForeground, keyboard.exists, let snapshot = try? keyboard.snapshot(), !snapshot.frame.isEmpty else { return nil }
        return snapshot.frame
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
