package com.gdavidpb.tuindice.auth.presentation.resource

// The texts sign-in shares with other screens: its labels and titles, and the common error messages. Read through this
// seam so the walk over the sign-in machine runs on any host without platform resources.
interface SignInSharedTextProvider {
	suspend fun retryLabel(): String
	suspend fun termsAndConditionsTitle(): String
	suspend fun privacyPolicyTitle(): String
	suspend fun serviceUnavailable(): String
	suspend fun networkUnavailable(): String
	suspend fun timeout(): String
	suspend fun unexpectedError(): String
}
