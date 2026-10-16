package com.gdavidpb.tuindice.testing

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.CancellationException

// A reporting whose report fails with a cancellation that is not the one of the job that makes it: what a
// scope that reports the timeout of its cancellation pass would throw from inside cancelActiveWork().
internal class ForeignCancellationReporting(
	delegate: ReportingRepository = RecordingReportingRepository()
) : ReportingRepository by delegate {
	var thrown = 0
		private set

	override fun logException(throwable: Throwable) {
		thrown += 1
		throw CancellationException("a cancellation that is not the job's")
	}
}
