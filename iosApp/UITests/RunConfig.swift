import Foundation

/// Launch-argument keys the host app reads. The Kotlin definition is
/// maincore/src/commonMain/kotlin/com/gdavidpb/tuindice/debug/DebugLaunchArguments.kt
/// (`API_BASE_URL`, `WEB_BASE_URL`); ScenarioKit does not export it, so the two keys that the
/// iOS driver (and not the catalog) supplies are declared here, and only here.
enum LaunchKeys {
    static let apiBaseUrl = "TUINDICE_E2E_API_BASE_URL"
    static let webBaseUrl = "TUINDICE_E2E_WEB_BASE_URL"
}

/// What the harness hands the runner. `xcodebuild` forwards every `TEST_RUNNER_`-prefixed variable
/// with the prefix removed, so `TEST_RUNNER_E2E_WIREMOCK_URL` arrives as `E2E_WIREMOCK_URL`.
struct RunConfig {
    static let shared = RunConfig()

    private final class BundleToken {}

    /// WireMock as the simulator reaches it; the app's API and web base URLs derive from it.
    let wiremockUrl: String
    /// Host directory that receives `<id>/result.json`; nil attaches the result to the test instead.
    let outputDir: String?
    let trace: Bool
    /// Characters per `typeText` call. A hook for measuring typing reliability, not a retry knob.
    let typeChunk: Int
    let repoRoot: String

    var apiBaseUrl: String { wiremockUrl + "/" }
    var webBaseUrl: String { wiremockUrl }

    var catalogJson: String {
        guard let url = Bundle(for: BundleToken.self).url(forResource: "scenarios", withExtension: "json"),
              let text = try? String(contentsOf: url, encoding: .utf8)
        else { return "" }
        return text
    }

    private init() {
        let environment = ProcessInfo.processInfo.environment
        let rawUrl = environment["E2E_WIREMOCK_URL"].flatMap { $0.isEmpty ? nil : $0 } ?? "http://localhost:18627"
        wiremockUrl = rawUrl.hasSuffix("/") ? String(rawUrl.dropLast()) : rawUrl
        outputDir = environment["E2E_OUTPUT_DIR"].flatMap { $0.isEmpty ? nil : $0 }
        trace = ["1", "true", "yes"].contains((environment["E2E_TRACE"] ?? "").lowercased())
        typeChunk = environment["E2E_TYPE_CHUNK"].flatMap { Int($0) }.flatMap { $0 > 0 ? $0 : nil } ?? 4
        // <repo>/iosApp/UITests/RunConfig.swift
        repoRoot = URL(fileURLWithPath: #filePath)
            .deletingLastPathComponent().deletingLastPathComponent().deletingLastPathComponent().path
    }
}
