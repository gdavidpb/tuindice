package com.gdavidpb.tuindice.summary.ui

object SummaryUiTags {
	const val LoadingIndicator = "summary_loading_indicator"

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
	const val ItemsList = "summary_items_list"

	const val ProfilePictureContainer = "summary_profile_picture_container"
	const val ProfilePicturePlaceholderIcon = "summary_profile_picture_placeholder_icon"
	const val ProfilePictureEditButton = "summary_profile_picture_edit_button"
	const val ProfilePictureLoadingIndicator = "summary_profile_picture_loading_indicator"

	const val ProfilePicturePickAction = "summary_profile_picture_pick_action"
	const val ProfilePictureTakeAction = "summary_profile_picture_take_action"
	const val ProfilePictureRemoveAction = "summary_profile_picture_remove_action"
	const val RemoveProfilePictureMessage = "summary_remove_profile_picture_message"

	fun statusCard(index: Int): String = "summary_status_card_$index"
}
