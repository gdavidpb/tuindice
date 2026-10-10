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

    /// The window XCTest gets to notice that the app left the front, from `Timeouts.ForegroundSettle`, in seconds.
    private static let settleWindow = Double(Timeouts.shared.ForegroundSettle) / 1000.0
    private(set) var failureAttachments: [XCTAttachment] = []

    init(config: RunConfig, log: DriverLog) {
        self.config = config
        self.log = log
        resolver = ElementResolver(app: app, log: log)
        backend = HttpBackend(baseUrl: config.wiremockUrl)
        super.init()
        typing = TextTyping(resolver: resolver, log: log) { [unowned self] resolved, point, primitive, target in
            tap(resolved, at: point, primitive: primitive, target: target)
        }
    }

    // MARK: AppControl

    func launch(spec: LaunchSpec) -> Bool { traced("launch") { launchApp(spec) } }

    private func launchApp(_ spec: LaunchSpec) -> Bool {
        guard guarded("launch", "terminate before launch", log: log, { app.terminate() }) else { return false }

        var environment = spec.arguments
        environment[LaunchKeys.apiBaseUrl] = config.apiBaseUrl
        environment[LaunchKeys.webBaseUrl] = config.webBaseUrl
        app.launchEnvironment = environment
        guard guarded("launch", "launch", log: log, { app.launch() }) else { return false }

        return app.wait(for: .runningForeground, timeout: Self.launchTimeout)
    }

    func foreground() -> Bool {
        traced("foreground") {
            log.clearRefusal()
            let state = app.state
            guard AppLife.canBeBroughtBack(state) else {
                return refuse("foreground", "the app is \(AppLife.describe(state)) (state \(state.rawValue)); it is not started again")
            }
            guard guarded("foreground", "activate", log: log, { app.activate() }) else { return false }
            // Not `app.wait(for: .runningForeground)`: it answers at once when the cached state still says foreground, with the
            // home screen or another app in front (measured: 15-40 ms). The app is in front when it also passes `isForeground`.
            let front = poll(timeoutMs: Int64(Self.launchTimeout * 1000)) { self.isForeground() }
            if !front { log.refuse("foreground", "the app was not in the foreground \(Self.launchTimeout) s after activate") }
            return front
        }
    }

    /// Whether the app is in front. `XCUIApplication.state` is a value XCTest refreshes lazily: with Safari opened on top of the
    /// app it stayed `runningForeground` for 10 s in every `conformance-foreground` run that followed other scenarios (and
    /// `foreground()` then answered in 15 ms with the home screen in front). Asking XCTest to wait for the background state is a
    /// round trip with the system, which sees the change as early as the cached value and no earlier (diff <= 0.03 s, measured:
    /// `ElementResolver.freshProbe`), so it is paid only here, where the polling converges, and not by the lookups; the one
    /// implementation is `ElementResolver.isAppFrontNow`, the same the proof of an absence uses. The limit of the cached state (about 2.7 s
    /// after Safari is in front) stays true for a call alone; [confirmForeground] is what keeps a scenario from passing through it.
    func isForeground() -> Bool { traced("isForeground") { resolver.isAppFrontNow } }

    /// Whether the app is in front once the system has had time to notice an exit (`AppControl.confirmForeground`). The limit of
    /// [isForeground] and of the lookups is real and stays: the state XCTest keeps turns to "not in front" about 2.7 s after another app
    /// is in front (2.56 to 2.89 s), and for that long a lookup can find the tree of the app with Safari in front. What this closes is
    /// its consequence: it waits the whole `Timeouts.ForegroundSettle` from the moment it is asked (`wait(for: .runningBackground)`, which
    /// ends as soon as the state changes), so an exit from before the question is seen whatever caused it. It answers false at once when
    /// the state already says the app is not in front. The only residue is an XCTest delay longer than the window.
    func confirmForeground() -> Bool {
        traced("confirmForeground") {
            guard app.state == .runningForeground else { return false }
            let began = Monotonic.now
            if app.wait(for: .runningBackground, timeout: Self.settleWindow) {
                log.add("[driver] confirmForeground: the app went to the background \(Monotonic.now - began) s after the question")
                return false
            }
            return app.state == .runningForeground
        }
    }

    /// False once the process is gone (`notRunning`); true when it is running in any way, suspended included, and when the
    /// state is unknown, which is not a death (see `AppControl.isRunning`).
    func isRunning() -> Bool { traced("isRunning") { app.state != .notRunning } }

    func terminate() { traced("terminate") { _ = guarded("terminate", "terminate", log: log) { app.terminate() } } }

    // MARK: ElementProbe

    func waitVisible(q: Query, timeoutMs: Int64) -> Bool {
        traced("waitVisible") { poll(timeoutMs: timeoutMs) { resolver.visibleFacts(q) != nil } }
    }

    func waitGone(q: Query, timeoutMs: Int64) -> Bool {
        traced("waitGone") {
            let gone = poll(timeoutMs: timeoutMs) { resolver.isAbsent(q) }
            if !gone { log.add("[driver] waitGone \(q): not gone after \(timeoutMs) ms (app state \(app.state.rawValue))") }
            return gone
        }
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

    /// A Compose `toggleable(role = Switch)` is published as a switch and a `toggleable(role = Checkbox)` as a `Button` with the
    /// `Selected` trait when it is on. So a button answers too, and then "unchecked" cannot be told from "not a toggle at all": any
    /// button without the trait reads `false`. That is why a scenario that asserts a checked state asserts both states of the same
    /// element (the state before and the state after), and the contract probes the limit (`ToggleState`).
    func isChecked(q: Query) -> KotlinBoolean? {
        traced("isChecked") {
            guard let (_, facts) = resolver.visibleFacts(q), ToggleState.holdsState(facts.type) else { return nil }
            guard let checked = ToggleState.checked(value: facts.value, isSelected: facts.isSelected) else {
                log.add("[driver] isChecked \(q): the value '\(facts.value ?? "")' is not one the driver knows how to read; no answer")
                return nil
            }
            return KotlinBoolean(bool: checked)
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
            log.clearRefusal()
            guard let (resolved, facts) = resolver.placed(q, primitive: "tap") else { return false }
            let target = resolver.visiblePart(of: facts.frame)
            return tap(resolved, at: CGPoint(x: target.midX, y: target.midY), primitive: "tap", target: "\(q)")
        }
    }

    func tapAt(q: Query?, fx: Double, fy: Double) -> Bool {
        traced("tapAt") {
            log.clearRefusal()
            guard resolver.isAppRunning else { return refuse("tapAt", resolver.notRunningReason) }
            guard let (resolved, area) = area(of: q, primitive: "tapAt") else { return false }
            let point = CGPoint(x: area.minX + area.width * fx, y: area.minY + area.height * fy)
            guard !coveredByKeyboard(point, gesture: "tapAt \(q.map { "\($0)" } ?? "screen")") else { return false }
            return guarded("tapAt", "tapAt", log: log) { resolver.coordinate(at: point, in: resolved).tap() }
        }
    }

    func doubleTap(q: Query) -> Bool {
        traced("doubleTap") {
            log.clearRefusal()
            guard resolver.isAppRunning else { return refuse("doubleTap", resolver.notRunningReason) }
            guard let (resolved, facts) = resolver.placed(q, primitive: "doubleTap") else { return false }
            let target = resolver.visiblePart(of: facts.frame)
            let point = CGPoint(x: target.midX, y: target.midY)
            guard !coveredByKeyboard(point, gesture: "doubleTap \(q)") else { return false }
            return guarded("doubleTap", "doubleTap", log: log) { resolver.coordinate(at: point, in: resolved).doubleTap() }
        }
    }

    func swipe(from: Query?, vector: SwipeVector, durationMs: Int64) -> Bool {
        traced("swipe") {
            log.clearRefusal()
            return performSwipe(from: from, vector: vector, durationMs: durationMs)
        }
    }

    private func performSwipe(from: Query?, vector: SwipeVector, durationMs: Int64) -> Bool {
        guard resolver.isAppRunning else { return refuse("swipe", resolver.notRunningReason) }
        guard let (resolved, area) = area(of: from, primitive: "swipe") else { return false }
        let screen = resolver.screen
        let start = CGPoint(x: area.minX + area.width * vector.fx, y: area.minY + area.height * vector.fy)
        let end = CGPoint(
            x: min(max(start.x + screen.width * vector.dx, Self.edgeMargin), screen.width - Self.edgeMargin),
            y: min(max(start.y + screen.height * vector.dy, Self.edgeMargin), screen.height - Self.edgeMargin)
        )
        guard !coveredByKeyboard(start, gesture: "swipe from \(from.map { "\($0)" } ?? "the screen")") else { return false }
        let seconds = max(Double(durationMs) / 1000.0, 0.05)
        let distance = hypot(end.x - start.x, end.y - start.y)
        let velocity = XCUIGestureVelocity(CGFloat(distance / seconds))

        // The finger lifts at the speed of the drag, as on Android: the gesture may fling its content.
        return guarded("swipe", "swipe", log: log) {
            resolver.coordinate(at: start, in: resolved).press(
                forDuration: 0.05,
                thenDragTo: resolver.coordinate(at: end, in: resolved),
                withVelocity: velocity,
                thenHoldForDuration: 0
            )
        }
    }

    func pressBack() -> Bool {
        traced("pressBack") {
            log.clearRefusal()
            return refuse("pressBack", "iOS has no system back action")
        }
    }

    // MARK: TextEntry

    func keysInjected() -> Int32 { Int32(typing.injected) }

    func typeKeys(q: Query, text: String) -> Bool {
        traced("typeKeys") {
            log.clearRefusal()
            return typing.typeKeys(q, text: text)
        }
    }

    func clearText(q: Query) -> Bool {
        traced("clearText") {
            log.clearRefusal()
            return typing.clearText(q)
        }
    }

    func submitTextEntry() -> Bool {
        traced("submitTextEntry") {
            log.clearRefusal()
            return typing.submitTextEntry()
        }
    }

    // MARK: BackendControl

    func http(method: String, path: String, body: String?, authorization: String?) -> HttpReply {
        traced("http") { backend.http(method: method, path: path, body: body, authorization: authorization) }
    }

    // MARK: Diagnostics

    var platform: Platform { Platform.ios }

    func log(line: String) {
        log.add(line)
    }

    func lastRefusal() -> String? { log.lastRefusal }

    func pause(ms: Int64) {
        traced("pause") { Thread.sleep(forTimeInterval: Double(max(ms, 0)) / 1000.0) }
    }

    func captureFailure(scenarioId: String, stepIndex: Int32) {
        var image: XCUIScreenshot?
        if guarded("captureFailure", "screenshot", log: log, { image = XCUIScreen.main.screenshot() }), let image {
            let screenshot = XCTAttachment(screenshot: image)
            screenshot.name = "\(scenarioId)-step\(stepIndex)-screenshot"
            screenshot.lifetime = .keepAlways
            failureAttachments.append(screenshot)
        }

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
    private func tap(_ resolved: ResolvedElement, at point: CGPoint, primitive: String, target: String) -> Bool {
        guard resolver.isAppRunning else { return refuse(primitive, "\(target): \(resolver.notRunningReason)") }
        guard !coveredByKeyboard(point, gesture: "\(primitive) \(target)") else { return false }
        return guarded(primitive, target, log: log) { resolver.coordinate(at: point, in: resolved).tap() }
    }

    /// Whether a tap at [point] must not be made because of the keyboard on screen: it would press a key and put a character in
    /// the field that has the focus. Says so in the driver log, as a `guard` refusal. A keyboard that exists and cannot be read
    /// refuses too: whether the point is under it is not known, and a touch that might press a key is not made on a guess. After
    /// either refusal the keyboard is read once more and what it says goes to the log, so a refusal that came from a bad read
    /// can be told from one that did not. (A key of the keyboard itself is not tapped through here: `typeText` types without a
    /// point of the screen, and the action key of `submitTextEntry` is tapped by coordinate on purpose, outside this guard.)
    private func coveredByKeyboard(_ point: CGPoint, gesture: String) -> Bool {
        switch resolver.keyboard {
        case .none:
            return false
        case let .frame(keyboard):
            guard keyboard.contains(point) else { return false }
            log.refuse("guard", "\(gesture): the point \(point) is inside the keyboard on screen \(keyboard); tap refused")
        case .unreadable:
            log.refuse("guard", "\(gesture): a keyboard is on screen and could not be read, so it is not known whether the point \(point) is under it; tap refused")
        }
        log.add("[driver] guard: the keyboard read again after that refusal: \(resolver.keyboard)")
        return true
    }

    /// Writes the refusal of [primitive] to the driver log as the reason of the call that is about to answer `false`, and answers it.
    private func refuse(_ primitive: String, _ reason: String) -> Bool {
        log.refuse(primitive, reason)
        return false
    }

    /// The place a gesture on [q] goes to: the whole screen without a query, otherwise the settled part of the element.
    private func area(of q: Query?, primitive: String) -> (ResolvedElement?, CGRect)? {
        guard let q else { return (nil, resolver.screen) }
        guard let (resolved, facts) = resolver.placed(q, primitive: primitive) else { return nil }
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
