package com.gdavidpb.tuindice.summary.presentation.model

data class SummaryViewState(
	val name: String,
	val lastUpdate: String,
	val careerName: String,
	val grade: Float,
	val profilePictureUrl: String,
	val items: List<SummaryItem>,
	val isGradeVisible: Boolean,
	val isProfilePictureLoading: Boolean,
	val isLoading: Boolean,
	val isUpdated: Boolean,
	val isUpdating: Boolean
)