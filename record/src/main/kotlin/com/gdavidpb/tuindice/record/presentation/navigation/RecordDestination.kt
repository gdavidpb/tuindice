package com.gdavidpb.tuindice.record.presentation.navigation

import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import kotlinx.serialization.Serializable

@Serializable
sealed class RecordDestination : Destination() {
	@Serializable
	data object NavGraph : RecordDestination()

	@Serializable
	data object Record : RecordDestination()
}