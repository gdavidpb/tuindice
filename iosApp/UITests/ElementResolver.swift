import XCTest
import ScenarioKit

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

    var isTextInput: Bool {
        type == .textField || type == .secureTextField || type == .textView || type == .searchField
    }

    /// What the user typed: the element's value, empty when it has none. An empty Compose field has no
    /// value (`placeholderValue` is nil too): its label then reads "USBID, 12-34567", the field's own
    /// label and placeholder, which is not text anyone typed.
    var typedText: String { value ?? "" }
}

/// Turns a ScenarioKit `Query` into an XCUITest element. Nothing here can raise an XCTest failure:
/// every lookup is guarded by the application state and `exists`, and reads go through snapshots
/// that throw instead of asserting.
final class ElementResolver {
    static let springboardId = "com.apple.springboard"
    private static let settleTimeout = 2.0
    private static let settlePoll = 0.1

    let app: XCUIApplication
    let springboard = XCUIApplication(bundleIdentifier: ElementResolver.springboardId)

    init(app: XCUIApplication) {
        self.app = app
    }

    var screen: CGRect { UIScreen.main.bounds }

    var isAppRunning: Bool {
        app.state == .runningForeground || app.state == .runningBackground
    }

    /// The first element matching [q] that exists right now, or nil.
    func find(_ q: Query) -> ResolvedElement? {
        if let tag = q as? QueryTag {
            return firstExisting(in: [app]) { $0.descendants(matching: .any).matching(identifier: tag.value).firstMatch }
        }
        if let text = q as? QueryText {
            let operatorName = text.contains ? "CONTAINS" : "=="
            let predicate = NSPredicate(
                format: "label \(operatorName) %@ OR value \(operatorName) %@ OR title \(operatorName) %@",
                text.value, text.value, text.value
            )
            return firstExisting(in: [app]) { $0.descendants(matching: .any).matching(predicate).firstMatch }
        }
        if let system = q as? QuerySystem {
            let predicate = NSPredicate(format: "label == %@ OR identifier == %@", system.value, system.value)
            return firstExisting(in: [app, springboard]) { $0.descendants(matching: .any).matching(predicate).firstMatch }
        }
        return nil
    }

    func facts(of resolved: ResolvedElement) -> ElementFacts? {
        guard let snapshot = try? resolved.element.snapshot() else { return nil }
        return ElementFacts(
            type: snapshot.elementType,
            frame: snapshot.frame,
            isEnabled: snapshot.isEnabled,
            value: snapshot.value as? String,
            label: snapshot.label
        )
    }

    /// On screen: it exists, has a non-empty frame and that frame meets the screen.
    func visibleFacts(_ q: Query) -> (ResolvedElement, ElementFacts)? {
        guard let resolved = find(q), let facts = facts(of: resolved) else { return nil }
        guard !facts.frame.isEmpty, screen.intersects(facts.frame) else { return nil }
        return (resolved, facts)
    }

    /// The element's facts once its frame has stopped moving, so a gesture lands on the element and not
    /// on whatever is passing over its old place (a sheet sliding in, the form the keyboard pushes up).
    /// The frame is read every [settlePoll] until two reads agree or [settleTimeout] passes; the last
    /// read is then used as it is.
    func settledFacts(_ q: Query) -> (ResolvedElement, ElementFacts)? {
        guard let (resolved, facts, _) = settle(q) else { return nil }
        return (resolved, facts)
    }

    /// Like [settledFacts], and says whether the frame stopped moving (`false`: the last read after
    /// [settleTimeout], or the element vanished while it was being watched).
    func settle(_ q: Query) -> (ResolvedElement, ElementFacts, Bool)? {
        guard var last = visibleFacts(q) else { return nil }
        let deadline = Date().addingTimeInterval(Self.settleTimeout)
        while Date() < deadline {
            Thread.sleep(forTimeInterval: Self.settlePoll)
            guard let current = visibleFacts(q) else { return (last.0, last.1, false) }
            if current.1.frame == last.1.frame { return (current.0, current.1, true) }
            last = current
        }
        return (last.0, last.1, false)
    }

    /// The part of [frame] that is on screen.
    func visiblePart(of frame: CGRect) -> CGRect { screen.intersection(frame) }

    func coordinate(at point: CGPoint, in resolved: ResolvedElement?) -> XCUICoordinate {
        let owner = resolved?.owner ?? app
        return owner.coordinate(withNormalizedOffset: .zero).withOffset(CGVector(dx: point.x, dy: point.y))
    }

    private func firstExisting(
        in owners: [XCUIApplication],
        _ query: (XCUIApplication) -> XCUIElement
    ) -> ResolvedElement? {
        for owner in owners {
            guard owner.state == .runningForeground || owner.state == .runningBackground else { continue }
            let element = query(owner)
            if element.exists { return ResolvedElement(owner: owner, element: element) }
        }
        return nil
    }
}
