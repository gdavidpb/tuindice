import Foundation

#if canImport(maincore)
import maincore
#elseif canImport(Maincore)
import Maincore
#endif

enum TuIndiceDebugRuntimeOverrides {
    enum WebResource {
        case privacyPolicy
        case termsAndConditions
        case support
    }

    static func webUrl(for resource: WebResource) -> String? {
        #if DEBUG && (canImport(maincore) || canImport(Maincore))
        guard let webBaseUrl = launchArguments.webBaseUrl else { return nil }
        return "\(webBaseUrl.trimmingCharacters(in: CharacterSet(charactersIn: "/")))/\(resource.path)"
        #else
        _ = resource
        return nil
        #endif
    }

    static func apiBaseUrl() -> String? {
        #if DEBUG && (canImport(maincore) || canImport(Maincore))
        return launchArguments.apiBaseUrl
        #else
        return nil
        #endif
    }

    static func networkAvailabilityOverride() -> Bool? {
        #if DEBUG && (canImport(maincore) || canImport(Maincore))
        return launchArguments.networkAvailable?.boolValue
        #else
        return nil
        #endif
    }

    #if canImport(maincore) || canImport(Maincore)
    static func applyLaunchArguments(appBootstrap: IosAppHostBootstrap) {
        #if DEBUG
        appBootstrap.applyDebugLaunchArguments(arguments: launchArguments)
        #else
        _ = appBootstrap
        #endif
    }
    #endif
}

#if DEBUG
private extension TuIndiceDebugRuntimeOverrides {
    #if canImport(maincore) || canImport(Maincore)
    /// Every launch value under the shared prefix, parsed once by the Kotlin definition.
    /// The key names live only there; the environment wins over `-KEY value` argument pairs.
    static let launchArguments: DebugLaunchArguments = {
        let prefix = DebugLaunchArguments.companion.PREFIX
        var values: [String: String] = [:]
        let arguments = ProcessInfo.processInfo.arguments

        for (index, argument) in arguments.enumerated()
        where argument.hasPrefix("-\(prefix)") && index + 1 < arguments.count {
            values[String(argument.dropFirst())] = arguments[index + 1]
        }

        for (key, value) in ProcessInfo.processInfo.environment where key.hasPrefix(prefix) {
            values[key] = value
        }

        return DebugLaunchArguments.companion.parse(values: values)
    }()
    #endif
}

private extension TuIndiceDebugRuntimeOverrides.WebResource {
    var path: String {
        switch self {
        case .privacyPolicy:
            return "e2e/privacy.html"
        case .termsAndConditions:
            return "e2e/terms.html"
        case .support:
            return "e2e/support.html"
        }
    }
}
#endif
