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
        #if DEBUG
        guard let webBaseUrl = launchArgumentString(for: webBaseUrlKey) else { return nil }
        return "\(webBaseUrl.trimmingCharacters(in: CharacterSet(charactersIn: "/")))/\(resource.path)"
        #else
        _ = resource
        return nil
        #endif
    }

    static func apiBaseUrl() -> String? {
        #if DEBUG
        return launchArgumentString(for: apiBaseUrlKey)
        #else
        return nil
        #endif
    }

    static func networkAvailabilityOverride() -> Bool? {
        #if DEBUG
        guard let rawValue = launchArgumentString(for: networkAvailabilityKey) else { return nil }

        switch rawValue.trimmingCharacters(in: .whitespacesAndNewlines).lowercased() {
        case "true", "1", "yes":
            return true
        case "false", "0", "no":
            return false
        default:
            return nil
        }
        #else
        return nil
        #endif
    }

    #if canImport(maincore) || canImport(Maincore)
    static func configureRemoteConfigOverridesIfNeeded(
        appBootstrap: IosAppHostBootstrap
    ) {
        #if DEBUG
        guard let rawEnabled = launchArgumentString(for: availabilityNoticeEnabledKey) else { return }
        let enabled = ["true", "1", "yes"].contains(rawEnabled.trimmingCharacters(in: .whitespacesAndNewlines).lowercased())

        appBootstrap.setDebugAppAvailabilityNoticeOverride(
            enabled: enabled,
            title: launchArgumentString(for: availabilityNoticeTitleKey) ?? "",
            message: launchArgumentString(for: availabilityNoticeMessageKey) ?? ""
        )
        #else
        _ = appBootstrap
        #endif
    }

    static func configureWizardStateOverridesIfNeeded(
        appBootstrap: IosAppHostBootstrap
    ) {
        #if DEBUG
        let isPending = launchArgumentString(for: wizardPendingKey)
            .map { ["true", "1", "yes"].contains($0.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()) }
            ?? false

        appBootstrap.setDebugWizardStartForced(enabled: isPending)
        #else
        _ = appBootstrap
        #endif
    }

    static func runStartupHooksIfNeeded(
        appBootstrap: IosAppHostBootstrap,
        apiBaseUrl: String
    ) {
        #if DEBUG
        guard let rawSeedState = launchArgumentString(for: seedStateKey) else { return }
        let seedState = rawSeedState.trimmingCharacters(in: .whitespacesAndNewlines)

        guard seedState.isEmpty == false else { return }

        let mainSectionName = launchArgumentString(for: mainSectionKey) ?? "SUMMARY"

        seedWireMockTokensIssuedState(apiBaseUrl: apiBaseUrl)
        switch seedState {
        case "authenticatedWizardComplete":
            appBootstrap.runAuthenticatedWizardCompleteStartupHook(mainSectionName: mainSectionName)
        case "authenticatedWizardPending":
            appBootstrap.runAuthenticatedWizardPendingStartupHook(mainSectionName: mainSectionName)
        default:
            fatalError("Unsupported debug startup hook: \(seedState)")
        }
        #else
        _ = appBootstrap
        _ = apiBaseUrl
        #endif
    }
    #endif
}

#if DEBUG
private extension TuIndiceDebugRuntimeOverrides {
    static let apiBaseUrlKey = "TUINDICE_E2E_API_BASE_URL"
    static let webBaseUrlKey = "TUINDICE_E2E_WEB_BASE_URL"
    static let networkAvailabilityKey = "TUINDICE_E2E_NETWORK_AVAILABLE"
    static let seedStateKey = "TUINDICE_E2E_SEED_STATE"
    static let mainSectionKey = "TUINDICE_E2E_MAIN_SECTION"
    static let availabilityNoticeEnabledKey = "TUINDICE_E2E_AVAILABILITY_NOTICE_ENABLED"
    static let availabilityNoticeTitleKey = "TUINDICE_E2E_AVAILABILITY_NOTICE_TITLE"
    static let availabilityNoticeMessageKey = "TUINDICE_E2E_AVAILABILITY_NOTICE_MESSAGE"
    static let wizardPendingKey = "TUINDICE_E2E_WIZARD_PENDING"

    static func launchArgumentString(for key: String) -> String? {
        if let value = ProcessInfo.processInfo.environment[key], value.isEmpty == false {
            return value
        }

        if let value = launchArgumentValue(for: key), value.isEmpty == false {
            return value
        }

        if let value = UserDefaults.standard.string(forKey: key), value.isEmpty == false {
            return value
        }

        return nil
    }

    static func launchArgumentValue(for key: String) -> String? {
        let arguments = ProcessInfo.processInfo.arguments
        guard let keyIndex = arguments.firstIndex(of: "-\(key)") else {
            return nil
        }

        let valueIndex = arguments.index(after: keyIndex)
        guard valueIndex < arguments.endIndex else {
            return nil
        }

        return arguments[valueIndex]
    }

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
