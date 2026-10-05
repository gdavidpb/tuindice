package com.gdavidpb.tuindice.record.domain.model

/** A record edit the server refused for good. */
data class RecordRejection(
	val mutationId: String,
	val kind: RecordRejectionKind
)
