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
    static func applyLaunchArguments(
        appBootstrap: IosAppHostBootstrap,
        apiBaseUrl: String
    ) {
        #if DEBUG
        if launchArguments.sessionSeed != nil {
            seedWireMockTokensIssuedState(apiBaseUrl: apiBaseUrl)
        }

        appBootstrap.applyDebugLaunchArguments(arguments: launchArguments)
        #else
        _ = appBootstrap
        _ = apiBaseUrl
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

    static func seedWireMockTokensIssuedState(apiBaseUrl: String) {
        let adminUrl = "\(apiBaseUrl.trimmingCharacters(in: CharacterSet(charactersIn: "/")))/__admin/scenarios/login-token-lifecycle/state"
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
    }
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
