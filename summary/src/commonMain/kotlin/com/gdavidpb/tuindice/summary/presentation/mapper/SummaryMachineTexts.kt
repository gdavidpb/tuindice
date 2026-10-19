package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import org.jetbrains.compose.resources.getString
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.share_term_unavailable_message
import tuindice.summary.generated.resources.share_term_unavailable_title
import tuindice.summary.generated.resources.snack_profile_picture_removed
import tuindice.summary.generated.resources.snack_profile_picture_updated
import tuindice.summary.generated.resources.summary_statistics_unavailable_message
import tuindice.summary.generated.resources.summary_statistics_unavailable_title

internal fun shareTermUnavailableMessage(): UiText {
	return UiText.Resource(Res.string.share_term_unavailable_message)
}

internal fun shareTermUnavailableTitle(): UiText {
	return UiText.Resource(Res.string.share_term_unavailable_title)
}

internal suspend fun snackProfilePictureRemoved(): String {
	return getString(Res.string.snack_profile_picture_removed)
}

internal suspend fun snackProfilePictureUpdated(): String {
	return getString(Res.string.snack_profile_picture_updated)
}

internal fun summaryStatisticsUnavailableMessage(): UiText {
	return UiText.Resource(Res.string.summary_statistics_unavailable_message)
}

internal fun summaryStatisticsUnavailableTitle(): UiText {
	return UiText.Resource(Res.string.summary_statistics_unavailable_title)
}
