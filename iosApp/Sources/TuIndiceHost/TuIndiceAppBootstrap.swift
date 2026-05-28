import Foundation
import UIKit

#if canImport(maincore)
import maincore
#elseif canImport(Maincore)
import Maincore
#endif

enum TuIndiceAppBootstrap {
    #if canImport(maincore) || canImport(Maincore)
    private static let defaultApiBaseUrl = "https://api.tuindice.app/"
    private static let defaultLocaleTag = "es-VE"
    private static let defaultAppleLocaleIdentifier = "es_VE"
    private static let e2eSeedStateKey = "TUINDICE_E2E_SEED_STATE"
    private static let e2eMainSectionKey = "TUINDICE_E2E_MAIN_SECTION"
    private static let authenticatedWizardCompleteSeed = "authenticatedWizardComplete"
    private static let buildVariant: IosBuildVariant = {
        #if DEBUG
        return .debug
        #else
        return .production
        #endif
    }()
    private static let bridge = TuIndicePlatformBridge()
    private static let hostConfig = IosAppHostConfig(
        bridge: bridge,
        apiBaseUrl: resolvedApiBaseUrl(defaultValue: defaultApiBaseUrl),
        privacyPolicyUrl: resolvedWebUrl(
            e2ePath: "e2e/privacy.html",
            bundleKey: "TUINDICE_PRIVACY_POLICY_URL",
            defaultValue: "https://tuindice.app/privacy_policy_v6_0.html"
        ),
        termsAndConditionsUrl: resolvedWebUrl(
            e2ePath: "e2e/terms.html",
            bundleKey: "TUINDICE_TERMS_AND_CONDITIONS_URL",
            defaultValue: "https://tuindice.app/terms_and_conditions_v6_0.html"
        ),
        supportUrl: resolvedWebUrl(
            e2ePath: "e2e/support.html",
            bundleKey: "TUINDICE_SUPPORT_URL",
            defaultValue: "https://tuindice.app/support_v6_0.html"
        ),
        debug: bundleBoolean(for: "TUINDICE_DEBUG", defaultValue: false),
        buildVariant: buildVariant
    )
    private static let appBootstrap = IosAppHostBootstrap(
        hostConfig: hostConfig
    )

    static var shouldUseFirebaseServices: Bool {
        buildVariant == .production
    }
    #endif

    static func makeRootViewController() -> UIViewController {
        #if canImport(maincore) || canImport(Maincore)
        configureLocale()
        seedE2eStateIfNeeded()
        return appBootstrap.createRootViewController()
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
    private static func bundleString(for key: String, defaultValue: String) -> String {
        if let value = launchArgumentString(for: key) {
            return value
        }

        if let value = Bundle.main.object(forInfoDictionaryKey: key) as? String,
           value.isEmpty == false {
            return value
        }

        return defaultValue
    }

    private static func launchArgumentString(for key: String) -> String? {
        if let value = ProcessInfo.processInfo.environment[key], value.isEmpty == false {
            return value
        }

        if let value = UserDefaults.standard.string(forKey: key), value.isEmpty == false {
            return value
        }

        return nil
    }

    private static func resolvedWebUrl(
        e2ePath: String,
        bundleKey: String,
        defaultValue: String
    ) -> String {
        #if DEBUG
        if let webBaseUrl = launchArgumentString(for: "TUINDICE_E2E_WEB_BASE_URL") {
            return "\(webBaseUrl.trimmingCharacters(in: CharacterSet(charactersIn: "/")))/\(e2ePath)"
        }
        #endif

        return bundleString(for: bundleKey, defaultValue: defaultValue)
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
        #if DEBUG
        let configured = launchArgumentString(for: "TUINDICE_E2E_API_BASE_URL") ??
            bundleString(for: "TUINDICE_API_BASE_URL", defaultValue: defaultValue)
        #else
        let configured = bundleString(for: "TUINDICE_API_BASE_URL", defaultValue: defaultValue)
        #endif

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

    private static func configureLocale() {
        UserDefaults.standard.set([defaultLocaleTag], forKey: "AppleLanguages")
        UserDefaults.standard.set(defaultAppleLocaleIdentifier, forKey: "AppleLocale")
    }

    private static func seedE2eStateIfNeeded() {
        #if DEBUG
        guard let seedState = launchArgumentString(for: e2eSeedStateKey) else { return }

        guard seedState == authenticatedWizardCompleteSeed else {
            fatalError("Unsupported E2E seed state: \(seedState)")
        }

        seedWireMockTokensIssuedState()
        appBootstrap.seedE2eState(
            state: seedState,
            mainSectionName: launchArgumentString(for: e2eMainSectionKey) ?? "SUMMARY"
        )
        #endif
    }

    private static func seedWireMockTokensIssuedState() {
        #if DEBUG
        let adminUrl = "\(hostConfig.apiBaseUrl.trimmingCharacters(in: CharacterSet(charactersIn: "/")))/__admin/scenarios/login-token-lifecycle/state"
        guard let url = URL(string: adminUrl) else {
            fatalError("Invalid WireMock admin URL: \(adminUrl)")
        }

        var request = URLRequest(url: url)
        request.httpMethod = "PUT"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = #"{"state":"TokensIssued"}"#.data(using: .utf8)

        let semaphore = DispatchSemaphore(value: 0)
        var requestError: Error?
        var responseStatusCode = 0

        URLSession.shared.dataTask(with: request) { _, response, error in
            requestError = error
            responseStatusCode = (response as? HTTPURLResponse)?.statusCode ?? 0
            semaphore.signal()
        }.resume()

        guard semaphore.wait(timeout: .now() + 5) == .success else {
            fatalError("Timed out seeding WireMock login-token-lifecycle scenario.")
        }

        if let requestError {
            fatalError("Failed to seed WireMock login-token-lifecycle scenario: \(requestError)")
        }

        guard (200...299).contains(responseStatusCode) else {
            fatalError("WireMock scenario seed failed with HTTP \(responseStatusCode).")
        }
        #endif
    }
    #endif
}
