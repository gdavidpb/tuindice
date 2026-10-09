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
    /// How long `submitTextEntry` waits for a keyboard: `Timeouts.Action`.
    private static let submitWait = 10.0
    private static let actionKeys = ["Search", "Return", "Done", "Go", "Buscar", "Listo", "Ir"]

    private let resolver: ElementResolver
    private let log: DriverLog
    /// Taps a point of the screen on behalf of a primitive (`typeKeys`, `clearText`) at a target; the driver's guarded tap.
    private let tapper: (ResolvedElement, CGPoint, String, String) -> Bool
    private var app: XCUIApplication { resolver.app }

    init(resolver: ElementResolver, log: DriverLog, tapper: @escaping (ResolvedElement, CGPoint, String, String) -> Bool) {
        self.resolver = resolver
        self.log = log
        self.tapper = tapper
    }

    /// Characters the last `typeKeys` put in (see `TextEntry.keysInjected`). `clearText` starts it again from zero: its own
    /// deletions are counted, not the characters of the `typeKeys` before it.
    private(set) var injected = 0

    func typeKeys(_ q: Query, text: String) -> Bool {
        injected = 0
        guard let before = resolver.visibleFacts(q)?.1.typedText else { return refuse("typeKeys", "\(q): the field is not on screen") }
        guard focus(q, primitive: "typeKeys") else { return false }
        // The touch that focuses the field must not alter it: if it holds other text than before, nothing is typed.
        let now = resolver.visibleFacts(q)?.1.typedText
        guard now == before else {
            return refuse("typeKeys", "\(q): the focus touch changed the field (it held \(before.count) characters and now holds \(now?.count ?? -1))")
        }
        return typeInChunks(text, primitive: "typeKeys")
    }

    /// Empties the field without a triple tap on the element: the field is focused, one tap near its right end puts the caret
    /// after the text (it is the end of any text that stops before that point), and one delete key per character the field
    /// holds removes it. `typeText` cannot send arrow or forward-delete keys (it types their glyphs as text), and the edit
    /// menu's "Select All" is a label that depends on the language.
    ///
    /// The result is verified: the field is read again and the call answers `false` unless it is empty. Nothing is retried
    /// and there is no second strategy: a field that keeps text after one delete key per character is reported as it is.
    func clearText(_ q: Query) -> Bool {
        injected = 0
        guard focus(q, primitive: "clearText"), let (resolved, facts) = resolver.placed(q, primitive: "clearText") else { return false }
        let held = facts.typedText
        if held.isEmpty { return true }

        let area = resolver.visiblePart(of: facts.frame)
        let end = CGPoint(x: area.minX + area.width * 0.75, y: area.midY)
        guard tapper(resolved, end, "clearText", "\(q) (caret to the end)") else { return false }
        guard typeInChunks(String(repeating: XCUIKeyboardKey.delete.rawValue, count: held.count), primitive: "clearText") else { return false }
        let left = typedTextAfterDeleting(q)
        if left?.isEmpty == true { return true }
        return refuse("clearText", "\(q): the field is not empty after deleting; it reads back \(left.map { "\($0.count) characters" } ?? "nothing")")
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
        guard resolver.isAppRunning else { return refuse("submitTextEntry", resolver.notRunningReason) }
        let keyboard = app.keyboards.firstMatch
        // Right after a touch that opens the keyboard it may not be there yet: wait for it as a condition (Timeouts.Action).
        guard keyboard.waitForExistence(timeout: Self.submitWait) else {
            return refuse("submitTextEntry", "no keyboard showed within \(Self.submitWait) s, so there is no action key to press")
        }

        for name in Self.actionKeys {
            let key = keyboard.buttons[name]
            guard key.exists else { continue }
            let (frame, reads) = settledFrame(of: key)
            log.add("[driver] submitTextEntry: key '\(name)' frame \(frame.map { "\($0)" } ?? "unreadable") after \(reads) reads")
            guard let frame else { return refuse("submitTextEntry", "the '\(name)' key never stopped moving or could not be read") }
            // A key of the keyboard is not tapped through the driver's guard, which refuses points under the keyboard.
            return guarded("submitTextEntry", "'\(name)' key", log: log) {
                resolver.coordinate(at: CGPoint(x: frame.midX, y: frame.midY), in: nil).tap()
            }
        }
        var offered: [String] = []
        _ = guarded("submitTextEntry", "keyboard button labels", log: log) { offered = keyboard.buttons.allElementsBoundByIndex.map { $0.label } }
        return refuse("submitTextEntry", "the keyboard has no action key among \(Self.actionKeys) (its buttons: \(offered))")
    }

    private func refuse(_ primitive: String, _ reason: String) -> Bool {
        log.refuse(primitive, reason)
        return false
    }

    /// The frame of [element] once it reads the same three times in a row; nil if it cannot be read or never stops.
    private func settledFrame(of element: XCUIElement) -> (CGRect?, Int) {
        var watch = SettleWatch()
        let began = Monotonic.now
        var frame: CGRect?
        var verdict: FrameVerdict?
        repeat {
            do {
                frame = try element.snapshot().frame
                verdict = watch.feed(frame, at: Monotonic.now)
            } catch {
                // The key was found a moment ago: a read that fails is not the key going away, it is a read that did not
                // happen. It does not settle the frame and it does not end the watch; the reads and the time limit do.
                frame = nil
                log.tolerate(.keyReadFailed, "submitTextEntry: read \(watch.reads + 1) of the key failed after \(String(format: "%.2f", Monotonic.now - began)) s: \(error)")
                verdict = watch.feedUnreadable(at: Monotonic.now)
            }
            if verdict == nil { Thread.sleep(forTimeInterval: Self.pollInterval) }
        } while verdict == nil
        return (verdict == .settled ? frame : nil, watch.reads)
    }

    /// Taps the field once so that it takes the focus and waits for the keyboard. There is no second tap: with the frame settled
    /// before the tap (and the keyboard guard) it was never needed in 196 conformance runs (0 second taps), and a field that does
    /// not bring the keyboard up is reported, not tapped again.
    private func focus(_ q: Query, primitive: String) -> Bool {
        guard let (resolved, facts) = resolver.placed(q, primitive: primitive) else { return false }
        // XCUITest reports no keyboard focus for a text field (`hasFocus` read false with the keyboard up, measured in the
        // driver contract), so the field cannot be told apart from an unfocused one: it is touched once, as before.
        guard tapper(resolved, center(of: facts), primitive, "\(q) (focus)") else { return false }
        if waitForKeyboard() { return true }
        log.refuse(primitive, "\(q): no keyboard \(Self.keyboardTimeout) s after the focus tap")
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

    private func typeInChunks(_ text: String, primitive: String) -> Bool {
        var rest = Substring(text)
        while !rest.isEmpty {
            guard resolver.isAppRunning else { return refuse(primitive, "typeText: the app stopped running after \(injected) characters") }
            guard app.keyboards.firstMatch.exists else { return refuse(primitive, "typeText: the keyboard went away after \(injected) characters") }
            let chunk = rest.prefix(Self.chunk)
            guard guarded(primitive, "typeText", log: log, { app.typeText(String(chunk)) }) else { return false }
            injected += chunk.count
            rest = rest.dropFirst(chunk.count)
        }
        return true
    }
}
