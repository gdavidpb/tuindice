package com.gdavidpb.tuindice.base.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SyncReport(
	@SerialName("status") val status: SyncReportStatus,
	@SerialName("sources") val sources: SyncReportSources,
	// Local metadata, never on the wire: when a sync last read the enrollment. It tells how old the
	// current term on screen is when a later sync cannot refresh it. Reports persisted before the
	// field existed decode it as null.
	@SerialName("enrollment_read_at") val enrollmentReadAt: Long? = null
) {
	val hasUnavailableSource: Boolean
		get() = sources.hasUnavailableSource

	// A sync that read the enrollment (it came back Success or NotEnrolled) is the only one allowed
	// to say whether the situation is still there.
	val hasReadEnrollment: Boolean
		get() = sources.enrollment.status == SyncSourceStatus.Success ||
			sources.enrollment.status == SyncSourceStatus.NotEnrolled

	fun readAt(timestamp: Long): SyncReport {
		return if (hasReadEnrollment) copy(enrollmentReadAt = timestamp) else this
	}

	// The one carry-over operation. When the enrollment source could not be read (partial or failed
	// sync) the notice must not vanish nor the data look fresher than it is: the situation and the
	// read instant of the previous report are carried over.
	fun carryingEnrollmentFrom(previous: SyncReport): SyncReport {
		return if (hasReadEnrollment) this else withEnrollment(previous)
	}

	// Same carry-over regardless of what this report claims; for a failure that brought no body.
	fun withEnrollment(previous: SyncReport): SyncReport {
		return copy(
			sources = sources.copy(
				enrollment = sources.enrollment.copy(
					situation = previous.sources.enrollment.situation ?: sources.enrollment.situation
				)
			),
			enrollmentReadAt = previous.enrollmentReadAt
		)
	}

	companion object {
		fun success(): SyncReport {
			return SyncReport(
				status = SyncReportStatus.Success,
				sources = SyncReportSources(
					record = SyncSourceReport(SyncSourceStatus.Success),
					enrollment = SyncSourceReport(SyncSourceStatus.Success)
				)
			)
		}

		fun partialEnrollmentUnavailable(): SyncReport {
			return SyncReport(
				status = SyncReportStatus.Partial,
				sources = SyncReportSources(
					record = SyncSourceReport(SyncSourceStatus.Success),
					enrollment = SyncSourceReport(SyncSourceStatus.Unavailable)
				)
			)
		}

		fun failedRecordUnavailable(): SyncReport {
			return SyncReport(
				status = SyncReportStatus.Failed,
				sources = SyncReportSources(
					record = SyncSourceReport(SyncSourceStatus.Unavailable),
					enrollment = SyncSourceReport(SyncSourceStatus.NotAttempted)
				)
			)
		}
	}
}
