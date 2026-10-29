package com.gdavidpb.tuindice.auth.presentation.mapper

import org.jetbrains.compose.resources.getString
import tuindice.auth.generated.resources.Res
import tuindice.auth.generated.resources.snack_password_updated

internal suspend fun snackPasswordUpdated(): String {
	return getString(Res.string.snack_password_updated)
}
