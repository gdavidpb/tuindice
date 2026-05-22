package com.gdavidpb.tuindice.record.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class DeleteSyntheticTermConfirmationResult {
	@Serializable
	data class Confirmed(val termId: String) : DeleteSyntheticTermConfirmationResult()
}
