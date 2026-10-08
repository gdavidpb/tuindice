import XCTest
import ScenarioKit

/// Base of the generated `*ScenarioTests` classes: runs one catalog scenario through the shared
/// ScenarioKit interpreter and reports its outcome to XCTest.
class ScenarioTestCase: XCTestCase {
    /// The driver's log of this test: it goes to `driver.log` as it is written and is shared with the interruption monitor.
    let driverLog = DriverLog(echo: RunConfig.shared.trace)

    override func setUp() {
        super.setUp()
        // A failure must never unwind through the Kotlin frames the interpreter is running on.
        continueAfterFailure = true
        SystemUi.installInterruptionMonitor(on: self, log: driverLog)
    }

    func runScenario(_ id: String) {
        let config = RunConfig.shared
        if let outputDir = config.outputDir {
            driverLog.open(at: URL(fileURLWithPath: outputDir).appendingPathComponent(id).appendingPathComponent("driver.log"))
        }
        let driver = XCUIScenarioDriver(config: config, log: driverLog)
        let catalogJson = config.catalogJson
        let start = DispatchTime.now().uptimeNanoseconds
        let outcome = ScenarioRunner.shared.run(catalogJson: catalogJson, scenarioId: id, driver: driver)
        if config.trace {
            // Whole interpreter run seen from Swift; with the `[driver]` lines it gives the bridge overhead.
            driver.log(line: "[bridge] run-total \((DispatchTime.now().uptimeNanoseconds - start) / 1_000)")
        }
        report(outcome, driver: driver)
    }

    /// Writes `result.json`, attaches the logs and, on failure, records one issue pointing at the Kotlin step.
    func report(_ outcome: ScenarioOutcome, driver: XCUIScenarioDriver, file: StaticString = #filePath, line: UInt = #line) {
        driverLog.close()
        let driverLog = self.driverLog.text
        publishResult(outcome, driverLog: driverLog)
        attach("driver.log", driverLog)
        attach("report.txt", outcome.report)
        guard !outcome.passed else { return }

        let location = outcome.failure?.site.map {
            XCTSourceCodeLocation(filePath: RunConfig.shared.repoRoot + "/" + $0.file, lineNumber: Int($0.line))
        } ?? XCTSourceCodeLocation(filePath: String(describing: file), lineNumber: Int(line))
        let issue = XCTIssue(
            type: .assertionFailure,
            compactDescription: outcome.report,
            detailedDescription: nil,
            sourceCodeContext: XCTSourceCodeContext(location: location),
            associatedError: nil,
            attachments: driver.failureAttachments
        )
        record(issue)
    }

    private func publishResult(_ outcome: ScenarioOutcome, driverLog: String) {
        let json = outcome.resultJson
        guard let outputDir = RunConfig.shared.outputDir else {
            attach("result.json", json)
            return
        }

        let directory = URL(fileURLWithPath: outputDir).appendingPathComponent(outcome.scenarioId)
        do {
            try FileManager.default.createDirectory(at: directory, withIntermediateDirectories: true)
            try json.write(to: directory.appendingPathComponent("result.json"), atomically: true, encoding: .utf8)
            try driverLog.write(to: directory.appendingPathComponent("driver.log"), atomically: true, encoding: .utf8)
        } catch {
            // The runner cannot write to the host path: keep the result with the test instead.
            attach("result.json", json)
            attach("result-write-error.txt", String(describing: error))
        }
    }

    private func attach(_ name: String, _ text: String) {
        let attachment = XCTAttachment(string: text)
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)
    }
}
