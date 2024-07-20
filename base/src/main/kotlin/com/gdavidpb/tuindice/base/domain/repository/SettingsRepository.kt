package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.presentation.navigation.Destination2

interface SettingsRepository {
	fun isReviewSuggested(value: Int): Boolean

	fun getLastDestination(): Destination2
	fun setLastDestination(destination: Destination2)

	fun clear()
}