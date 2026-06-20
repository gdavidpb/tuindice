package com.gdavidpb.tuindice.base.domain.model

sealed interface UpdateLaunchResult {
	data object Launched : UpdateLaunchResult

	data class OpenStoreFallback(
		val primaryUrl: String,
		val fallbackUrl: String
	) : UpdateLaunchResult
}
