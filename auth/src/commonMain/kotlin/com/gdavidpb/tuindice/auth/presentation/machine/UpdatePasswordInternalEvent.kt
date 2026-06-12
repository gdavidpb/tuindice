package com.gdavidpb.tuindice.auth.presentation.machine

/**
 * Internal machine inputs for the password update: the submit lifecycle outcomes.
 */
sealed interface UpdatePasswordInternalEvent {
	data class PasswordUpdateSucceeded(
		val message: String
	) : UpdatePasswordInternalEvent

	data class PasswordUpdateFailed(
		val message: String
	) : UpdatePasswordInternalEvent
}
