import CryptoKit
import DeviceCheck
import Foundation
import Network
import Security
import StoreKit
import UIKit
#if canImport(FirebaseCrashlytics)
import FirebaseCrashlytics
#endif
#if canImport(FirebaseMessaging)
import FirebaseMessaging
#endif
#if canImport(FirebaseRemoteConfig)
import FirebaseRemoteConfig
#endif

#if canImport(maincore)
import maincore
#elseif canImport(Maincore)
import Maincore
#endif

/// Swift-side bridge entry points expected by shared Kotlin runtime.
///
/// Keep implementations platform-only:
/// - APNs / Firebase lifecycle hooks
/// - App Attest callbacks
/// - URL / file / share / email external actions
/// - in-app review / update equivalents on iOS
#if canImport(maincore) || canImport(Maincore)
final class TuIndicePlatformBridge: NSObject, IosPlatformBridge {
    private let secureStore = KeychainSecureStore(
        service: Bundle.main.bundleIdentifier ?? "com.gdavidpb.tuindice.securestore"
    )
    private let pathMonitor = NWPathMonitor()
    private let monitorQueue = DispatchQueue(label: "com.gdavidpb.tuindice.network-monitor")
    private let stateQueue = DispatchQueue(label: "com.gdavidpb.tuindice.bridge-state")
    private var latestPushToken: String?
    private var isReachable: Bool = true
    private var appStoreTrackUrl: String?
    private let remoteConfigFetchInterval: TimeInterval = {
        #if DEBUG
        return 0
        #else
        return 43_200
        #endif
    }()

    private var isFirebaseConfigured: Bool {
        TuIndiceFirebaseRuntimeState.isConfigured
    }

    override init() {
        super.init()

        pathMonitor.pathUpdateHandler = { [weak self] path in
            self?.stateQueue.async {
                self?.isReachable = path.status == .satisfied
            }
        }
        pathMonitor.start(queue: monitorQueue)
    }

    deinit {
        pathMonitor.cancel()
    }

    func updatePushToken(_ token: String?) {
        stateQueue.async {
            self.latestPushToken = token
        }
    }

    func fetchRemoteConfig(completionHandler: @escaping (Error?) -> Void) {
        #if canImport(FirebaseRemoteConfig)
        guard isFirebaseConfigured else {
            completionHandler(nil)
            return
        }

        let remoteConfig = RemoteConfig.remoteConfig()
        let settings = RemoteConfigSettings()
        settings.minimumFetchInterval = remoteConfigFetchInterval
        remoteConfig.configSettings = settings

        remoteConfig.fetchAndActivate { _, error in
            completionHandler(error)
        }
        #else
        completionHandler(nil)
        #endif
    }

    func remoteConfigString(key: String) -> String? {
        #if canImport(FirebaseRemoteConfig)
        guard isFirebaseConfigured else {
            return nil
        }

        let value = RemoteConfig.remoteConfig().configValue(forKey: key).stringValue
        return value.isEmpty ? nil : value
        #else
        return nil
        #endif
    }

    func remoteConfigStringList(key: String) -> [String]? {
        guard let raw = remoteConfigString(key: key),
              let data = raw.data(using: .utf8),
              let list = try? JSONDecoder().decode([String].self, from: data) else {
            return nil
        }

        return list
    }

    func resolveAttestationKeyId(completionHandler: @escaping (String?, Error?) -> Void) {
        resolveAppAttestKeyId(completionHandler: completionHandler)
    }

    func invalidateAttestationKeyId(completionHandler: @escaping (Error?) -> Void) {
        secureStore.delete(Self.appAttestKeyIdKey)
        completionHandler(nil)
    }

    func requestAttestation(
        attestationInput: String,
        keyId: String,
        evidenceMode: String,
        completionHandler: @escaping (IosPlatformAttestation?, Error?) -> Void
    ) {
        let appAttestService = DCAppAttestService.shared

        guard appAttestService.isSupported else {
            completionHandler(nil, nil)
            return
        }

        guard let clientDataHash = Self.appAttestClientDataHash(from: attestationInput) else {
            completionHandler(nil, nil)
            return
        }

        switch evidenceMode {
        case Self.appAttestAttestationEvidenceMode:
            appAttestService.attestKey(keyId, clientDataHash: clientDataHash) { attestation, error in
                if let error {
                    completionHandler(nil, error)
                    return
                }

                guard let attestation else {
                    completionHandler(nil, nil)
                    return
                }

                completionHandler(
                    IosPlatformAttestation(
                        token: attestation.base64EncodedString(),
                        keyId: keyId,
                        provider: BaseAttestationProvider.appAttest
                    ),
                    nil
                )
            }

        case Self.appAttestAssertionEvidenceMode:
            appAttestService.generateAssertion(keyId, clientDataHash: clientDataHash) { assertion, error in
                if let error {
                    completionHandler(nil, error)
                    return
                }

                guard let assertion else {
                    completionHandler(nil, nil)
                    return
                }

                completionHandler(
                    IosPlatformAttestation(
                        token: assertion.base64EncodedString(),
                        keyId: keyId,
                        provider: BaseAttestationProvider.appAttest
                    ),
                    nil
                )
            }

        default:
            completionHandler(nil, nil)
        }
    }

    func pushToken(completionHandler: @escaping (String?, Error?) -> Void) {
        #if canImport(FirebaseMessaging)
        guard isFirebaseConfigured else {
            stateQueue.async {
                completionHandler(self.latestPushToken, nil)
            }
            return
        }

        Messaging.messaging().token { [weak self] token, error in
            guard let self else {
                completionHandler(nil, nil)
                return
            }

            if let error {
                completionHandler(nil, error)
                return
            }

            if let token, token.isEmpty == false {
                self.updatePushToken(token)
                completionHandler(token, nil)
                return
            }

            self.stateQueue.async {
                completionHandler(self.latestPushToken, nil)
            }
        }
        #else
        stateQueue.async {
            completionHandler(self.latestPushToken, nil)
        }
        #endif
    }

    func sha256Base64Url(value: String) -> String? {
        guard let data = value.data(using: .utf8) else {
            return nil
        }

        let digest = Data(SHA256.hash(data: data))
        return digest.base64EncodedString()
            .replacingOccurrences(of: "+", with: "-")
            .replacingOccurrences(of: "/", with: "_")
            .replacingOccurrences(of: "=", with: "")
    }

    func launchReview(completionHandler: @escaping (Error?) -> Void) {
        guard let windowScene = UIApplication.shared.connectedScenes
            .compactMap({ $0 as? UIWindowScene })
            .first else {
            completionHandler(nil)
            return
        }

        SKStoreReviewController.requestReview(in: windowScene)
        completionHandler(nil)
    }

    func checkForUpdate(
        stalenessDays: Int32,
        completionHandler: @escaping (BaseUpdateAction?, Error?) -> Void
    ) {
        guard let bundleId = Bundle.main.bundleIdentifier,
              let encodedBundleId = bundleId.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed),
              let lookupUrl = URL(string: "https://itunes.apple.com/lookup?bundleId=\(encodedBundleId)") else {
            completionHandler(nil, nil)
            return
        }

        URLSession.shared.dataTask(with: lookupUrl) { [weak self] data, _, _ in
            guard let self else {
                completionHandler(nil, nil)
                return
            }

            guard let data,
                  let lookup = try? JSONDecoder().decode(AppStoreLookupResponse.self, from: data),
                  let app = lookup.results.first else {
                completionHandler(nil, nil)
                return
            }

            let installedVersion = self.appVersionName()
            let hasNewerVersion = Self.isVersion(app.version, newerThan: installedVersion)

            guard hasNewerVersion else {
                completionHandler(nil, nil)
                return
            }

            if stalenessDays > 0 {
                let isStaleEnough = Self.isReleaseDateOlderThan(
                    app.currentVersionReleaseDate,
                    stalenessDays: Int(stalenessDays)
                )

                guard isStaleEnough else {
                    completionHandler(nil, nil)
                    return
                }
            }

            self.stateQueue.async {
                self.appStoreTrackUrl = app.trackViewUrl
            }

            completionHandler(BaseUpdateAction.immediate, nil)
        }
        .resume()
    }

    func launchUpdate(action: BaseUpdateAction, completionHandler: @escaping (Error?) -> Void) {
        if action == BaseUpdateAction.immediate {
            let trackUrl = stateQueue.sync { appStoreTrackUrl }

            if let trackUrl {
                openUrl(url: trackUrl)
            } else {
                openStorePage()
            }
        }

        completionHandler(nil)
    }

    func openUrl(url: String) {
        guard let url = URL(string: url) else { return }
        UIApplication.shared.open(url)
    }

    func openStorePage() {
        let trackUrl = stateQueue.sync { appStoreTrackUrl }

        if let trackUrl {
            openUrl(url: trackUrl)
        } else {
            openUrl(url: "itms-apps://apps.apple.com")
        }
    }

    func openFile(path: String) -> Bool {
        return openResolvedFile(path: path)
    }

    func canOpen(path: String) -> Bool {
        return canOpenResolvedFile(path: path)
    }

    private func openResolvedFile(path: String) -> Bool {
        let url: URL
        if path.contains("://"), let parsed = URL(string: path) {
            url = parsed
        } else {
            url = URL(fileURLWithPath: path)
        }

        guard UIApplication.shared.canOpenURL(url) else { return false }
        UIApplication.shared.open(url)
        return true
    }

    private func canOpenResolvedFile(path: String) -> Bool {
        let url: URL
        if path.contains("://"), let parsed = URL(string: path) {
            url = parsed
        } else {
            url = URL(fileURLWithPath: path)
        }

        return UIApplication.shared.canOpenURL(url)
    }

    func sendEmail(email: String, subject: String, text: String) {
        let encodedSubject = subject.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
        let encodedBody = text.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
        let raw = "mailto:\(email)?subject=\(encodedSubject)&body=\(encodedBody)"
        openUrl(url: raw)
    }

    func shareText(subject: String, text: String) {
        let activity = UIActivityViewController(
            activityItems: [text],
            applicationActivities: nil
        )

        activity.setValue(subject, forKey: "subject")

        guard let topController = Self.topViewController() else {
            sendEmail(email: "", subject: subject, text: text)
            return
        }

        topController.present(activity, animated: true)
    }

    func appVersionName() -> String {
        Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "0.0.0"
    }

    func appVersionCode() -> Int64 {
        Int64(Bundle.main.infoDictionary?["CFBundleVersion"] as? String ?? "") ?? 0
    }

    func hasCamera() -> Bool {
        UIImagePickerController.isSourceTypeAvailable(.camera)
    }

    func isNetworkAvailable() -> Bool {
        stateQueue.sync { isReachable }
    }

    func setUserIdentifier(identifier: String) {
        #if canImport(FirebaseCrashlytics)
        guard isFirebaseConfigured else { return }
        Crashlytics.crashlytics().setUserID(identifier)
        #endif
    }

    func logMessage(message: String) {
        NSLog("%@", message)

        #if canImport(FirebaseCrashlytics)
        guard isFirebaseConfigured else { return }
        Crashlytics.crashlytics().log(message)
        #endif
    }

    func logException(throwable: KotlinThrowable) {
        let description = throwable.description()
        NSLog("%@", description)

        #if canImport(FirebaseCrashlytics)
        guard isFirebaseConfigured else { return }
        let message = throwable.message ?? description
        let crashlytics = Crashlytics.crashlytics()
        crashlytics.log("Kotlin throwable: \(description)")

        if message != description {
            crashlytics.log("Kotlin throwable message: \(message)")
        }

        let error = NSError(
            domain: "com.gdavidpb.tuindice.kotlin",
            code: 1,
            userInfo: [
                NSLocalizedDescriptionKey: message,
                NSDebugDescriptionErrorKey: description
            ]
        )
        crashlytics.record(error: error)
        #endif
    }

    func setCustomKey(key: String, value_ value: String) {
        #if canImport(FirebaseCrashlytics)
        guard isFirebaseConfigured else { return }
        Crashlytics.crashlytics().setCustomValue(value, forKey: key)
        #endif
    }

    private static func topViewController(
        from root: UIViewController? = UIApplication.shared
            .connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap(\.windows)
            .first(where: { $0.isKeyWindow })?
            .rootViewController
    ) -> UIViewController? {
        if let navigation = root as? UINavigationController {
            return topViewController(from: navigation.visibleViewController)
        }

        if let tab = root as? UITabBarController {
            return topViewController(from: tab.selectedViewController)
        }

        if let presented = root?.presentedViewController {
            return topViewController(from: presented)
        }

        return root
    }

    private static func isVersion(_ lhs: String, newerThan rhs: String) -> Bool {
        lhs.compare(rhs, options: .numeric) == .orderedDescending
    }

    private static func isReleaseDateOlderThan(_ rawDate: String?, stalenessDays: Int) -> Bool {
        guard stalenessDays > 0 else { return true }
        guard let rawDate else { return true }

        let parser = ISO8601DateFormatter()
        guard let releaseDate = parser.date(from: rawDate) else { return true }

        let elapsed = Date().timeIntervalSince(releaseDate)
        let elapsedDays = Int(elapsed / 86_400)

        return elapsedDays >= stalenessDays
    }

    private func resolveAppAttestKeyId(
        completionHandler: @escaping (String?, Error?) -> Void
    ) {
        if let stored = secureStore.read(Self.appAttestKeyIdKey),
           stored.isEmpty == false {
            completionHandler(stored, nil)
            return
        }

        DCAppAttestService.shared.generateKey { [weak self] keyId, error in
            guard let self else {
                completionHandler(nil, nil)
                return
            }

            if let error {
                completionHandler(nil, error)
                return
            }

            guard let keyId else {
                completionHandler(nil, nil)
                return
            }

            self.secureStore.write(Self.appAttestKeyIdKey, value: keyId)
            completionHandler(keyId, nil)
        }
    }

    private static func appAttestClientDataHash(from input: String) -> Data? {
        guard let inputData = input.data(using: .utf8) else { return nil }
        return Data(SHA256.hash(data: inputData))
    }

    private static let appAttestKeyIdKey = "tuindice.app_attest.key_id"
    private static let appAttestAttestationEvidenceMode = "app_attest_attestation"
    private static let appAttestAssertionEvidenceMode = "app_attest_assertion"
}
#else
final class TuIndicePlatformBridge {}
#endif

private struct KeychainSecureStore {
    let service: String

    func contains(_ key: String) -> Bool {
        read(key) != nil
    }

    func read(_ key: String) -> String? {
        var query = baseQuery(for: key)
        query[kSecReturnData as String] = true
        query[kSecMatchLimit as String] = kSecMatchLimitOne

        var item: CFTypeRef?
        let status = SecItemCopyMatching(query as CFDictionary, &item)

        guard status == errSecSuccess else { return nil }
        guard let data = item as? Data else { return nil }

        return String(data: data, encoding: .utf8)
    }

    func write(_ key: String, value: String) {
        guard let encoded = value.data(using: .utf8) else { return }

        let query = baseQuery(for: key)
        let attributes: [String: Any] = [
            kSecValueData as String: encoded
        ]

        let updateStatus = SecItemUpdate(query as CFDictionary, attributes as CFDictionary)

        if updateStatus == errSecItemNotFound {
            var addQuery = query
            addQuery[kSecValueData as String] = encoded
            SecItemAdd(addQuery as CFDictionary, nil)
        }
    }

    func clear() {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service
        ]

        SecItemDelete(query as CFDictionary)
    }

    func delete(_ key: String) {
        let query = baseQuery(for: key)
        SecItemDelete(query as CFDictionary)
    }

    private func baseQuery(for key: String) -> [String: Any] {
        [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: key
        ]
    }
}

private struct AppStoreLookupResponse: Decodable {
    let results: [AppStoreLookupEntry]
}

private struct AppStoreLookupEntry: Decodable {
    let version: String
    let trackViewUrl: String?
    let currentVersionReleaseDate: String?
}
