// Generated from the scenario catalog by ./gradlew syncE2eArtifacts. Do not edit.
import XCTest

final class AboutScenarioTests: ScenarioTestCase {
    func test_about_external_url_links() { runScenario("about-external-url-links") }
    func test_about_internal_browser_links() { runScenario("about-internal-browser-links") }
    func test_about_platform_edge_triggers() { runScenario("about-platform-edge-triggers") }
    func test_about_smoke() { runScenario("about-smoke") }
    func test_about_usage_data_consent() { runScenario("about-usage-data-consent") }
}

final class AuthScenarioTests: ScenarioTestCase {
    func test_auth_login_cancel() { runScenario("auth-login-cancel") }
    func test_auth_login_disabled() { runScenario("auth-login-disabled") }
    func test_auth_login_invalid() { runScenario("auth-login-invalid") }
    func test_auth_login_outdated_app() { runScenario("auth-login-outdated-app") }
    func test_auth_login_password_toggle() { runScenario("auth-login-password-toggle") }
    func test_auth_login_retry_after_unavailable() { runScenario("auth-login-retry-after-unavailable") }
    func test_auth_login_success() { runScenario("auth-login-success") }
    func test_auth_login_usb_email() { runScenario("auth-login-usb-email") }
    func test_auth_login_usb_email_usbid() { runScenario("auth-login-usb-email-usbid") }
    func test_auth_pending_sign_out() { runScenario("auth-pending-sign-out") }
    func test_auth_pending_sign_out_flush_success() { runScenario("auth-pending-sign-out-flush-success") }
    func test_auth_session_invalidated() { runScenario("auth-session-invalidated") }
    func test_auth_sign_out() { runScenario("auth-sign-out") }
    func test_auth_sign_out_cancel() { runScenario("auth-sign-out-cancel") }
    func test_auth_terms_privacy_from_login() { runScenario("auth-terms-privacy-from-login") }
    func test_auth_update_password() { runScenario("auth-update-password") }
    func test_auth_update_password_failure() { runScenario("auth-update-password-failure") }
    func test_auth_usage_data_consent() { runScenario("auth-usage-data-consent") }
}

final class CoachmarksScenarioTests: ScenarioTestCase {
    func test_coachmarks_contextual_summary() { runScenario("coachmarks-contextual-summary") }
    func test_coachmarks_progressive_record() { runScenario("coachmarks-progressive-record") }
}

final class ConformanceScenarioTests: ScenarioTestCase {
    func test_conformance_backend() { runScenario("conformance-backend") }
    func test_conformance_double_tap_swipe() { runScenario("conformance-double-tap-swipe") }
    func test_conformance_enabled() { runScenario("conformance-enabled") }
    func test_conformance_foreground() { runScenario("conformance-foreground") }
    func test_conformance_launch_clean() { runScenario("conformance-launch-clean") }
    func test_conformance_launch_seeded() { runScenario("conformance-launch-seeded") }
    func test_conformance_mock_state() { runScenario("conformance-mock-state") }
    func test_conformance_scroll() { runScenario("conformance-scroll") }
    func test_conformance_scroll_horizontal() { runScenario("conformance-scroll-horizontal") }
    func test_conformance_secure_field() { runScenario("conformance-secure-field") }
    func test_conformance_sheet_tags() { runScenario("conformance-sheet-tags") }
    func test_conformance_submit_search() { runScenario("conformance-submit-search") }
    func test_conformance_submit_text_entry() { runScenario("conformance-submit-text-entry") }
    func test_conformance_swipe_from_element() { runScenario("conformance-swipe-from-element") }
    func test_conformance_tap_at() { runScenario("conformance-tap-at") }
    func test_conformance_text_query() { runScenario("conformance-text-query") }
    func test_conformance_type_readback() { runScenario("conformance-type-readback") }
    func test_conformance_type_replace() { runScenario("conformance-type-replace") }
    func test_conformance_type_replace_after_back() { runScenario("conformance-type-replace-after-back") }
}

final class EnrollmentproofScenarioTests: ScenarioTestCase {
    func test_enrollmentproof_annulled_not_found() { runScenario("enrollmentproof-annulled-not-found") }
    func test_enrollmentproof_error_unavailable() { runScenario("enrollmentproof-error-unavailable") }
    func test_enrollmentproof_fetching_cancel() { runScenario("enrollmentproof-fetching-cancel") }
    func test_enrollmentproof_not_found() { runScenario("enrollmentproof-not-found") }
    func test_enrollmentproof_outdated_credentials() { runScenario("enrollmentproof-outdated-credentials") }
    func test_enrollmentproof_smoke() { runScenario("enrollmentproof-smoke") }
}

final class EvaluationsScenarioTests: ScenarioTestCase {
    func test_evaluations_add_submit() { runScenario("evaluations-add-submit") }
    func test_evaluations_add_validation() { runScenario("evaluations-add-validation") }
    func test_evaluations_annulled_no_attempts() { runScenario("evaluations-annulled-no-attempts") }
    func test_evaluations_annulled_provisional_notice() { runScenario("evaluations-annulled-provisional-notice") }
    func test_evaluations_edit_submit() { runScenario("evaluations-edit-submit") }
    func test_evaluations_enrollment_unavailable() { runScenario("evaluations-enrollment-unavailable") }
    func test_evaluations_filters_and_form() { runScenario("evaluations-filters-and-form") }
    func test_evaluations_grade_from_list() { runScenario("evaluations-grade-from-list") }
    func test_evaluations_not_enrolled() { runScenario("evaluations-not-enrolled") }
    func test_evaluations_smoke() { runScenario("evaluations-smoke") }
    func test_evaluations_swipe_delete() { runScenario("evaluations-swipe-delete") }
}

final class MaincoreScenarioTests: ScenarioTestCase {
    func test_maincore_app_availability_notice() { runScenario("maincore-app-availability-notice") }
    func test_maincore_back_stack() { runScenario("maincore-back-stack") }
    func test_maincore_bottom_bar_state() { runScenario("maincore-bottom-bar-state") }
    func test_maincore_browser_external_dialog() { runScenario("maincore-browser-external-dialog") }
    func test_maincore_tab_stack_preservation() { runScenario("maincore-tab-stack-preservation") }
}

final class PensumScenarioTests: ScenarioTestCase {
    func test_pensum_cache_refresh_failed() { runScenario("pensum-cache-refresh-failed") }
    func test_pensum_current_absent() { runScenario("pensum-current-absent") }
    func test_pensum_detail_navigation() { runScenario("pensum-detail-navigation") }
    func test_pensum_equivalence_fulfilled() { runScenario("pensum-equivalence-fulfilled") }
    func test_pensum_node_detail() { runScenario("pensum-node-detail") }
    func test_pensum_record_unavailable() { runScenario("pensum-record-unavailable") }
    func test_pensum_refresh_failed_retry() { runScenario("pensum-refresh-failed-retry") }
    func test_pensum_refresh_not_found() { runScenario("pensum-refresh-not-found") }
    func test_pensum_selection() { runScenario("pensum-selection") }
    func test_pensum_smoke() { runScenario("pensum-smoke") }
}

final class RecordScenarioTests: ScenarioTestCase {
    func test_record_annulled_final_notice() { runScenario("record-annulled-final-notice") }
    func test_record_annulled_provisional_schedule() { runScenario("record-annulled-provisional-schedule") }
    func test_record_attempt_overrides() { runScenario("record-attempt-overrides") }
    func test_record_refresh_retry() { runScenario("record-refresh-retry") }
    func test_record_schedule_view_remembered() { runScenario("record-schedule-view-remembered") }
    func test_record_smoke() { runScenario("record-smoke") }
    func test_record_stale_enrollment_notice() { runScenario("record-stale-enrollment-notice") }
    func test_record_synthetic_term_discard() { runScenario("record-synthetic-term-discard") }
    func test_record_synthetic_term_lifecycle() { runScenario("record-synthetic-term-lifecycle") }
    func test_record_synthetic_term_rejected() { runScenario("record-synthetic-term-rejected") }
    func test_record_synthetic_term_search_empty() { runScenario("record-synthetic-term-search-empty") }
    func test_record_synthetic_term_search_states() { runScenario("record-synthetic-term-search-states") }
    func test_record_term_selection() { runScenario("record-term-selection") }
    func test_record_withdrawn_subject() { runScenario("record-withdrawn-subject") }
}

final class SubjectsScenarioTests: ScenarioTestCase {
    func test_subjects_detail_failed_retry() { runScenario("subjects-detail-failed-retry") }
    func test_subjects_detail_tabs_tooltip() { runScenario("subjects-detail-tabs-tooltip") }
    func test_subjects_detail_unavailable() { runScenario("subjects-detail-unavailable") }
    func test_subjects_search_failed_retry() { runScenario("subjects-search-failed-retry") }
    func test_subjects_search_query_clear() { runScenario("subjects-search-query-clear") }
    func test_subjects_smoke() { runScenario("subjects-smoke") }
}

final class SummaryScenarioTests: ScenarioTestCase {
    func test_summary_new_student_no_record() { runScenario("summary-new-student-no-record") }
    func test_summary_outdated_credentials() { runScenario("summary-outdated-credentials") }
    func test_summary_partial_enrollment_status_dialog() { runScenario("summary-partial-enrollment-status-dialog") }
    func test_summary_profile_picture() { runScenario("summary-profile-picture") }
    func test_summary_record_access_denied() { runScenario("summary-record-access-denied") }
    func test_summary_refresh_retry() { runScenario("summary-refresh-retry") }
    func test_summary_smoke() { runScenario("summary-smoke") }
    func test_summary_status_dialog() { runScenario("summary-status-dialog") }
}
