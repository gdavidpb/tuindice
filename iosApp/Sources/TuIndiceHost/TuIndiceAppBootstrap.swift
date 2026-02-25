import Foundation
import UIKit

#if canImport(maincore)
import maincore
#elseif canImport(Maincore)
import Maincore
#endif

enum TuIndiceAppBootstrap {
    #if canImport(maincore) || canImport(Maincore)
    private static let smokeValidationEnvKey = "TUINDICE_IOS_SMOKE_VALIDATE"
    private static let smokeRunIdEnvKey = "TUINDICE_IOS_SMOKE_RUN_ID"
    private static let smokeMarkerPrefix = "TUINDICE_SMOKE_MARKER"
    private static let defaultApiBaseUrl = "https://api.tuindice.app/"
    private static let buildVariant: IosBuildVariant = {
        #if DEBUG
        return .debug
        #else
        return .production
        #endif
    }()
    private static let bridge = TuIndicePlatformBridge()
    private static let appLauncher = TuIndiceIosAppLauncher(
        bridge: bridge,
        apiBaseUrl: resolvedApiBaseUrl(defaultValue: defaultApiBaseUrl),
        privacyPolicyUrl: bundleString(
            for: "TUINDICE_PRIVACY_POLICY_URL",
            defaultValue: "https://tuindice.app/privacy_policy.html"
        ),
        termsAndConditionsUrl: bundleString(
            for: "TUINDICE_TERMS_AND_CONDITIONS_URL",
            defaultValue: "https://tuindice.app/terms_and_conditions.html"
        ),
        debug: bundleBoolean(for: "TUINDICE_DEBUG", defaultValue: false),
        buildVariant: buildVariant
    )
    #endif

    static func makeRootViewController() -> UIViewController {
        #if canImport(maincore) || canImport(Maincore)
        let root = appLauncher.createRootViewController()
        runSmokeValidationIfEnabled()
        return root
        #else
        return UIViewController()
        #endif
    }

    static func updatePushToken(_ token: String?) {
        #if canImport(maincore) || canImport(Maincore)
        bridge.updatePushToken(token)
        #endif
    }

    #if canImport(maincore) || canImport(Maincore)
    private static func runSmokeValidationIfEnabled() {
        guard isSmokeValidationEnabled() else { return }

        DispatchQueue.global(qos: .userInitiated).async {
            let smokeRunId = ProcessInfo.processInfo.environment[smokeRunIdEnvKey] ?? "default"
            let result = TuIndiceIosSmokeVerifier().runChecks()
            NSLog("\(smokeMarkerPrefix):\(smokeRunId):\(result)")
        }
    }

    private static func isSmokeValidationEnabled() -> Bool {
        return environmentBoolean(for: smokeValidationEnvKey, defaultValue: false)
    }

    private static func environmentBoolean(for key: String, defaultValue: Bool) -> Bool {
        guard let rawValue = ProcessInfo.processInfo.environment[key] else {
            return defaultValue
        }

        switch rawValue.lowercased() {
        case "1", "true", "yes":
            return true
        case "0", "false", "no":
            return false
        default:
            return defaultValue
        }
    }

    private static func bundleString(for key: String, defaultValue: String) -> String {
        if let value = ProcessInfo.processInfo.environment[key], value.isEmpty == false {
            return value
        }

        if let value = Bundle.main.object(forInfoDictionaryKey: key) as? String,
           value.isEmpty == false {
            return value
        }

        return defaultValue
    }

    private static func bundleBoolean(for key: String, defaultValue: Bool) -> Bool {
        if let raw = ProcessInfo.processInfo.environment[key] {
            switch raw.lowercased() {
            case "1", "true", "yes":
                return true
            case "0", "false", "no":
                return false
            default:
                return defaultValue
            }
        }

        if let value = Bundle.main.object(forInfoDictionaryKey: key) as? Bool {
            return value
        }

        if let raw = Bundle.main.object(forInfoDictionaryKey: key) as? String {
            switch raw.lowercased() {
            case "1", "true", "yes":
                return true
            case "0", "false", "no":
                return false
            default:
                return defaultValue
            }
        }

        return defaultValue
    }

    private static func resolvedApiBaseUrl(defaultValue: String) -> String {
        let configured = bundleString(for: "TUINDICE_API_BASE_URL", defaultValue: defaultValue)

        guard shouldRejectLocalhostApiUrl(configured) else {
            return configured
        }

        NSLog(
            "TuIndice iOS: TUINDICE_API_BASE_URL=\(configured) is localhost on physical device; falling back to \(defaultValue)."
        )
        return defaultValue
    }

    private static func shouldRejectLocalhostApiUrl(_ value: String) -> Bool {
        #if targetEnvironment(simulator)
        return false
        #else
        guard let host = URL(string: value)?.host?.lowercased() else {
            return false
        }

        return host == "localhost" ||
            host == "127.0.0.1" ||
            host == "0.0.0.0" ||
            host == "::1"
        #endif
    }
    #endif
}
