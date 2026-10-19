package com.gdavidpb.tuindice.record.presentation.mapper

import org.jetbrains.compose.resources.getString
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.snack_synthetic_term_delete_failed
import tuindice.record.generated.resources.snack_synthetic_term_deleted

internal suspend fun snackSyntheticTermDeleteFailed(): String {
	return getString(Res.string.snack_synthetic_term_delete_failed)
}

internal suspend fun snackSyntheticTermDeleted(): String {
	return getString(Res.string.snack_synthetic_term_deleted)
}
