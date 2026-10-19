package com.gdavidpb.tuindice.summary.presentation.mapper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.FindInPage
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.ui.graphics.vector.ImageVector
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantNavTarget
import com.gdavidpb.tuindice.base.ui.icon.ShareIcon

// The mark of each screen an action can open, the same screens the chat's actions open.
internal fun AssistantNavTarget.toActionIcon(): ImageVector {
	return when (this) {
		AssistantNavTarget.RECORD_PROJECTION -> Icons.AutoMirrored.Outlined.TrendingUp
		AssistantNavTarget.RECORD_SCHEDULE, AssistantNavTarget.SUBJECTS_SEARCH -> Icons.Outlined.AccountTree
		AssistantNavTarget.SHARE_TERM -> ShareIcon
		AssistantNavTarget.STATISTICS -> Icons.Outlined.BarChart
		AssistantNavTarget.PENSUM, AssistantNavTarget.SUBJECTS_DETAIL -> Icons.Outlined.School
		AssistantNavTarget.RECORD, AssistantNavTarget.RECORD_CREATE_TERM -> Icons.Outlined.TableChart
		AssistantNavTarget.EVALUATIONS, AssistantNavTarget.EVALUATIONS_ADD, AssistantNavTarget.EVALUATIONS_DETAIL ->
			Icons.AutoMirrored.Outlined.Assignment
		AssistantNavTarget.ENROLLMENT_PROOF -> Icons.Outlined.FindInPage
		AssistantNavTarget.SUMMARY -> Icons.Outlined.Dashboard
		AssistantNavTarget.ABOUT -> Icons.Outlined.Info
		AssistantNavTarget.SIGN_IN, AssistantNavTarget.UPDATE_PASSWORD -> Icons.Outlined.Lock
	}
}
