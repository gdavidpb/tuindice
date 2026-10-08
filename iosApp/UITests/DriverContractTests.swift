import XCTest
import ScenarioKit

/// Probes the iOS driver with the catalog's contract fixture, the way `DriverContractTest` does on
/// Android, before scenarios are trusted on it. Failures are reported by the same path as scenarios.
final class DriverContractTests: ScenarioTestCase {
    func test_driver_contract() {
        let config = RunConfig.shared
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
}
