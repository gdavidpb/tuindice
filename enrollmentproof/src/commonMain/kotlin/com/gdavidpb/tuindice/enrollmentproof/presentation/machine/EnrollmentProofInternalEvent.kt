package com.gdavidpb.tuindice.enrollmentproof.presentation.machine

import io.github.vinceglb.filekit.PlatformFile

/**
 * Internal machine inputs for the enrollment proof dialog: fetch outcomes split per
 * effect (open the file, report an error, or escalate outdated credentials).
 */
sealed interface EnrollmentProofInternalEvent {
	data class EnrollmentProofFetched(
		val file: PlatformFile
	) : EnrollmentProofInternalEvent

	data class EnrollmentProofFetchFailed(
		val message: String
	) : EnrollmentProofInternalEvent

	data object EnrollmentProofUnauthorized : EnrollmentProofInternalEvent
}
