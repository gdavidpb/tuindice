package com.gdavidpb.tuindice.record.presentation.navigation

import com.gdavidpb.tuindice.base.presentation.navigation.Destination2
import kotlinx.serialization.Serializable

@Serializable
sealed class RecordDestination : Destination2() {
	@Serializable
	data object NavGraph : RecordDestination()

	@Serializable
	data object Record : RecordDestination()
}