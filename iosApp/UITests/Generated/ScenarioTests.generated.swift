// Generated from the scenario catalog by ./gradlew syncE2eArtifacts. Do not edit.
import XCTest

final class AuthScenarioTests: ScenarioTestCase {
    func test_auth_login_cancel() { runScenario("auth-login-cancel") }
}

final class EvaluationsScenarioTests: ScenarioTestCase {
    func test_evaluations_swipe_delete() { runScenario("evaluations-swipe-delete") }
}

final class PocScenarioTests: ScenarioTestCase {
    func test_poc_expected_failure() { runScenario("poc-expected-failure") }
}

final class SummaryScenarioTests: ScenarioTestCase {
    func test_summary_profile_picture() { runScenario("summary-profile-picture") }
}
