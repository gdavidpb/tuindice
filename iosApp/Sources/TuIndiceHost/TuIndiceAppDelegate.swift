import Foundation
import UIKit
import UserNotifications
#if canImport(FirebaseCore)
import FirebaseCore
#endif
#if canImport(FirebaseMessaging)
import FirebaseMessaging
#endif

enum TuIndiceFirebaseRuntimeState {
    static var isConfigured = false
}

final class TuIndiceAppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        configureFirebaseIfNeeded()
        configureRemoteNotifications(application: application)
        return true
    }

    func application(
        _ application: UIApplication,
        didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    ) {
        #if canImport(FirebaseMessaging)
        if TuIndiceFirebaseRuntimeState.isConfigured {
            Messaging.messaging().apnsToken = deviceToken
        }
        #endif
        TuIndiceAppBootstrap.updatePushToken(deviceToken.hexString)
    }

    func application(
        _ application: UIApplication,
        didFailToRegisterForRemoteNotificationsWithError error: Error
    ) {
        TuIndiceAppBootstrap.updatePushToken(nil)
    }

    private func configureFirebaseIfNeeded() {
        #if canImport(FirebaseCore)
        guard TuIndiceAppBootstrap.shouldUseFirebaseServices else {
            NSLog("Firebase disabled for debug build: using debug platform mocks.")
            return
        }

        guard TuIndiceFirebaseRuntimeState.isConfigured == false else { return }

        guard let optionsPath = Bundle.main.path(
            forResource: "GoogleService-Info",
            ofType: "plist"
        ), let options = FirebaseOptions(contentsOfFile: optionsPath) else {
            NSLog(
                "Firebase disabled for this run: missing GoogleService-Info.plist in app bundle."
            )
            return
        }

        guard isValidFirebaseOptions(options) else {
            NSLog(
                "Firebase disabled for this run: invalid GoogleService-Info.plist for this bundle."
            )
            return
        }

        FirebaseApp.configure(options: options)
        TuIndiceFirebaseRuntimeState.isConfigured = true
        #endif
    }

    private func configureRemoteNotifications(application: UIApplication) {
        guard TuIndiceAppBootstrap.shouldUseFirebaseServices else {
            TuIndiceAppBootstrap.updatePushToken(nil)
            return
        }

        guard TuIndiceFirebaseRuntimeState.isConfigured else {
            TuIndiceAppBootstrap.updatePushToken(nil)
            return
        }

        #if canImport(FirebaseMessaging)
        Messaging.messaging().delegate = self
        #endif

        UNUserNotificationCenter.current().requestAuthorization(
            options: [.alert, .badge, .sound]
        ) { _, _ in
            DispatchQueue.main.async {
                application.registerForRemoteNotifications()
            }
        }
    }

    #if canImport(FirebaseCore)
    private func isValidFirebaseOptions(_ options: FirebaseOptions) -> Bool {
        guard
            let bundleIdentifier = Bundle.main.bundleIdentifier,
            options.bundleID == bundleIdentifier,
            isValidFirebaseApiKey(options.apiKey),
            isValidFirebaseAppID(options.googleAppID),
            isValidFirebaseSenderID(options.gcmSenderID)
        else {
            return false
        }

        return true
    }

    private func isValidFirebaseApiKey(_ value: String?) -> Bool {
        guard let value else { return false }
        return value.hasPrefix("AIza")
            && value.count >= 30
            && containsPlaceholderMarker(value) == false
    }

    private func isValidFirebaseAppID(_ value: String?) -> Bool {
        guard let value else { return false }
        return value.range(
            of: #"^1:[1-9][0-9]*:ios:[0-9a-fA-F]+$"#,
            options: .regularExpression
        ) != nil
    }

    private func isValidFirebaseSenderID(_ value: String?) -> Bool {
        guard let value else { return false }
        return value.allSatisfy(\.isNumber)
            && value.allSatisfy { $0 == "0" } == false
    }

    private func containsPlaceholderMarker(_ value: String) -> Bool {
        let normalizedValue = value.lowercased()
        return normalizedValue.contains("placeholder")
            || normalizedValue.contains("debugonly")
            || normalizedValue.contains("000000")
    }
    #endif
}

#if canImport(FirebaseMessaging)
extension TuIndiceAppDelegate: MessagingDelegate {
    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        TuIndiceAppBootstrap.updatePushToken(fcmToken)
    }
}
#endif

private extension Data {
    var hexString: String {
        map { String(format: "%02x", $0) }.joined()
    }
}
