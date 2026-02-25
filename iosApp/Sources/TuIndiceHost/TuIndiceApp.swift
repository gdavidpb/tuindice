import SwiftUI
import UIKit

@main
struct TuIndiceApp: App {
    @UIApplicationDelegateAdaptor(TuIndiceAppDelegate.self)
    private var appDelegate

    init() {
        TuIndiceAppBootstrap.start()
    }

    var body: some Scene {
        WindowGroup {
            TuIndiceRootHostView()
        }
    }
}

private struct TuIndiceRootHostView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        TuIndiceAppBootstrap.makeRootViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
