import XCTest
import ScenarioKit

/// Probes the iOS driver with the catalog's contract fixture, the way `DriverContractTest` does on
/// Android, before scenarios are trusted on it. Failures are reported by the same path as scenarios.
final class DriverContractTests: ScenarioTestCase {
    func test_driver_contract() {
        let config = RunConfig.shared
        // Like a scenario: the log is on disk line by line, so a contract run that hangs still leaves its `driver.log`.
        if let outputDir = config.outputDir {
            driverLog.open(at: URL(fileURLWithPath: outputDir).appendingPathComponent("driver-contract").appendingPathComponent("driver.log"))
        }
        let driver = XCUIScenarioDriver(config: config, log: driverLog)
        let outcome = ScenarioRunner.shared.driverContract(catalogJson: config.catalogJson, driver: driver)
        report(outcome, driver: driver)
    }
}

/// The rule that decides when a frame has stopped moving, on sequences of frames (B-6). No device is involved.
final class SettleWatchTests: XCTestCase {
    private let a = CGRect(x: 0, y: 100, width: 50, height: 20)
    private let b = CGRect(x: 0, y: 110, width: 50, height: 20)
    private let c = CGRect(x: 0, y: 120, width: 50, height: 20)

    private func run(_ frames: [CGRect?], watch: SettleWatch = SettleWatch(), step: Double = 0.1) -> (FrameVerdict?, Int) {
        var watch = watch
        var verdict: FrameVerdict?
        for (index, frame) in frames.enumerated() {
            verdict = watch.feed(frame, at: Double(index) * step)
            if verdict != nil { break }
        }
        return (verdict, watch.reads)
    }

    func test_three_equal_reads_settle() {
        XCTAssertEqual(run([a, a, a]).0, .settled)
        XCTAssertNil(run([a, a]).0, "two equal reads are not enough: a decelerating list repeats a frame")
    }

    func test_a_frame_that_changes_starts_the_count_again() {
        let (verdict, reads) = run([a, a, b, b, b])
        XCTAssertEqual(verdict, .settled)
        XCTAssertEqual(reads, 5)
        XCTAssertNil(run([a, a, b, b]).0)
    }

    func test_an_element_that_vanishes_is_gone_not_settled() {
        XCTAssertEqual(run([a, a, nil]).0, .vanished)
        XCTAssertEqual(run([a, b, nil]).0, .vanished)
    }

    func test_a_frame_that_never_stops_is_moving_after_the_reads_run_out() {
        let frames = (0..<30).map { CGRect(x: 0, y: CGFloat($0), width: 1, height: 1) }
        let (verdict, reads) = run(frames, watch: SettleWatch(requiredEqualReads: 3, maxReads: 20, timeLimit: 100))
        XCTAssertEqual(verdict, .moving)
        XCTAssertEqual(reads, 20)
    }

    func test_slow_reads_end_by_time_as_moving_but_never_as_settled_by_hope() {
        let (verdict, reads) = run([a, b, c, a, b], watch: SettleWatch(requiredEqualReads: 3, maxReads: 20, timeLimit: 1), step: 0.6)
        XCTAssertEqual(verdict, .moving)
        XCTAssertEqual(reads, 3, "the third read is at 1.2 s, past the 1 s limit")
    }

    func test_the_proof_of_stillness_wins_over_the_limits_on_the_same_read() {
        XCTAssertEqual(run([a, a, a], watch: SettleWatch(requiredEqualReads: 3, maxReads: 3, timeLimit: 100), step: 1).0, .settled)
    }

    // The pure decisions of the driver live beside the rule of stillness because the verb that runs this class is the one that
    // runs them (`PROBE_CLASSES` of `ios/adapter.sh`); none of them needs a device.

    /// ZB-11/ΔB-9: "gone" needs positive evidence. Only a miss needs the tree read and the app still in front.
    func test_absence_needs_a_tree_that_was_read_and_an_app_that_stayed_in_front() {
        XCTAssertTrue(AbsenceRule.isAbsent(.nothing, treeRead: { true }, stillInForeground: { true }))
        XCTAssertFalse(AbsenceRule.isAbsent(.nothing, treeRead: { false }, stillInForeground: { true }), "a tree that cannot be read proves nothing")
        XCTAssertFalse(AbsenceRule.isAbsent(.nothing, treeRead: { true }, stillInForeground: { false }), "an app that left in the meantime proves nothing")
        XCTAssertFalse(AbsenceRule.isAbsent(.unreadable, treeRead: { true }, stillInForeground: { true }), "an element that exists and cannot be read is not gone")
        XCTAssertFalse(AbsenceRule.isAbsent(.onScreen, treeRead: { true }, stillInForeground: { true }))
        XCTAssertTrue(AbsenceRule.isAbsent(.offScreen, treeRead: { false }, stillInForeground: { false }), "an element that exists off the screen is not shown")
    }

    func test_absence_reads_the_tree_only_after_a_miss() {
        var reads = 0
        _ = AbsenceRule.isAbsent(.onScreen, treeRead: { reads += 1; return true }, stillInForeground: { true })
        _ = AbsenceRule.isAbsent(.offScreen, treeRead: { reads += 1; return true }, stillInForeground: { true })
        _ = AbsenceRule.isAbsent(.unreadable, treeRead: { reads += 1; return true }, stillInForeground: { true })
        XCTAssertEqual(reads, 0)
    }

    /// K-3/ZB-9: a Compose checkbox is a button with the `Selected` trait; a value that is present and unknown is no answer.
    func test_a_checkbox_is_read_from_the_selected_trait_and_a_known_value() {
        XCTAssertEqual(ToggleState.checked(value: nil, isSelected: true), true)
        XCTAssertEqual(ToggleState.checked(value: nil, isSelected: false), false)
        XCTAssertEqual(ToggleState.checked(value: "", isSelected: true), true, "an empty value is no value")
        XCTAssertEqual(ToggleState.checked(value: "1", isSelected: false), true)
        XCTAssertEqual(ToggleState.checked(value: "On", isSelected: false), true)
        XCTAssertEqual(ToggleState.checked(value: "0", isSelected: true), false)
        XCTAssertEqual(ToggleState.checked(value: "unchecked", isSelected: true), false)
    }

    /// The limit of the contract on iOS, fixed so nobody relies on the opposite: a button that is not a toggle has no `Selected`
    /// trait and no value, so it reads exactly like a checkbox that is off. That is why a scenario that asserts a checked state
    /// asserts both states of the same element.
    func test_on_ios_unchecked_is_not_told_apart_from_not_being_a_toggle() {
        XCTAssertTrue(ToggleState.holdsState(.button))
        XCTAssertEqual(ToggleState.checked(value: nil, isSelected: false), false)
    }

    func test_a_value_the_driver_does_not_know_is_not_read_as_unchecked() {
        XCTAssertNil(ToggleState.checked(value: "activado", isSelected: false))
        XCTAssertNil(ToggleState.checked(value: "marcado", isSelected: true))
    }

    func test_only_a_switch_a_checkbox_a_toggle_or_a_button_can_hold_a_checked_state() {
        for type in [XCUIElement.ElementType.switch, .checkBox, .toggle, .button] { XCTAssertTrue(ToggleState.holdsState(type)) }
        for type in [XCUIElement.ElementType.staticText, .textField, .other, .image] { XCTAssertFalse(ToggleState.holdsState(type)) }
    }

    /// ZC-3: the line of a refusal begins with the primitive, and that word is what the harness counts.
    func test_a_refusal_line_begins_with_the_primitive_that_was_refused() {
        let log = DriverLog(echo: false)
        log.refuse("tapAt", "the app is not running")
        XCTAssertEqual(log.lines.last, "[refusal] tapAt the app is not running")
        XCTAssertEqual(log.lastRefusal, "tapAt the app is not running")
    }
}

/// The Objective-C shim that keeps an XCTest exception from reaching the Kotlin frames of the interpreter. No device is involved.
final class ObjCCatchTests: XCTestCase {
    private func raise(_ reason: String) {
        NSException(name: .invalidArgumentException, reason: reason, userInfo: nil).raise()
    }

    func test_a_block_that_returns_is_not_a_refusal() {
        var reason: NSString?
        XCTAssertTrue(TIObjCCatch({}, &reason))
        XCTAssertNil(reason)
    }

    func test_an_exception_is_caught_and_its_reason_reported() {
        var reason: NSString?
        XCTAssertFalse(TIObjCCatch({ self.raise("the app died") }, &reason))
        XCTAssertTrue((reason as String?)?.contains("the app died") ?? false, "reason was \(String(describing: reason))")
    }

    func test_guarded_answers_false_and_leaves_the_exception_as_the_refusal() {
        let log = DriverLog(echo: false)
        XCTAssertFalse(guarded("tap", "tap on a button", log: log) { self.raise("boom") })
        XCTAssertTrue(log.lastRefusal?.contains("boom") ?? false)
        XCTAssertTrue(log.lines.last?.hasPrefix("[refusal] tap ") ?? false, "the line begins with the primitive: \(String(describing: log.lines.last))")
        XCTAssertTrue(log.lines.last?.contains("XCTest raised an exception") ?? false)

        log.clearRefusal()
        XCTAssertTrue(guarded("tap", "tap on a button", log: log) {})
        XCTAssertNil(log.lastRefusal)
    }
}

/// The shim against a real XCTest incident (dB-4c): a touch on an application that is not installed makes XCTest record an
/// issue and raise. It runs inside `XCTExpectFailure`, so the recorded issue is the expected one, and inside `guarded`, so the
/// exception does not leave the closure: the test passing means the runner survived it and the call answered `false` with the
/// exception as its reason.
final class GuardedIncidentTests: XCTestCase {
    func test_a_real_xctest_incident_inside_guarded_is_a_refusal_and_not_a_crash() {
        let log = DriverLog(echo: false)
        let missing = XCUIApplication(bundleIdentifier: "com.gdavidpb.tuindice.shim.does.not.exist")
        var answered = true

        XCTExpectFailure("a touch on an application that does not exist is an XCTest incident") {
            answered = guarded("tap", "tap on a missing application", log: log) {
                missing.coordinate(withNormalizedOffset: .zero).tap()
            }
        }

        XCTAssertFalse(answered, "the call must answer false after the incident")
        XCTAssertNotNil(log.lastRefusal, "and leave the reason of the refusal")
    }
}
