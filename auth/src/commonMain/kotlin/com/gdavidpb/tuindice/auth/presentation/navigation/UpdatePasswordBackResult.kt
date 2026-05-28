package com.gdavidpb.tuindice.auth.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class UpdatePasswordBackResult {
	@Serializable
	data object PasswordUpdated : UpdatePasswordBackResult()
}
