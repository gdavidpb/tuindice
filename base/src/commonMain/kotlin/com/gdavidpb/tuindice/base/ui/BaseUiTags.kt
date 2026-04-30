package com.gdavidpb.tuindice.base.ui

import com.gdavidpb.tuindice.base.presentation.model.TopBarAction

object BaseUiTags {
	const val EmptyViewContainer = "base_empty_view_container"
	const val EmptyViewTitle = "base_empty_view_title"
	const val EmptyViewMessage = "base_empty_view_message"
	const val EmptyViewActionButton = "base_empty_view_action_button"

	const val ErrorViewContainer = "base_error_view_container"
	const val ErrorViewTitle = "base_error_view_title"
	const val ErrorViewMessage = "base_error_view_message"
	const val ErrorViewRetryButton = "base_error_view_retry_button"

	const val DropdownMenuTextField = "base_dropdown_text_field"
	const val DropdownMenuError = "base_dropdown_error"

	const val TopAppBarActionsContainer = "base_top_app_bar_actions_container"
	const val TopAppBarTitle = "base_top_app_bar_title"

	const val ConfirmationDialogSheet = "base_confirmation_dialog_sheet"
	const val ConfirmationDialogTitle = "base_confirmation_dialog_title"
	const val ConfirmationDialogNegativeButton = "base_confirmation_dialog_negative_button"
	const val ConfirmationDialogPositiveButton = "base_confirmation_dialog_positive_button"
	const val ConfirmationDialogPositiveLoading = "base_confirmation_dialog_positive_loading"
	const val ConfirmationDialogEntry = "base_confirmation_dialog_entry"
	const val ExternalResourceMessage = "base_external_resource_message"
	const val ExternalResourceUrl = "base_external_resource_url"

	const val WheelPickerList = "base_wheel_picker_list"
	const val EmptyStateAnimation = "base_empty_state_animation"
	const val ErrorStateAnimation = "base_error_state_animation"
	const val StatsLoadingAnimation = "base_stats_loading_animation"

	fun topBarActionButton(action: TopBarAction): String =
		"base_top_app_bar_action_${action.action}"

	fun dropdownItem(index: Int): String =
		"base_dropdown_item_$index"

	fun wheelPickerItem(index: Int): String =
		"base_wheel_picker_item_$index"
}
