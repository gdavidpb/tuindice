package com.gdavidpb.tuindice.record.presentation.navigation

import com.gdavidpb.tuindice.base.presentation.navigation.NavResult
import kotlinx.serialization.Serializable

@Serializable
sealed class DeleteSyntheticTermConfirmationResult : NavResult {
	@Serializable
	data class Confirmed(val termId: String) : DeleteSyntheticTermConfirmationResult()
}
