package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.presentation.navigation.Destination

interface SettingsRepository {
	fun isReviewSuggested(value: Int): Boolean

	fun getLastDestination(): Destination
	fun setLastDestination(destination: Destination)

	fun clear()
}