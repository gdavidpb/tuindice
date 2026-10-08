import XCTest
import ScenarioKit

/// Text entry through the software keyboard. The driver never retypes and never judges: it types
/// what it is told and the interpreter re-reads the field. `app.typeText` is used instead of
/// `element.typeText` so XCTest's element-focus assertion cannot fire; before every call the
/// keyboard is confirmed on screen, which is the condition `typeText` asserts.
final class TextTyping {
    /// Characters per `typeText` call (F13 measured it: 4).
    private static let chunk = 4
    private static let pollInterval = 0.1
    private static let rereadTimeout = 1.0
    private static let keyboardTimeout = 2.0
    private static let actionKeys = ["Search", "Return", "Done", "Go", "Buscar", "Listo", "Ir"]

    private let resolver: ElementResolver
    private let log: DriverLog
    private let tapper: (ResolvedElement, CGPoint, String) -> Bool
    private var app: XCUIApplication { resolver.app }

    init(resolver: ElementResolver, log: DriverLog, tapper: @escaping (ResolvedElement, CGPoint, String) -> Bool) {
        self.resolver = resolver
        self.log = log
        self.tapper = tapper
    }

    func typeKeys(_ q: Query, text: String) -> Bool {
        guard focus(q) else { return false }
        return typeInChunks(text)
    }

    /// Empties the field without a triple tap on the element: the field is focused, one tap near its right end puts the caret
    /// after the text (it is the end of any text that stops before that point), and one delete key per character the field
    /// holds removes it. `typeText` cannot send arrow or forward-delete keys (it types their glyphs as text), and the edit
    /// menu's "Select All" is a label that depends on the language.
    ///
    /// The result is verified: the field is read again and the call answers `false` unless it is empty. Nothing is retried
    /// and there is no second strategy: a field that keeps text after one delete key per character is reported as it is.
    func clearText(_ q: Query) -> Bool {
        guard focus(q), let (resolved, facts) = resolver.placed(q, for: "clearText") else { return false }
        let held = facts.typedText
        if held.isEmpty { return true }

        let area = resolver.visiblePart(of: facts.frame)
        let end = CGPoint(x: area.minX + area.width * 0.75, y: area.midY)
        guard tapper(resolved, end, "clearText \(q) (caret to the end)") else { return false }
        guard typeInChunks(String(repeating: XCUIKeyboardKey.delete.rawValue, count: held.count)) else { return false }
        return typedTextAfterDeleting(q)?.isEmpty ?? false
    }

    /// What the field holds once the delete has been applied: read until two reads agree (the field
    /// redraws after a key), at most [rereadTimeout]; nil when the field cannot be read.
    private func typedTextAfterDeleting(_ q: Query) -> String? {
        var last = resolver.visibleFacts(q)?.1.typedText
        let deadline = Monotonic.now + Self.rereadTimeout
        while Monotonic.now < deadline {
            Thread.sleep(forTimeInterval: Self.pollInterval)
            let now = resolver.visibleFacts(q)?.1.typedText
            if now == last { return now }
            last = now
        }
        return last
    }

    /// Presses the keyboard's action key (Search, Return, Done...): the key a user presses to send the field. The key is
    /// tapped once its frame is still, and what was seen goes to the driver log: the keyboard slides in, and a tap on a key
    /// that is still moving was suspected of being the one that did not take. Answers true when the key was tapped; what the
    /// app does with the action, including whether the keyboard closes, is for the next step to wait for. A keyboard with
    /// no action key (a number pad) has nothing to press: that is a refusal that names the keys the keyboard does offer.
    func submitTextEntry() -> Bool {
        guard resolver.isAppRunning else { return refuse("submitTextEntry: the app is not running") }
        let keyboard = app.keyboards.firstMatch
        guard keyboard.exists else { return refuse("submitTextEntry: no keyboard is showing, so there is no action key to press") }

        for name in Self.actionKeys {
            let key = keyboard.buttons[name]
            guard key.exists else { continue }
            let (frame, reads) = settledFrame(of: key)
            log.add("[driver] submitTextEntry: key '\(name)' frame \(frame.map { "\($0)" } ?? "unreadable") after \(reads) reads")
            guard let frame else { return refuse("submitTextEntry: the '\(name)' key never stopped moving or could not be read") }
            // A key of the keyboard is not tapped through the driver's guard, which refuses points under the keyboard.
            resolver.coordinate(at: CGPoint(x: frame.midX, y: frame.midY), in: nil).tap()
            return true
        }
        let offered = keyboard.buttons.allElementsBoundByIndex.map { $0.label }
        return refuse("submitTextEntry: the keyboard has no action key among \(Self.actionKeys) (its buttons: \(offered))")
    }

    /// An iPhone keyboard has no key or gesture that closes it without the action of the field it serves, so there is
    /// nothing honest to do while one is showing: the answer is false and says what to use instead.
    func hideKeyboard() -> Bool {
        guard resolver.isAppRunning else { return refuse("hideKeyboard: the app is not running") }
        guard app.keyboards.firstMatch.exists else { return true }
        return refuse("hideKeyboard: iOS has no action that hides the keyboard without the action of its field; send that action or touch what the app closes it with")
    }

    private func refuse(_ reason: String) -> Bool {
        log.refuse("[driver] \(reason)")
        return false
    }

    /// The frame of [element] once it reads the same three times in a row; nil if it cannot be read or never stops.
    private func settledFrame(of element: XCUIElement) -> (CGRect?, Int) {
        var watch = SettleWatch()
        var frame: CGRect?
        var verdict: FrameVerdict?
        repeat {
            frame = (try? element.snapshot())?.frame
            verdict = watch.feed(frame, at: Monotonic.now)
            if verdict == nil { Thread.sleep(forTimeInterval: Self.pollInterval) }
        } while verdict == nil
        return (verdict == .settled ? frame : nil, watch.reads)
    }

    /// Taps the field once so that it takes the focus and waits for the keyboard. There is no second tap: with the frame settled
    /// before the tap (and the keyboard guard) it was never needed in 196 conformance runs (0 second taps), and a field that does
    /// not bring the keyboard up is reported, not tapped again.
    private func focus(_ q: Query) -> Bool {
        guard let (resolved, facts) = resolver.placed(q, for: "focus") else { return false }
        guard tapper(resolved, center(of: facts), "focus \(q)") else { return false }
        if waitForKeyboard() { return true }
        log.refuse("[driver] focus \(q): no keyboard \(Self.keyboardTimeout) s after the focus tap")
        return false
    }

    private func center(of facts: ElementFacts) -> CGPoint {
        let target = resolver.visiblePart(of: facts.frame)
        return CGPoint(x: target.midX, y: target.midY)
    }

    private func waitForKeyboard() -> Bool {
        guard resolver.isAppRunning else { return false }
        return app.keyboards.firstMatch.waitForExistence(timeout: Self.keyboardTimeout)
    }

    private func typeInChunks(_ text: String) -> Bool {
        var rest = Substring(text)
        while !rest.isEmpty {
            guard resolver.isAppRunning, app.keyboards.firstMatch.exists else { return false }
            let chunk = rest.prefix(Self.chunk)
            app.typeText(String(chunk))
            rest = rest.dropFirst(chunk.count)
        }
        return true
    }
}
