package com.gdavidpb.tuindice.enrollmentproof.domain.model

import io.github.vinceglb.filekit.PlatformFile

/** The proof ready to open. [isFromCache] when it is the saved copy because a fresh one could not be had. */
data class FetchedEnrollmentProof(
	val file: PlatformFile,
	val isFromCache: Boolean
)
