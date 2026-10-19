package com.gdavidpb.tuindice.base.utils

object DefaultRemoteConfig {
	private val DEFAULT_LOADING_MESSAGES = listOf(
		"Calculando la resolvente…",
		"Calculando el campo magnético…",
		"Midiendo los anillos de Newton…",
		"Haciendo la transformada de Fourier…",
		"Haciendo la transformada de Laplace…",
		"Rompiendo indeterminaciones con L'Hopital…",
		"Ejecutando algoritmo de Dijkstra…",
		"Aplicando la 1ra Ley de Newton…",
		"Aplicando la 2da Ley de Newton…",
		"Aplicando la 3ra Ley de Newton…",
		"Aplicando Gauss-Jordan…",
		"Factorizando…",
		"Integrando…",
		"Derivando…",
		"Cancelando términos…"
	)

	fun values(profile: RemoteConfigDefaultsProfile): DefaultRemoteConfigValues {
		return when (profile) {
			RemoteConfigDefaultsProfile.PRODUCTION -> PRODUCTION_VALUES
			RemoteConfigDefaultsProfile.DEBUG -> DEBUG_VALUES
		}
	}

	private val PRODUCTION_VALUES = DefaultRemoteConfigValues(
		timeoutMillis = 90_000L,
		contactEmail = "info@tuindice.app",
		contactSubject = "TuIndice - Contacto",
		loadingMessages = DEFAULT_LOADING_MESSAGES,
		updateStalenessDays = 7,
		syncsToSuggestReview = 3,
		attestationAndroidEnforcementEnabled = false,
		attestationIosEnforcementEnabled = true,
		appAvailabilityNoticeEnabled = false,
		appAvailabilityNoticeTitle = "",
		appAvailabilityNoticeMessage = "",
		assistantMode = "off"
	)

	private val DEBUG_VALUES = PRODUCTION_VALUES.copy(assistantMode = "on")
}
