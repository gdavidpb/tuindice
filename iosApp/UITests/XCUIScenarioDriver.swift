import XCTest
import ScenarioKit

/// ScenarioKit's driver contract on XCUITest. Every call is blocking, answers `false` or nil instead
/// of failing, and never lets XCTest record an issue of its own: elements are looked up only while
/// the app runs and read through snapshots, which throw when there is nothing to read.
///
/// A gesture goes only to an element whose frame is still and, when it is a tap, to a point the on-screen
/// keyboard does not cover; otherwise it is refused with a line in the driver log that says which of the two.
final class XCUIScenarioDriver: NSObject, ScenarioDriver {
    private static let pollInterval = 0.1
    private static let launchTimeout = 30.0
    private static let edgeMargin: CGFloat = 12

    private let config: RunConfig
    private let app = XCUIApplication()
    private let resolver: ElementResolver
    private let backend: HttpBackend
    private let log: DriverLog
    private var typing: TextTyping!
    private(set) var failureAttachments: [XCTAttachment] = []

    init(config: RunConfig, log: DriverLog) {
        self.config = config
        self.log = log
        resolver = ElementResolver(app: app, log: log)
        backend = HttpBackend(baseUrl: config.wiremockUrl)
        super.init()
        typing = TextTyping(resolver: resolver, log: log) { [unowned self] resolved, point, gesture in
            tap(resolved, at: point, gesture: gesture)
        }
    }

    // MARK: AppControl

    func launch(spec: LaunchSpec) -> Bool { traced("launch") { launchApp(spec) } }

    private func launchApp(_ spec: LaunchSpec) -> Bool {
        app.terminate()

        var environment = spec.arguments
        environment[LaunchKeys.apiBaseUrl] = config.apiBaseUrl
        environment[LaunchKeys.webBaseUrl] = config.webBaseUrl
        app.launchEnvironment = environment
        app.launch()

        return app.wait(for: .runningForeground, timeout: Self.launchTimeout)
    }

    func foreground() -> Bool {
        traced("foreground") {
            guard resolver.isAppRunning else { return false }
            app.activate()
            return app.wait(for: .runningForeground, timeout: Self.launchTimeout)
        }
    }

    func isForeground() -> Bool { traced("isForeground") { app.state == .runningForeground } }

    func terminate() { traced("terminate") { app.terminate() } }

    // MARK: ElementProbe

    func waitVisible(q: Query, timeoutMs: Int64) -> Bool {
        traced("waitVisible") { poll(timeoutMs: timeoutMs) { resolver.visibleFacts(q) != nil } }
    }

    func waitGone(q: Query, timeoutMs: Int64) -> Bool {
        traced("waitGone") { poll(timeoutMs: timeoutMs) { resolver.isAbsent(q) } }
    }

    func isVisible(q: Query) -> Bool { traced("isVisible") { resolver.visibleFacts(q) != nil } }

    func isEnabled(q: Query) -> Bool { traced("isEnabled") { resolver.visibleFacts(q)?.1.isEnabled ?? false } }

    func readText(q: Query) -> String? {
        traced("readText") {
            guard let (_, facts) = resolver.visibleFacts(q) else { return nil }
            if facts.isTextInput { return facts.typedText }
            return facts.value ?? facts.label
        }
    }

    func bounds(q: Query?) -> ElementBounds? {
        traced("bounds") {
            guard let q else { return rectBounds(resolver.screen) }
            guard let (_, facts) = resolver.visibleFacts(q) else { return nil }
            return rectBounds(resolver.visiblePart(of: facts.frame))
        }
    }

    // MARK: Gestures

    func tap(q: Query) -> Bool {
        traced("tap") {
            guard let (resolved, facts) = resolver.placed(q, for: "tap") else { return false }
            let target = resolver.visiblePart(of: facts.frame)
            return tap(resolved, at: CGPoint(x: target.midX, y: target.midY), gesture: "tap \(q)")
        }
    }

    func tapAt(q: Query?, fx: Double, fy: Double) -> Bool {
        traced("tapAt") {
            guard resolver.isAppRunning else { return false }
            guard let (resolved, area) = area(of: q, for: "tapAt") else { return false }
            let point = CGPoint(x: area.minX + area.width * fx, y: area.minY + area.height * fy)
            guard !coveredByKeyboard(point, gesture: "tapAt \(q.map { "\($0)" } ?? "screen")") else { return false }
            resolver.coordinate(at: point, in: resolved).tap()
            return true
        }
    }

    func doubleTap(q: Query) -> Bool {
        traced("doubleTap") {
            guard resolver.isAppRunning, let (resolved, facts) = resolver.placed(q, for: "doubleTap") else { return false }
            let target = resolver.visiblePart(of: facts.frame)
            let point = CGPoint(x: target.midX, y: target.midY)
            guard !coveredByKeyboard(point, gesture: "doubleTap \(q)") else { return false }
            resolver.coordinate(at: point, in: resolved).doubleTap()
            return true
        }
    }

    func swipe(from: Query?, vector: SwipeVector, durationMs: Int64) -> Bool {
        traced("swipe") { performSwipe(from: from, vector: vector, durationMs: durationMs) }
    }

    private func performSwipe(from: Query?, vector: SwipeVector, durationMs: Int64) -> Bool {
        guard resolver.isAppRunning, let (resolved, area) = area(of: from, for: "swipe") else { return false }
        let screen = resolver.screen
        let start = CGPoint(x: area.minX + area.width * vector.fx, y: area.minY + area.height * vector.fy)
        let end = CGPoint(
            x: min(max(start.x + screen.width * vector.dx, Self.edgeMargin), screen.width - Self.edgeMargin),
            y: min(max(start.y + screen.height * vector.dy, Self.edgeMargin), screen.height - Self.edgeMargin)
        )
        let seconds = max(Double(durationMs) / 1000.0, 0.05)
        let distance = hypot(end.x - start.x, end.y - start.y)
        let velocity = XCUIGestureVelocity(CGFloat(distance / seconds))

        // The finger lifts at the speed of the drag, as on Android: the gesture may fling its content.
        resolver.coordinate(at: start, in: resolved).press(
            forDuration: 0.05,
            thenDragTo: resolver.coordinate(at: end, in: resolved),
            withVelocity: velocity,
            thenHoldForDuration: 0
        )
        return true
    }

    func pressBack() -> Bool { traced("pressBack") { false } }

    // MARK: TextEntry

    func typeKeys(q: Query, text: String) -> Bool { traced("typeKeys") { typing.typeKeys(q, text: text) } }

    func clearText(q: Query) -> Bool { traced("clearText") { typing.clearText(q) } }

    func finishTextEntry() -> Bool { traced("finishTextEntry") { typing.finishTextEntry() } }

    // MARK: BackendControl

    func http(method: String, path: String, body: String?, authorization: String?) -> HttpReply {
        traced("http") { backend.http(method: method, path: path, body: body, authorization: authorization) }
    }

    // MARK: Diagnostics

    var platform: Platform { Platform.ios }

    func log(line: String) {
        log.add(line)
    }

    func pause(ms: Int64) {
        traced("pause") { Thread.sleep(forTimeInterval: Double(max(ms, 0)) / 1000.0) }
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

    /// With `E2E_TRACE`, adds one `[driver] <call> <microseconds>` line to the driver log for each
    /// call the interpreter makes. The interpreter logs `[<index>] ...` right after each step, so
    /// the calls between two such lines belong to the step the second one names. Calls the driver
    /// makes to itself are not traced again: only the entry points above are.
    private func traced<T>(_ name: String, _ body: () -> T) -> T {
        guard config.trace else { return body() }
        let start = DispatchTime.now().uptimeNanoseconds
        let result = body()
        let micros = (DispatchTime.now().uptimeNanoseconds - start) / 1_000
        log.add("[driver] \(name) \(micros)")
        return result
    }

    /// A coordinate tap at [point] (the centre of the visible part for a plain tap): no hittability assertion, and no dependence on the
    /// synthetic child Compose adds under a tagged element. A point under the on-screen keyboard is not tapped.
    private func tap(_ resolved: ResolvedElement, at point: CGPoint, gesture: String) -> Bool {
        guard resolver.isAppRunning else { return false }
        guard !coveredByKeyboard(point, gesture: gesture) else { return false }
        resolver.coordinate(at: point, in: resolved).tap()
        return true
    }

    /// Whether the keyboard on screen covers [point]: a tap there would press a key and put a character in the field that has
    /// the focus. Says so in the driver log. (A key of the keyboard itself is not tapped through here: the driver's own paths
    /// to it, `typeText` and the action key of `finishTextEntry`, do not use a point of the screen.)
    private func coveredByKeyboard(_ point: CGPoint, gesture: String) -> Bool {
        guard let keyboard = resolver.keyboardFrame, keyboard.contains(point) else { return false }
        log.add("[driver] \(gesture): the point \(point) is inside the keyboard on screen \(keyboard); tap refused")
        return true
    }

    /// The place a gesture on [q] goes to: the whole screen without a query, otherwise the settled part of the element.
    private func area(of q: Query?, for gesture: String) -> (ResolvedElement?, CGRect)? {
        guard let q else { return (nil, resolver.screen) }
        guard let (resolved, facts) = resolver.placed(q, for: gesture) else { return nil }
        return (resolved, resolver.visiblePart(of: facts.frame))
    }

    private func rectBounds(_ rect: CGRect) -> ElementBounds {
        ElementBounds(left: Double(rect.minX), top: Double(rect.minY), right: Double(rect.maxX), bottom: Double(rect.maxY))
    }

    /// Polls [check] until it holds or [timeoutMs] pass (on the monotonic clock); always checks at least once.
    private func poll(timeoutMs: Int64, _ check: () -> Bool) -> Bool {
        let deadline = Monotonic.now + Double(max(timeoutMs, 0)) / 1000.0
        repeat {
            if check() { return true }
            if Monotonic.now >= deadline { return false }
            Thread.sleep(forTimeInterval: Self.pollInterval)
        } while true
    }
}
