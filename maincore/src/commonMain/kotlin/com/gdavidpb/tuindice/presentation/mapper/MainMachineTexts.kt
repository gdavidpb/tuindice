package com.gdavidpb.tuindice.presentation.mapper

import org.jetbrains.compose.resources.getString
import tuindice.maincore.generated.resources.Res
import tuindice.maincore.generated.resources.snack_pending_changes_unavailable

internal suspend fun snackPendingChangesUnavailable(): String {
	return getString(Res.string.snack_pending_changes_unavailable)
}
