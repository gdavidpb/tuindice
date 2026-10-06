import XCTest

/// OS dialogs (permission sheets, "Allow Paste") live in SpringBoard, outside the app's tree.
enum SystemUi {
    static let dismissLabels = ["Don't Allow", "No permitir", "Cancel", "Cancelar"]

    /// Registered once per test: taps the first known dismiss button of an alert that interrupts the app.
    static func installInterruptionMonitor(on testCase: XCTestCase) {
        testCase.addUIInterruptionMonitor(withDescription: "System dialogs") { alert in
            for label in dismissLabels where alert.buttons[label].exists {
                alert.buttons[label].tap()
                return true
            }
            return false
        }
    }

    /// Text of the SpringBoard alert in front of the app, or nil when there is none.
    static func dialogInFront(springboard: XCUIApplication) -> String? {
        guard springboard.state == .runningForeground || springboard.state == .runningBackground else { return nil }
        let alert = springboard.alerts.firstMatch
        guard alert.exists, let snapshot = try? alert.snapshot() else { return nil }

        let texts = snapshot.children.compactMap { child -> String? in
            let text = child.label
            return text.isEmpty ? nil : text
        }
        let title = snapshot.label
        return ([title] + texts).filter { !$0.isEmpty }.joined(separator: " | ")
    }

    /// Taps the first existing button of an "Allow Paste" style SpringBoard alert; true when one was tapped.
    static func acceptPasteAlert(springboard: XCUIApplication) -> Bool {
        guard springboard.state == .runningForeground || springboard.state == .runningBackground else { return false }
        for label in ["Allow Paste", "Permitir pegar"] {
            let button = springboard.alerts.firstMatch.buttons[label]
            if button.exists {
                button.tap()
                return true
            }
        }
        return false
    }
}
