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

        FirebaseApp.configure(options: options)
        TuIndiceFirebaseRuntimeState.isConfigured = true
        #endif
    }

    private func configureRemoteNotifications(application: UIApplication) {
        guard TuIndiceAppBootstrap.shouldUseFirebaseServices else {
            TuIndiceAppBootstrap.updatePushToken(nil)
            return
        }

        #if canImport(FirebaseMessaging)
        if TuIndiceFirebaseRuntimeState.isConfigured {
            Messaging.messaging().delegate = self
        }
        #endif

        UNUserNotificationCenter.current().requestAuthorization(
            options: [.alert, .badge, .sound]
        ) { _, _ in
            DispatchQueue.main.async {
                application.registerForRemoteNotifications()
            }
        }
    }
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
