import XCTest
import ScenarioKit

/// ScenarioKit's driver contract on XCUITest. Every call is blocking, answers `false` or nil instead
/// of failing, and never lets XCTest record an issue of its own: elements are looked up only while
/// the app runs, only after `exists`, and read through snapshots.
final class XCUIScenarioDriver: NSObject, ScenarioDriver {
    private static let pollInterval = 0.1
    private static let launchTimeout = 30.0
    private static let edgeMargin: CGFloat = 12

    private let config: RunConfig
    private let app = XCUIApplication()
    private let resolver: ElementResolver
    private let backend: HttpBackend
    private var typing: TextTyping!
    private(set) var failureAttachments: [XCTAttachment] = []
    private(set) var logLines: [String] = []

    init(config: RunConfig) {
        self.config = config
        resolver = ElementResolver(app: app)
        backend = HttpBackend(baseUrl: config.wiremockUrl)
        super.init()
        typing = TextTyping(resolver: resolver, config: config) { [unowned self] resolved, facts in
            _ = tap(resolved, facts)
        }
    }

    // MARK: AppControl

    func launch(spec: LaunchSpec) -> Bool {
        app.terminate()

        var environment = spec.arguments
        environment[LaunchKeys.apiBaseUrl] = config.apiBaseUrl
        environment[LaunchKeys.webBaseUrl] = config.webBaseUrl
        app.launchEnvironment = environment
        app.launch()

        return app.wait(for: .runningForeground, timeout: Self.launchTimeout)
    }

    func foreground() -> Bool {
        guard resolver.isAppRunning else { return false }
        app.activate()
        return app.wait(for: .runningForeground, timeout: Self.launchTimeout)
    }

    func isForeground() -> Bool { app.state == .runningForeground }

    func terminate() { app.terminate() }

    // MARK: ElementProbe

    func waitVisible(q: Query, timeoutMs: Int64) -> Bool {
        poll(timeoutMs: timeoutMs) { resolver.visibleFacts(q) != nil }
    }

    func waitGone(q: Query, timeoutMs: Int64) -> Bool {
        poll(timeoutMs: timeoutMs) { resolver.visibleFacts(q) == nil }
    }

    func isVisible(q: Query) -> Bool { resolver.visibleFacts(q) != nil }

    func isEnabled(q: Query) -> Bool { resolver.visibleFacts(q)?.1.isEnabled ?? false }

    func readText(q: Query) -> String? {
        guard let (_, facts) = resolver.visibleFacts(q) else { return nil }
        if facts.isTextInput { return facts.typedText }
        return facts.value ?? facts.label
    }

    func bounds(q: Query?) -> ElementBounds? {
        guard let q else { return rectBounds(resolver.screen) }
        guard let (_, facts) = resolver.visibleFacts(q) else { return nil }
        return rectBounds(resolver.visiblePart(of: facts.frame))
    }

    // MARK: Gestures

    func tap(q: Query) -> Bool {
        guard let (resolved, facts) = resolver.visibleFacts(q) else { return false }
        return tap(resolved, facts)
    }

    func tapAt(q: Query?, fx: Double, fy: Double) -> Bool {
        guard resolver.isAppRunning else { return false }
        guard let (resolved, area) = area(of: q) else { return false }
        let point = CGPoint(x: area.minX + area.width * fx, y: area.minY + area.height * fy)
        resolver.coordinate(at: point, in: resolved).tap()
        return true
    }

    func doubleTap(q: Query) -> Bool {
        guard let (resolved, facts) = resolver.visibleFacts(q) else { return false }
        let target = resolver.visiblePart(of: facts.frame)
        resolver.coordinate(at: CGPoint(x: target.midX, y: target.midY), in: resolved).doubleTap()
        return true
    }

    func swipe(from: Query?, vector: SwipeVector, durationMs: Int64) -> Bool {
        guard resolver.isAppRunning, let (resolved, area) = area(of: from) else { return false }
        let screen = resolver.screen
        let start = CGPoint(x: area.minX + area.width * vector.fx, y: area.minY + area.height * vector.fy)
        let end = CGPoint(
            x: min(max(start.x + screen.width * vector.dx, Self.edgeMargin), screen.width - Self.edgeMargin),
            y: min(max(start.y + screen.height * vector.dy, Self.edgeMargin), screen.height - Self.edgeMargin)
        )
        let seconds = max(Double(durationMs) / 1000.0, 0.05)
        let distance = hypot(end.x - start.x, end.y - start.y)
        let velocity = XCUIGestureVelocity(CGFloat(distance / seconds))

        // The trailing hold removes fling inertia, so a swipe moves content by the drag only.
        resolver.coordinate(at: start, in: resolved).press(
            forDuration: 0.05,
            thenDragTo: resolver.coordinate(at: end, in: resolved),
            withVelocity: velocity,
            thenHoldForDuration: 0.1
        )
        return true
    }

    func pressBack() -> Bool { false }

    // MARK: TextEntry

    func typeKeys(q: Query, text: String) -> Bool { typing.typeKeys(q, text: text) }

    func setText(q: Query, text: String) -> Bool { typing.setText(q, text: text) }

    func clearText(q: Query) -> Bool { typing.clearText(q) }

    func finishTextEntry() -> Bool { typing.finishTextEntry() }

    // MARK: BackendControl

    func http(method: String, path: String, body: String?, authorization: String?) -> HttpReply {
        backend.http(method: method, path: path, body: body, authorization: authorization)
    }

    // MARK: Diagnostics

    var platform: Platform { Platform.ios }

    func log(line: String) {
        logLines.append(line)
        if config.trace { print("[scenario] \(line)") }
    }

    func pause(ms: Int64) {
        Thread.sleep(forTimeInterval: Double(max(ms, 0)) / 1000.0)
    }

    func captureFailure(scenarioId: String, stepIndex: Int32) {
        let screenshot = XCTAttachment(screenshot: XCUIScreen.main.screenshot())
        screenshot.name = "\(scenarioId)-step\(stepIndex)-screenshot"
        screenshot.lifetime = .keepAlways
        failureAttachments.append(screenshot)

        let hierarchy = XCTAttachment(string: resolver.isAppRunning ? app.debugDescription : "the app is not running")
        hierarchy.name = "\(scenarioId)-step\(stepIndex)-hierarchy"
        hierarchy.lifetime = .keepAlways
        failureAttachments.append(hierarchy)

        if let dialog = systemDialogInFront() {
            let note = XCTAttachment(string: dialog)
            note.name = "\(scenarioId)-step\(stepIndex)-system-dialog"
            note.lifetime = .keepAlways
            failureAttachments.append(note)
        }
    }

    func systemDialogInFront() -> String? {
        SystemUi.dialogInFront(springboard: resolver.springboard)
    }

    // MARK: Helpers

    private func tap(_ resolved: ResolvedElement, _ facts: ElementFacts) -> Bool {
        guard resolver.isAppRunning else { return false }
        // A coordinate tap at the centre of the visible part: no hittability assertion, and no
        // dependence on the synthetic child Compose adds under a tagged element.
        let target = resolver.visiblePart(of: facts.frame)
        resolver.coordinate(at: CGPoint(x: target.midX, y: target.midY), in: resolved).tap()
        return true
    }

    private func area(of q: Query?) -> (ResolvedElement?, CGRect)? {
        guard let q else { return (nil, resolver.screen) }
        guard let (resolved, facts) = resolver.visibleFacts(q) else { return nil }
        return (resolved, resolver.visiblePart(of: facts.frame))
    }

    private func rectBounds(_ rect: CGRect) -> ElementBounds {
        ElementBounds(left: Double(rect.minX), top: Double(rect.minY), right: Double(rect.maxX), bottom: Double(rect.maxY))
    }

    /// Polls [check] until it holds or [timeoutMs] pass; always checks at least once.
    private func poll(timeoutMs: Int64, _ check: () -> Bool) -> Bool {
        let deadline = Date().addingTimeInterval(Double(max(timeoutMs, 0)) / 1000.0)
        repeat {
            if check() { return true }
            if Date() >= deadline { return false }
            Thread.sleep(forTimeInterval: Self.pollInterval)
        } while true
    }
}
