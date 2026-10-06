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
