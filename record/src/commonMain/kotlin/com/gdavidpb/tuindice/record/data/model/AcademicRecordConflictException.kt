package com.gdavidpb.tuindice.record.data.model

import io.ktor.client.plugins.ResponseException

/**
 * An overlay 409 with its body read. It is still a [ResponseException] (so every status check keeps
 * working); [currentRevision] is only there for `STALE_PRECONDITION`, which is the revision the
 * rejected write should have expected. `CONCURRENT_WRITE` does not carry one.
 */
class AcademicRecordConflictException(
	val reason: String?,
	val currentRevision: Long?,
	original: ResponseException
) : ResponseException(original.response, original.message.orEmpty())
