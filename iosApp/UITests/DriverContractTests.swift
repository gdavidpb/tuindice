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

/// Measures how long the Kotlin codec takes to decode the catalog through the bridge. It is a
/// measurement, not a check: it writes `catalog-decode/decode.json` under `E2E_OUTPUT_DIR` and attaches it.
final class CatalogDecodeTimingTests: XCTestCase {
    private static let warmRuns = 20
    private static let syntheticScenarios = 100
    private static let syntheticSteps = 30

    func test_catalog_decode_time() throws {
        let catalog = RunConfig.shared.catalogJson
        let synthetic = try syntheticCatalog(from: catalog)

        let cold = measureMicros { _ = ScenarioRunner.shared.ids(catalogJson: catalog) }
        let warm = (0..<Self.warmRuns).map { _ in measureMicros { _ = ScenarioRunner.shared.ids(catalogJson: catalog) } }
        var syntheticIds = 0
        let syntheticCold = measureMicros { syntheticIds = ScenarioRunner.shared.ids(catalogJson: synthetic).count }
        let syntheticWarm = (0..<Self.warmRuns).map { _ in measureMicros { _ = ScenarioRunner.shared.ids(catalogJson: synthetic) } }

        let report: [String: Any] = [
            "catalogBytes": catalog.utf8.count,
            "catalogScenarios": ScenarioRunner.shared.ids(catalogJson: catalog).count,
            "coldMicros": cold,
            "warmMicros": warm,
            "syntheticBytes": synthetic.utf8.count,
            "syntheticScenariosDecoded": syntheticIds,
            "syntheticSteps": Self.syntheticSteps,
            "syntheticColdMicros": syntheticCold,
            "syntheticWarmMicros": syntheticWarm,
        ]
        let data = try JSONSerialization.data(withJSONObject: report, options: [.sortedKeys])
        let text = String(decoding: data, as: UTF8.self)
        let attachment = XCTAttachment(string: text)
        attachment.name = "catalog-decode.json"
        attachment.lifetime = .keepAlways
        add(attachment)

        guard let outputDir = RunConfig.shared.outputDir else { return }
        let directory = URL(fileURLWithPath: outputDir).appendingPathComponent("catalog-decode")
        try FileManager.default.createDirectory(at: directory, withIntermediateDirectories: true)
        try text.write(to: directory.appendingPathComponent("decode.json"), atomically: true, encoding: .utf8)
    }

    /// The real catalog's longest scenario, cloned under new ids until there are 100 scenarios of 30 steps.
    private func syntheticCatalog(from catalog: String) throws -> String {
        guard var root = try JSONSerialization.jsonObject(with: Data(catalog.utf8)) as? [String: Any],
              let scenarios = root["scenarios"] as? [[String: Any]],
              let template = scenarios.max(by: { ($0["steps"] as? [Any])?.count ?? 0 < ($1["steps"] as? [Any])?.count ?? 0 }),
              let templateSteps = template["steps"] as? [Any], !templateSteps.isEmpty
        else { throw NSError(domain: "CatalogDecodeTimingTests", code: 1) }

        let steps = (0..<Self.syntheticSteps).map { templateSteps[$0 % templateSteps.count] }
        root["scenarios"] = (0..<Self.syntheticScenarios).map { index -> [String: Any] in
            var scenario = template
            scenario["id"] = "synthetic-\(index)"
            scenario["steps"] = steps
            return scenario
        }
        return String(decoding: try JSONSerialization.data(withJSONObject: root), as: UTF8.self)
    }

    private func measureMicros(_ body: () -> Void) -> UInt64 {
        let start = DispatchTime.now().uptimeNanoseconds
        body()
        return (DispatchTime.now().uptimeNanoseconds - start) / 1_000
    }
}
