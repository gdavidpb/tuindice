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
    private static let defaultAppStoreUrl = "itms-apps://apps.apple.com/app/id6760307454"
    private static let defaultLocaleTag = "es-VE"
    private static let defaultAppleLocaleIdentifier = "es_VE"
    private static let buildVariant: IosBuildVariant = {
        #if DEBUG
        return .debug
        #else
        return .production
        #endif
    }()
    private static let appStoreUrl = bundleString(
        for: "TUINDICE_APP_STORE_URL",
        defaultValue: defaultAppStoreUrl
    )
    private static let apiBaseUrl = resolvedApiBaseUrl(defaultValue: defaultApiBaseUrl)
    private static let bridge = TuIndicePlatformBridge(
        appStoreUrl: appStoreUrl,
        apiBaseUrl: apiBaseUrl
    )
    private static let hostConfig = IosAppHostConfig(
        bridge: bridge,
        apiBaseUrl: apiBaseUrl,
        privacyPolicyUrl: resolvedWebUrl(
            debugResource: .privacyPolicy,
            bundleKey: "TUINDICE_PRIVACY_POLICY_URL",
            defaultValue: "https://tuindice.app/privacy"
        ),
        termsAndConditionsUrl: resolvedWebUrl(
            debugResource: .termsAndConditions,
            bundleKey: "TUINDICE_TERMS_AND_CONDITIONS_URL",
            defaultValue: "https://tuindice.app/terms"
        ),
        supportUrl: resolvedWebUrl(
            debugResource: .support,
            bundleKey: "TUINDICE_SUPPORT_URL",
            defaultValue: "https://tuindice.app/support"
        ),
        appStoreUrl: appStoreUrl,
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
        TuIndiceDebugRuntimeOverrides.configureRemoteConfigOverridesIfNeeded(
            appBootstrap: appBootstrap
        )
        TuIndiceDebugRuntimeOverrides.runStartupHooksIfNeeded(
            appBootstrap: appBootstrap,
            apiBaseUrl: hostConfig.apiBaseUrl
        )
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
        debugResource: TuIndiceDebugRuntimeOverrides.WebResource,
        bundleKey: String,
        defaultValue: String
    ) -> String {
        if let debugUrl = TuIndiceDebugRuntimeOverrides.webUrl(for: debugResource) {
            return debugUrl
        }

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
        let configured = TuIndiceDebugRuntimeOverrides.apiBaseUrl() ??
            bundleString(for: "TUINDICE_API_BASE_URL", defaultValue: defaultValue)

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
    #endif
}
