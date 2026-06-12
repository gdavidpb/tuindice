package $PACKAGE.presentation.machine

/**
 * Internal machine inputs: async use-case results re-enter the table as input
 * symbols, split per outcome so every row keeps a fixed declared target.
 */
sealed interface $INTERNAL_EVENT_NAME {
	data class $CONTENT_OBSERVED_EVENT_NAME(
		val message: String
	) : $INTERNAL_EVENT_NAME

	data class $OBSERVATION_FAILED_EVENT_NAME(
		val message: String
	) : $INTERNAL_EVENT_NAME

	data object $REFRESH_STARTED_EVENT_NAME : $INTERNAL_EVENT_NAME

	data class $REFRESH_FAILED_EVENT_NAME(
		val message: String
	) : $INTERNAL_EVENT_NAME
}
