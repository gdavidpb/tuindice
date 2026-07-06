package com.gdavidpb.tuindice.enrollmentproof.presentation.resource

import com.gdavidpb.tuindice.base.presentation.mapper.commonNetworkUnavailableMessage
import com.gdavidpb.tuindice.base.presentation.mapper.commonServiceUnavailableMessage
import com.gdavidpb.tuindice.base.presentation.mapper.commonTimeoutMessage
import com.gdavidpb.tuindice.base.presentation.mapper.commonUnexpectedErrorMessage
import org.jetbrains.compose.resources.getString
import tuindice.enrollmentproof.generated.resources.Res
import tuindice.enrollmentproof.generated.resources.error_enrollment_not_found
import tuindice.enrollmentproof.generated.resources.error_enrollment_unsupported

class DefaultEnrollmentProofTextProvider : EnrollmentProofTextProvider {
	override suspend fun serviceUnavailable(): String =
		commonServiceUnavailableMessage()

	override suspend fun networkUnavailable(): String =
		commonNetworkUnavailableMessage()

	override suspend fun enrollmentNotFound(): String =
		getString(Res.string.error_enrollment_not_found)

	override suspend fun enrollmentUnsupported(): String =
		getString(Res.string.error_enrollment_unsupported)

	override suspend fun timeout(): String =
		commonTimeoutMessage()

	override suspend fun defaultError(): String =
		commonUnexpectedErrorMessage()
}
