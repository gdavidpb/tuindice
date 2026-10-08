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
        // <repo>/iosApp/UITests/RunConfig.swift
        repoRoot = URL(fileURLWithPath: #filePath)
            .deletingLastPathComponent().deletingLastPathComponent().deletingLastPathComponent().path
    }
}

/// What the driver does on its own, besides what a step asks: the choices it makes and tolerates. Each one leaves a line in
/// the driver log (always, not only with `E2E_TRACE`) and is counted, so a run that passed by way of a tolerance can be told
/// from one that did not.
final class DriverLog {
    enum Tolerance: String, CaseIterable {
        /// An alert that interrupted the app was dismissed by the interruption monitor.
        case dismissedAlert = "dismissed-alert"
    }

    private let lock = NSLock()
    private let echo: Bool
    private var entries: [String] = []
    private var counts: [Tolerance: Int] = [:]
    private var file: FileHandle?
    private var refusal: String?

    init(echo: Bool) {
        self.echo = echo
    }

    var lines: [String] { locked { entries } }

    /// The log as it is published: every line, then the count of each tolerance.
    var text: String { (lines + [summary]).joined(separator: "\n") + "\n" }

    var summary: String {
        let snapshot = locked { counts }
        let parts = Tolerance.allCases.map { kind in "\(kind.rawValue)=\(snapshot[kind] ?? 0)" }
        return "[tolerances] " + parts.joined(separator: " ")
    }

    /// From now on each line is also appended to the file at [path], as it is written, so that it survives a run that hangs
    /// and is killed. The lines written before are put in first.
    func open(at path: URL) {
        locked {
            try? FileManager.default.createDirectory(at: path.deletingLastPathComponent(), withIntermediateDirectories: true)
            guard FileManager.default.createFile(atPath: path.path, contents: Data((entries.joined(separator: "\n") + (entries.isEmpty ? "" : "\n")).utf8)),
                  let handle = try? FileHandle(forWritingTo: path)
            else { return }
            _ = try? handle.seekToEnd()
            file = handle
        }
    }

    func close() {
        locked {
            try? file?.close()
            file = nil
        }
    }

    func add(_ line: String) {
        locked {
            entries.append(line)
            if let data = (line + "\n").data(using: .utf8) { try? file?.write(contentsOf: data) }
        }
        if echo { print("[scenario] \(line)") }
    }

    /// The one funnel of every refusal: writes `[refusal] <primitive> <reason>` (the harness counts them by the first word, so it is always the
    /// primitive that was refused: `tap`, `tapAt`, `doubleTap`, `swipe`, `typeKeys`, `clearText`, `submitTextEntry`, `pressBack`, `foreground`,
    /// `scroll` or `guard` for the keyboard guard) and keeps the whole line as the reason the gesture or text entry in progress was refused.
    /// A static test reads the sources and fails if a call does not name one of those primitives as a literal.
    func refuse(_ primitive: String, _ reason: String) {
        let line = "\(primitive) \(reason)"
        locked { refusal = line }
        add("[refusal] \(line)")
    }

    /// Forgets the last refusal: every gesture and text entry starts without one.
    func clearRefusal() {
        locked { refusal = nil }
    }

    /// The reason the last gesture or text entry was refused, or nil when it was not.
    var lastRefusal: String? { locked { refusal } }

    /// Records that the driver tolerated [kind]: one line that says what, and one more in the count.
    func tolerate(_ kind: Tolerance, _ detail: String) {
        locked { counts[kind, default: 0] += 1 }
        add("[tolerance] \(kind.rawValue) \(detail)")
    }

    private func locked<T>(_ body: () -> T) -> T {
        lock.lock()
        defer { lock.unlock() }
        return body()
    }
}
