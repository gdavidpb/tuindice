package com.gdavidpb.tuindice.record.ui

object RecordUiTags {
	const val LoadingIndicator = "record_loading_indicator"
	const val ContentContainer = "record_content_container"
	const val TermSelectorRow = "record_term_selector_row"
	const val TermPager = "record_term_pager"
	const val SelectedTermSummary = "record_selected_term_summary"
	const val AttemptsList = "record_attempts_list"
	const val TermSelectionSheet = "record_term_selection_sheet"
	const val TermSelectionList = "record_term_selection_list"
	const val EnrollmentProofButton = "record_enrollment_proof_button"
	const val TopBarViewModeSwitch = "record_top_bar_view_mode_switch"
	const val TopBarViewModeButton = "record_top_bar_view_mode_button"
	const val TopBarViewModeBanner = "record_top_bar_view_mode_banner"
	const val TopBarViewModeInfoButton = "record_top_bar_view_mode_info_button"
	const val ViewModeInfoMessage = "record_view_mode_info_message"
	const val CreateSyntheticTermFab = "record_create_synthetic_term_fab"
	const val EditSyntheticTermButton = "record_edit_synthetic_term_button"
	const val DeleteSyntheticTermButton = "record_delete_synthetic_term_button"
	const val DeleteSyntheticTermMessage = "record_delete_synthetic_term_message"
	const val DiscardSyntheticTermMessage = "record_discard_synthetic_term_message"
	const val CreateSyntheticTermScreen = "record_create_synthetic_term_screen"
	const val CreateSyntheticTermContentList = "record_create_synthetic_term_content_list"
	const val CreateSyntheticTermPeriodSelector = "record_create_synthetic_term_period_selector"
	const val CreateSyntheticTermLoadInfoButton = "record_create_synthetic_term_load_info_button"
	const val CreateSyntheticTermLoadInfoMessage = "record_create_synthetic_term_load_info_message"
	const val CreateSyntheticTermSearchField = "record_create_synthetic_term_search_field"
	const val CreateSyntheticTermSearchClearButton = "record_create_synthetic_term_search_clear_button"
	const val CreateSyntheticTermSearchGuidance = "record_create_synthetic_term_search_guidance"
	const val CreateSyntheticTermSearchResultsTitle = "record_create_synthetic_term_search_results_title"
	const val CreateSyntheticTermSubmitButton = "record_create_synthetic_term_submit_button"
	const val CreateSyntheticTermSubmitError = "record_create_synthetic_term_submit_error"
	const val CreateSyntheticTermSubmitProgress = "record_create_synthetic_term_submit_progress"
	const val CreateSyntheticTermTakenSubjectsToggle = "record_create_synthetic_term_taken_subjects_toggle"
	const val CreateSyntheticTermSuggestedTab = "record_create_synthetic_term_suggested_tab"
	const val CreateSyntheticTermSearchTab = "record_create_synthetic_term_search_tab"

	const val EmptyContainer = "record_empty_container"
	const val EmptyTitle = "record_empty_title"
	const val EmptyMessage = "record_empty_message"
	const val EmptyIllustration = "record_empty_illustration"

	fun termChip(termId: String): String = "record_term_chip_$termId"
	fun termCurrentChip(termId: String): String = "record_term_current_chip_$termId"
	fun termSelectionYear(year: Int): String = "record_term_selection_year_$year"
	fun termSelectionOption(termId: String): String = "record_term_selection_option_$termId"
	fun termSelectionSelectedIcon(termId: String): String = "record_term_selection_selected_icon_$termId"
	fun termSelectionKind(termId: String): String = "record_term_selection_kind_$termId"
	fun attemptItem(attemptId: String): String = "record_attempt_item_$attemptId"
	fun attemptCard(attemptId: String): String = "record_attempt_card_$attemptId"
	fun attemptSubjectChip(attemptId: String): String = "record_attempt_subject_chip_$attemptId"
	fun attemptGradeSlider(attemptId: String): String = "record_attempt_grade_slider_$attemptId"
	fun attemptGradeValue(attemptId: String, grade: Int): String = "record_attempt_grade_value_${attemptId}_$grade"
	fun attemptStatusChip(attemptId: String): String = "record_attempt_status_chip_$attemptId"
	fun attemptStatusSelector(attemptId: String): String = "record_attempt_status_selector_$attemptId"
	fun attemptStatusOption(attemptId: String, status: String): String = "record_attempt_status_option_${attemptId}_$status"
	fun attemptStatusValue(attemptId: String, status: String): String = "record_attempt_status_value_${attemptId}_$status"
	fun createSyntheticTermSubject(subjectCode: String): String = "record_create_synthetic_term_subject_$subjectCode"
	fun createSyntheticTermSearchResult(index: Int, subjectCode: String): String =
		"record_create_synthetic_term_search_result_${index}_subject_$subjectCode"

	fun createSyntheticTermSearchExample(index: Int): String =
		"record_create_synthetic_term_search_example_$index"

	fun createSyntheticTermSubjectStatus(subjectCode: String, status: String): String =
		"record_create_synthetic_term_subject_${subjectCode}_status_$status"

	fun createSyntheticTermSubjectStatusTooltip(subjectCode: String): String =
		"record_create_synthetic_term_subject_${subjectCode}_status_tooltip"

	fun createSyntheticTermSubjectAction(subjectCode: String, action: String): String =
		"record_create_synthetic_term_subject_${subjectCode}_${action}_button"

	fun createSyntheticTermSubjectStatsButton(subjectCode: String): String =
		"record_create_synthetic_term_subject_${subjectCode}_stats_button"

	fun createSyntheticTermPeriodOption(termKey: String): String = "record_create_synthetic_term_period_option_$termKey"
}
