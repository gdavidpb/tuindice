package com.gdavidpb.tuindice.base.utils

enum class RemoteConfigDefaultsProfile {
	PRODUCTION,
	DEBUG
}

data class DefaultRemoteConfigValues(
	val timeoutMillis: Long,
	val contactEmail: String,
	val contactSubject: String,
	val loadingMessages: List<String>,
	val updateStalenessDays: Int,
	val syncsToSuggestReview: Int,
	val appAvailabilityNoticeEnabled: Boolean,
	val appAvailabilityNoticeTitle: String,
	val appAvailabilityNoticeMessage: String
)

object RemoteConfigKeys {
	const val CONTACT_EMAIL = "contact_email"
	const val CONTACT_SUBJECT = "contact_subject"
	const val LOADING_MESSAGES = "loading_messages"
	const val TIME_UPDATE_STALENESS_DAYS = "time_update_staleness_days"
	const val SYNCS_TO_SUGGEST_REVIEW = "syncs_to_suggest_review"
	const val TIME_OUT_CONNECTION = "time_out_connection"
	const val APP_AVAILABILITY_NOTICE_ENABLED = "app_availability_notice_enabled"
	const val APP_AVAILABILITY_NOTICE_TITLE = "app_availability_notice_title"
	const val APP_AVAILABILITY_NOTICE_MESSAGE = "app_availability_notice_message"
}

object DefaultRemoteConfig {
	private val DEFAULT_LOADING_MESSAGES = listOf(
		"Calculando la resolvente...",
		"Calculando el campo magnético...",
		"Midiendo los anillos de Newton...",
		"Haciendo la transformada de Fourier...",
		"Haciendo la transformada de Laplace...",
		"Rompiendo indeterminaciones con L'Hopital...",
		"Ejecutando algoritmo de Dijkstra...",
		"Aplicando la 1ra Ley de Newton...",
		"Aplicando la 2da Ley de Newton...",
		"Aplicando la 3ra Ley de Newton...",
		"Aplicando Gauss-Jordan...",
		"Factorizando...",
		"Integrando...",
		"Derivando...",
		"Cancelando términos..."
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
		appAvailabilityNoticeEnabled = false,
		appAvailabilityNoticeTitle = "",
		appAvailabilityNoticeMessage = ""
	)

	private val DEBUG_VALUES = PRODUCTION_VALUES
}
