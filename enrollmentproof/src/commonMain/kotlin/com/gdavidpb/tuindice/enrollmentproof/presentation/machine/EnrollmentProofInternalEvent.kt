package com.gdavidpb.tuindice.enrollmentproof.presentation.machine

import io.github.vinceglb.filekit.PlatformFile

/**
 * Internal machine inputs for the enrollment proof dialog: fetch outcomes split per
 * effect (open the file, report an error, or escalate outdated credentials).
 */
sealed interface EnrollmentProofInternalEvent {
	data class EnrollmentProofFetched(
		val file: PlatformFile,
		val isFromCache: Boolean
	) : EnrollmentProofInternalEvent

	data class EnrollmentProofFetchFailed(
		val message: String,
		val canRetry: Boolean
	) : EnrollmentProofInternalEvent

	data object EnrollmentProofUnauthorized : EnrollmentProofInternalEvent
}
