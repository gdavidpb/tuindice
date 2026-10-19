package com.gdavidpb.tuindice.summary.ui

object SummaryUiTags {
	const val LoadingIndicator = "summary_loading_indicator"

	const val NewStudentContainer = "summary_new_student_container"
	const val NewStudentTitle = "summary_new_student_title"
	const val NewStudentMessage = "summary_new_student_message"
	const val NewStudentRetryButton = "summary_new_student_retry_button"

	const val ContentContainer = "summary_content_container"
	const val GradeText = "summary_grade_text"
	const val NameText = "summary_name_text"
	const val CareerText = "summary_career_text"
	const val StatusRow = "summary_status_row"
	const val StatusIcon = "summary_status_icon"
	const val StatusIconButton = "summary_status_icon_button"
	const val StatusIconHalo = "summary_status_icon_halo"
	const val StatusText = "summary_status_text"
	const val SyncStatusMessage = "summary_sync_status_message"

	// The hub under the profile: the attention, the week, the term and the career.
	const val ItemsList = "summary_items_list"

	const val AttentionContainer = "summary_attention_container"

	const val WeekSection = "summary_week_section"
	const val WeekClassRow = "summary_week_class_row"
	const val WeekClassPill = "summary_week_class_pill"
	const val WeekCard = "summary_week_card"

	const val TermSection = "summary_term_section"
	const val TermWeekText = "summary_term_week_text"
	const val TermWeekTrack = "summary_term_week_track"
	const val TermWeekRemaining = "summary_term_week_remaining"
	const val TermSummary = "summary_term_summary"
	const val TermCard = "summary_term_card"
	const val TermFinishedCard = "summary_term_finished_card"
	const val TermFinishedName = "summary_term_finished_name"
	const val TermFinishedIndex = "summary_term_finished_index"
	const val TermFinishedCredits = "summary_term_finished_credits"
	const val TermFinishedShare = "summary_term_finished_share"

	// The button of a card that offers one (the invitations to add an evaluation or to plan a term).
	const val CardAction = "summary_card_action"

	const val CareerSection = "summary_career_section"
	const val CareerCard = "summary_career_card"
	const val CareerProgressBar = "summary_career_progress_bar"
	const val CareerPercentText = "summary_career_percent_text"
	const val CareerInviteCard = "summary_career_invite_card"

	// The section of the statistics under the career: its card with figures, its invitation and its informative card, and
	// what the card holds: the figure of the cumulative average, its line and its phrase.
	const val StatisticsSection = "summary_statistics_section"
	const val StatisticsCard = "summary_statistics_card"
	const val StatisticsInviteCard = "summary_statistics_invite_card"
	const val StatisticsEmptyCard = "summary_statistics_empty_card"
	const val StatisticsFigureText = "summary_statistics_figure_text"
	const val StatisticsSparkline = "summary_statistics_sparkline"
	const val StatisticsPhrase = "summary_statistics_phrase"

	const val ProfilePictureContainer = "summary_profile_picture_container"
	const val ProfilePicturePlaceholderIcon = "summary_profile_picture_placeholder_icon"
	const val ProfilePicturePreviewImage = "summary_profile_picture_preview_image"
	const val ProfilePictureContinuityImage = "summary_profile_picture_continuity_image"
	// The round avatar of the shell and of the Settings card: the stored picture over the neutral person icon.
	const val ProfileAvatarImage = "summary_profile_avatar_image"
	const val ProfileAvatarFallback = "summary_profile_avatar_fallback"
	const val ProfilePictureEditButton = "summary_profile_picture_edit_button"
	const val ProfilePictureLoadingIndicator = "summary_profile_picture_loading_indicator"
	const val ProfilePictureRemoteLoadingIndicator = "summary_profile_picture_remote_loading_indicator"

	const val ProfilePicturePickAction = "summary_profile_picture_pick_action"
	const val ProfilePictureTakeAction = "summary_profile_picture_take_action"
	const val ProfilePictureRemoveAction = "summary_profile_picture_remove_action"
	const val RemoveProfilePictureMessage = "summary_remove_profile_picture_message"

	// The edges a statistics chart fades where more of it lies beyond, and the label of its reference line.
	const val StatisticsLeadingEdge = "summary_statistics_leading_edge"
	const val StatisticsTrailingEdge = "summary_statistics_trailing_edge"
	const val StatisticsReferenceLabel = "summary_statistics_reference_label"

	// The shared card of a term: the card itself, its index and its credit line.
	const val ShareTermCard = "summary_share_term_card"
	const val ShareTermCardLogo = "summary_share_term_card_logo"
	const val ShareTermCardIndex = "summary_share_term_card_index"
	const val ShareTermCardCredit = "summary_share_term_card_credit"

	// The sheet that shares a closed term: its terms, its card, its backgrounds and its share button.
	const val ShareTermSheet = "summary_share_term_sheet"
	const val ShareTermLoadingIndicator = "summary_share_term_loading_indicator"
	const val ShareTermTerms = "summary_share_term_terms"
	const val ShareTermEmpty = "summary_share_term_empty"
	const val ShareTermPreview = "summary_share_term_preview"
	const val ShareTermBackgrounds = "summary_share_term_backgrounds"
	const val ShareTermShareButton = "summary_share_term_share_button"
	const val ShareTermUnavailable = "summary_share_term_unavailable"
	const val ShareTermUnavailableTitle = "summary_share_term_unavailable_title"
	const val ShareTermUnavailableMessage = "summary_share_term_unavailable_message"

	fun attentionItem(key: String): String = "summary_attention_item_$key"

	fun weekEvaluationRow(evaluationId: String): String = "summary_week_evaluation_row_$evaluationId"

	fun termPointsRow(subjectCode: String): String = "summary_term_points_row_$subjectCode"

	fun termPointsBar(subjectCode: String): String = "summary_term_points_bar_$subjectCode"

	// The part of the bar evaluated but not earned yet, which follows the earned part at a lighter tint. The earned part
	// has its own tag, in SummaryEvaluatedBarUiTags.
	fun termPointsBarFill(subjectCode: String): String = "summary_term_points_bar_fill_$subjectCode"

	// The header of a section: its title and its link, named after the section's own tag.
	fun sectionTitle(sectionTag: String): String = "summary_section_title_$sectionTag"

	fun sectionLink(sectionTag: String): String = "summary_section_link_$sectionTag"

	// A statistics chart is named by its key, and so are the touch targets and the drawn fills of its points.
	fun statisticsChart(key: String): String = "summary_statistics_$key"

	fun statisticsTarget(key: String, index: Int): String = "summary_statistics_${key}_target_$index"

	// The coloured part of a point: a bar, or a segment of a stacked bar.
	fun statisticsFill(key: String, index: Int): String = "summary_statistics_${key}_fill_$index"

	// The Statistics screen: its content, its unavailable message, and the six blocks that hold its figures in order.
	const val StatisticsContent = "summary_statistics_content"
	const val StatisticsUnavailable = "summary_statistics_unavailable"
	const val StatisticsUnavailableTitle = "summary_statistics_unavailable_title"
	const val StatisticsUnavailableMessage = "summary_statistics_unavailable_message"
	const val StatisticsCurrentTermBlock = "summary_statistics_current_term_block"
	const val StatisticsIndexBlock = "summary_statistics_index_block"
	const val StatisticsSubjectsBlock = "summary_statistics_subjects_block"
	const val StatisticsCreditsBlock = "summary_statistics_credits_block"
	const val StatisticsGradesBlock = "summary_statistics_grades_block"
	const val StatisticsPaceBlock = "summary_statistics_pace_block"

	// The parts a block is made of: its figure, its compact facts, the line that says what it lacks, and its legend.
	const val StatisticsFigure = "summary_statistics_figure"
	const val StatisticsCaption = "summary_statistics_caption"
	const val StatisticsEmpty = "summary_statistics_empty"
	const val StatisticsEmptyAction = "summary_statistics_empty_action"
	const val StatisticsEmptyIcon = "summary_statistics_empty_icon"
	const val StatisticsBestTerm = "summary_statistics_best_term"
	const val StatisticsWorstTerm = "summary_statistics_worst_term"
	const val StatisticsFacts = "summary_statistics_facts"
	const val StatisticsLegend = "summary_statistics_legend"
	const val StatisticsOverdue = "summary_statistics_overdue"
	const val StatisticsProjection = "summary_statistics_projection"
	const val StatisticsProjectionTag = "summary_statistics_projection_tag"

	// A track is a share of a whole drawn as a thin bar: the track itself and the part of it that is filled.
	const val StatisticsTrackFill = "summary_statistics_track_fill"
	const val StatisticsPensumTrack = "summary_statistics_pensum_track"
}
