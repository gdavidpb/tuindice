import XCTest
import ScenarioKit

/// Text entry through the software keyboard. The driver never retypes and never judges: it types
/// what it is told and the interpreter re-reads the field. `app.typeText` is used instead of
/// `element.typeText` so XCTest's element-focus assertion cannot fire; before every call the
/// keyboard is confirmed on screen, which is the condition `typeText` asserts.
final class TextTyping {
    private static let pasteTimeout = 5.0
    private static let pollInterval = 0.1
    private static let rereadTimeout = 1.0

    private let resolver: ElementResolver
    private let config: RunConfig
    private let tapper: (ResolvedElement, ElementFacts) -> Void
    private var app: XCUIApplication { resolver.app }

    init(resolver: ElementResolver, config: RunConfig, tapper: @escaping (ResolvedElement, ElementFacts) -> Void) {
        self.resolver = resolver
        self.config = config
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
        guard focus(q), let (resolved, facts) = resolver.settledFacts(q) else { return false }
        UIPasteboard.general.string = text
        let target = resolver.visiblePart(of: facts.frame)
        resolver.coordinate(at: CGPoint(x: target.midX, y: target.midY), in: resolved).press(forDuration: 1.0)

        let item = app.menuItems.firstMatch
        guard item.waitForExistence(timeout: 2) else { return false }
        item.tap()

        let deadline = Date().addingTimeInterval(Self.pasteTimeout)
        repeat {
            if let (_, now) = resolver.visibleFacts(q), !now.typedText.isEmpty { return true }
            _ = SystemUi.allowPaste(springboard: resolver.springboard)
            Thread.sleep(forTimeInterval: Self.pollInterval)
        } while Date() < deadline
        return false
    }

    /// A triple tap selects the field's whole content wherever the first tap put the caret, and one
    /// delete key removes the selection. No caret movement is needed: `typeText` cannot send arrow or
    /// forward-delete keys (it types their glyphs as text), and the edit menu's "Select All" is a label
    /// that depends on the language.
    ///
    /// The result is verified: the field is read again and the call answers `false` unless it is empty.
    /// A field that took its focus back on returning to its screen has been seen to ignore the selection,
    /// so the delete key removed one character only. When the field still holds text after the selection
    /// delete, the caret sits at the end of what is left (the tap was past the text) and one delete key
    /// per remaining character empties it; the field is read again once more. Nothing else is retried.
    func clearText(_ q: Query) -> Bool {
        guard focus(q), let (resolved, facts) = resolver.settledFacts(q) else { return false }
        if facts.typedText.isEmpty { return true }

        resolved.element.tap(withNumberOfTaps: 3, numberOfTouches: 1)
        guard typeInChunks(XCUIKeyboardKey.delete.rawValue) else { return false }

        guard let left = typedTextAfterDeleting(q) else { return false }
        if left.isEmpty { return true }
        guard typeInChunks(String(repeating: XCUIKeyboardKey.delete.rawValue, count: left.count)) else { return false }
        return typedTextAfterDeleting(q)?.isEmpty ?? false
    }

    /// What the field holds once the delete has been applied: read until two reads agree (the field
    /// redraws after a key), at most [rereadTimeout]; nil when the field cannot be read.
    private func typedTextAfterDeleting(_ q: Query) -> String? {
        var last = resolver.visibleFacts(q)?.1.typedText
        let deadline = Date().addingTimeInterval(Self.rereadTimeout)
        while Date() < deadline {
            Thread.sleep(forTimeInterval: Self.pollInterval)
            let now = resolver.visibleFacts(q)?.1.typedText
            if now == last { return now }
            last = now
        }
        return last
    }

    func finishTextEntry() -> Bool {
        guard resolver.isAppRunning else { return false }
        let keyboard = app.keyboards.firstMatch
        guard keyboard.exists else { return true }

        for name in ["Search", "Return", "Done", "Go", "Buscar", "Listo", "Ir"] {
            let key = keyboard.buttons[name]
            if key.exists {
                key.tap()
                break
            }
        }
        return keyboard.waitForNonExistence(timeout: 2)
    }

    private func focus(_ q: Query) -> Bool {
        guard let (resolved, facts) = resolver.settledFacts(q) else { return false }
        tapper(resolved, facts)
        if waitForKeyboard() { return true }

        // One more tap: the first can land while the field is still taking focus.
        guard let (again, place) = resolver.settledFacts(q) else { return false }
        tapper(again, place)
        return waitForKeyboard()
    }

    private func waitForKeyboard() -> Bool {
        guard resolver.isAppRunning else { return false }
        return app.keyboards.firstMatch.waitForExistence(timeout: 2)
    }

    private func typeInChunks(_ text: String) -> Bool {
        var rest = Substring(text)
        while !rest.isEmpty {
            guard resolver.isAppRunning, app.keyboards.firstMatch.exists else { return false }
            let chunk = rest.prefix(config.typeChunk)
            app.typeText(String(chunk))
            rest = rest.dropFirst(chunk.count)
        }
        return true
    }
}
