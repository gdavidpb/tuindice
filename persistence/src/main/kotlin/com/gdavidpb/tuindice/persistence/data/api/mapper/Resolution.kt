package com.gdavidpb.tuindice.persistence.data.api.mapper

import com.gdavidpb.tuindice.base.domain.model.resolution.Resolution
import com.gdavidpb.tuindice.base.domain.model.resolution.ResolutionAction
import com.gdavidpb.tuindice.base.domain.model.resolution.ResolutionType
import com.gdavidpb.tuindice.persistence.data.api.model.resolution.ResolutionResponseAction
import com.gdavidpb.tuindice.persistence.data.api.model.resolution.ResolutionResponseType
import com.gdavidpb.tuindice.persistence.data.api.response.ResolutionOperationResponse
import com.gdavidpb.tuindice.persistence.data.api.response.ResolutionResponse
import com.gdavidpb.tuindice.persistence.data.api.response.subject.SubjectUpdateResponse

fun ResolutionResponse.toResolution() = Resolution(
	uid = uid,
	localReference = localReference,
	remoteReference = remoteReference,
	type = type.toResolutionType(),
	action = action.toResolutionAction(),
	operation = operation.toResolutionOperation()
)

private fun ResolutionResponseType.toResolutionType() = when (this) {
	ResolutionResponseType.QUARTER -> ResolutionType.QUARTER
	ResolutionResponseType.SUBJECT -> ResolutionType.SUBJECT
	ResolutionResponseType.EVALUATION -> ResolutionType.EVALUATION
}

private fun ResolutionResponseAction.toResolutionAction() = when (this) {
	ResolutionResponseAction.ADD -> ResolutionAction.ADD
	ResolutionResponseAction.UPDATE -> ResolutionAction.UPDATE
	ResolutionResponseAction.DELETE -> ResolutionAction.DELETE
}

private fun ResolutionOperationResponse.toResolutionOperation() =
	when (this) {
		is SubjectUpdateResponse -> toSubjectUpdateResolution()
		else -> throw NoWhenBranchMatchedException()
	}