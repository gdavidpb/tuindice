package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.presentation.navigation.Destination

interface SettingsRepository {
	suspend fun isReviewSuggested(value: Int): Boolean

	suspend fun getLastDestination(): Destination
	suspend fun setLastDestination(destination: Destination)

	suspend fun clear()
}