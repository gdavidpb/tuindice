package com.gdavidpb.tuindice.enrollmentproof.presentation.machine

import io.github.vinceglb.filekit.PlatformFile

/**
 * Internal machine inputs for the enrollment proof dialog: fetch outcomes split per
 * effect (open the file, report an error, or escalate outdated credentials), plus what
 * to say when the file could not be handed to a viewer.
 */
sealed interface EnrollmentProofInternalEvent {
	data class EnrollmentProofFetched(
		val file: PlatformFile
	) : EnrollmentProofInternalEvent

	/** The download failed for now and the copy saved earlier is all there is. */
	data class SavedEnrollmentProofFound(
		val file: PlatformFile
	) : EnrollmentProofInternalEvent

	data class EnrollmentProofFetchFailed(
		val message: String,
		val canRetry: Boolean
	) : EnrollmentProofInternalEvent

	data object EnrollmentProofUnauthorized : EnrollmentProofInternalEvent

	/** The proof is there and the device has nothing to open it with. */
	data class EnrollmentProofViewerMissing(
		val message: String
	) : EnrollmentProofInternalEvent
}
