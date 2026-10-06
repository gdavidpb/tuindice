package com.gdavidpb.tuindice.enrollmentproof.data.repository

import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProofNotFoundReason

/** What the last sync said about the enrollment, as far as the proof is concerned. */
interface EnrollmentSyncDataRepository {
	/** Which message explains the absence of a proof. */
	suspend fun getNotFoundReason(): EnrollmentProofNotFoundReason

	/** Asks for a new sync when the last one denied the enrollment a proof has just confirmed. */
	suspend fun refreshWhenContradicted()
}
