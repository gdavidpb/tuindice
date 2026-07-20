package com.gdavidpb.tuindice.auth.presentation.navigation

import com.gdavidpb.tuindice.base.presentation.navigation.NavResult
import kotlinx.serialization.Serializable

@Serializable
sealed class UpdatePasswordBackResult : NavResult {
	@Serializable
	data object PasswordUpdated : UpdatePasswordBackResult()
}
