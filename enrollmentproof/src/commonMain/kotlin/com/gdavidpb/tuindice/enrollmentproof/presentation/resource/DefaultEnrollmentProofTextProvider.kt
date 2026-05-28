package com.gdavidpb.tuindice.enrollmentproof.presentation.resource

import org.jetbrains.compose.resources.getString
import tuindice.enrollmentproof.generated.resources.Res
import tuindice.enrollmentproof.generated.resources.error_default
import tuindice.enrollmentproof.generated.resources.error_enrollment_not_found
import tuindice.enrollmentproof.generated.resources.error_enrollment_unsupported
import tuindice.enrollmentproof.generated.resources.error_network_unavailable
import tuindice.enrollmentproof.generated.resources.error_service_unavailable
import tuindice.enrollmentproof.generated.resources.error_timeout

class DefaultEnrollmentProofTextProvider : EnrollmentProofTextProvider {
	override suspend fun serviceUnavailable(): String =
		getString(Res.string.error_service_unavailable)

	override suspend fun networkUnavailable(): String =
		getString(Res.string.error_network_unavailable)

	override suspend fun enrollmentNotFound(): String =
		getString(Res.string.error_enrollment_not_found)

	override suspend fun enrollmentUnsupported(): String =
		getString(Res.string.error_enrollment_unsupported)

	override suspend fun timeout(): String =
		getString(Res.string.error_timeout)

	override suspend fun defaultError(): String =
		getString(Res.string.error_default)
}
