import XCTest
import ScenarioKit

/// Text entry through the software keyboard. The driver never retypes and never judges: it types
/// what it is told and the interpreter re-reads the field. `app.typeText` is used instead of
/// `element.typeText` so XCTest's element-focus assertion cannot fire; before every call the
/// keyboard is confirmed on screen, which is the condition `typeText` asserts.
final class TextTyping {
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

    func setText(_ q: Query, text: String) -> Bool {
        guard let (resolved, facts) = resolver.visibleFacts(q) else { return false }
        UIPasteboard.general.string = text
        tapper(resolved, facts)
        let target = resolver.visiblePart(of: facts.frame)
        let point = CGPoint(x: target.midX, y: target.midY)
        resolver.coordinate(at: point, in: resolved).press(forDuration: 1.0)

        for label in ["Paste", "Pegar"] {
            let item = app.menuItems[label]
            if item.waitForExistence(timeout: 2) {
                item.tap()
                _ = SystemUi.acceptPasteAlert(springboard: resolver.springboard)
                return true
            }
        }
        return false
    }

    func clearText(_ q: Query) -> Bool {
        guard focus(q) else { return false }
        guard let (_, facts) = resolver.visibleFacts(q) else { return false }
        let count = facts.typedText.count
        if count == 0 { return true }

        // The tap may have put the caret anywhere, so delete in both directions.
        let backward = String(repeating: XCUIKeyboardKey.delete.rawValue, count: count)
        let forward = String(repeating: XCUIKeyboardKey.forwardDelete.rawValue, count: count)
        return typeInChunks(forward) && typeInChunks(backward)
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
        guard let (resolved, facts) = resolver.visibleFacts(q) else { return false }
        tapper(resolved, facts)
        if waitForKeyboard() { return true }

        // One more tap: the first can land while the field is still taking focus.
        tapper(resolved, facts)
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
