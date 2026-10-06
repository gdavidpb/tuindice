import XCTest
import ScenarioKit

/// Probes the iOS driver with the catalog's contract fixture, the way `DriverContractTest` does on
/// Android, before scenarios are trusted on it. Failures are reported by the same path as scenarios.
final class DriverContractTests: ScenarioTestCase {
    func test_driver_contract() {
        let config = RunConfig.shared
        let driver = XCUIScenarioDriver(config: config)
        let outcome = ScenarioRunner.shared.driverContract(catalogJson: config.catalogJson, driver: driver)
        report(outcome, driver: driver)
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
