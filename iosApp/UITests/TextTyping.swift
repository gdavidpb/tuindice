import XCTest
import ScenarioKit

/// Text entry through the software keyboard. The driver never retypes and never judges: it types
/// what it is told and the interpreter re-reads the field. `app.typeText` is used instead of
/// `element.typeText` so XCTest's element-focus assertion cannot fire; before every call the
/// keyboard is confirmed on screen, which is the condition `typeText` asserts.
final class TextTyping {
    /// Characters per `typeText` call (F13 measured it: 4).
    private static let chunk = 4
    private static let pasteTimeout = 5.0
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

    /// Pastes through the edit menu. The field is focused first and its place is read again afterwards:
    /// the keyboard moves the form, so the place it had before the tap is another field's. The menu
    /// item is the only one the long press shows on an empty field, so it is taken by position, not by
    /// label. The permission alert of a cross-app paste may or may not appear, and comes after the tap:
    /// it is looked for until the field holds text or [pasteTimeout] passes.
    func setText(_ q: Query, text: String) -> Bool {
        guard focus(q), let (resolved, facts) = resolver.placed(q, for: "setText") else { return false }
        UIPasteboard.general.string = text
        let target = resolver.visiblePart(of: facts.frame)
        resolver.coordinate(at: CGPoint(x: target.midX, y: target.midY), in: resolved).press(forDuration: 1.0)

        let item = app.menuItems.firstMatch
        guard item.waitForExistence(timeout: 2) else { return false }
        item.tap()

        let deadline = Monotonic.now + Self.pasteTimeout
        repeat {
            if let (_, now) = resolver.visibleFacts(q), !now.typedText.isEmpty { return true }
            if let answered = SystemUi.allowPaste(springboard: resolver.springboard) {
                log.tolerate(.allowedPaste, "the SpringBoard alert was answered with '\(answered)'")
            }
            Thread.sleep(forTimeInterval: Self.pollInterval)
        } while Monotonic.now < deadline
        return false
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

    /// Presses the keyboard's action key (Search, Return, Done...). The key is tapped once its frame is still, and what was
    /// seen is written to the driver log: the keyboard slides in, and a tap on a key that is still moving was suspected of
    /// being the one that did not close the keyboard.
    func finishTextEntry() -> Bool {
        guard resolver.isAppRunning else { return false }
        let keyboard = app.keyboards.firstMatch
        guard keyboard.exists else { return true }

        for name in Self.actionKeys {
            let key = keyboard.buttons[name]
            guard key.exists else { continue }
            let (frame, reads) = settledFrame(of: key)
            log.add("[driver] finishTextEntry: key '\(name)' frame \(frame.map { "\($0)" } ?? "unreadable") after \(reads) reads")
            guard let frame else { return false }
            resolver.coordinate(at: CGPoint(x: frame.midX, y: frame.midY), in: nil).tap()
            break
        }
        let closed = keyboard.waitForNonExistence(timeout: Self.keyboardTimeout)
        if !closed { log.add("[driver] finishTextEntry: the keyboard was still on screen \(Self.keyboardTimeout) s after the action key") }
        return closed
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

    /// Taps the field so that it takes the focus and waits for the keyboard. If the keyboard does not show, the field is tapped
    /// once more, and that is a tolerance: it is written to the driver log and counted.
    private func focus(_ q: Query) -> Bool {
        guard let (resolved, facts) = resolver.placed(q, for: "focus") else { return false }
        guard tapper(resolved, center(of: facts), "focus \(q)") else { return false }
        if waitForKeyboard() { return true }

        log.tolerate(.focusRetry, "\(q): no keyboard \(Self.keyboardTimeout) s after the focus tap; the field is tapped once more")
        guard let (again, place) = resolver.placed(q, for: "focus (second tap)") else { return false }
        guard tapper(again, center(of: place), "focus \(q) (second tap)") else { return false }
        return waitForKeyboard()
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
