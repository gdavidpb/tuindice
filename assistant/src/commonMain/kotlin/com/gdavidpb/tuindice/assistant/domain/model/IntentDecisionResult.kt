package com.gdavidpb.tuindice.assistant.domain.model

/**
 * How the server took the user's decision on an intent: accepted, or refused because the intent had expired or
 * conflicted.
 */
sealed interface IntentDecisionResult {
	data object Accepted : IntentDecisionResult

	data object Expired : IntentDecisionResult

	data object Conflict : IntentDecisionResult
}
