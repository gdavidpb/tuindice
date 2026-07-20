package com.gdavidpb.tuindice.record.presentation.navigation

import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.presentation.navigation.DialogDestination
import kotlinx.serialization.Serializable

@Serializable
sealed class RecordDestination : Destination() {
	@Serializable
	data object Record : RecordDestination()

	@Serializable
	data class CreateSyntheticTerm(
		val termId: String? = null
	) : RecordDestination()

	@Serializable
	data class DeleteSyntheticTermConfirmationDialog(
		val termId: String
	) : RecordDestination(), DialogDestination
}
