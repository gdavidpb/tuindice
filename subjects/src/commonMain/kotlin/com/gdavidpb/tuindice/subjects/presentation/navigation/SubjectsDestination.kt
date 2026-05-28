package com.gdavidpb.tuindice.subjects.presentation.navigation

import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import kotlinx.serialization.Serializable

@Serializable
sealed class SubjectsDestination : Destination() {
	@Serializable
	data object SubjectSearch : SubjectsDestination()

	@Serializable
	data class SubjectDetail(
		val subjectCode: String
	) : SubjectsDestination()
}
