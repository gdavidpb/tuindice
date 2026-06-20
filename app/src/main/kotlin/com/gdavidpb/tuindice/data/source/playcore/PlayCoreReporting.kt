package com.gdavidpb.tuindice.data.source.playcore

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository

internal fun ReportingRepository.reportPlayCoreFailure(
	message: String,
	throwable: Throwable
) {
	runCatching {
		logMessage(message)
		logException(throwable)
	}
}
