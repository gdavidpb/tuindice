package com.gdavidpb.tuindice.transactions.data.api.mapper

import com.gdavidpb.tuindice.base.domain.model.resolution.Resolution
import com.gdavidpb.tuindice.base.domain.model.resolution.ResolutionAction
import com.gdavidpb.tuindice.base.domain.model.resolution.ResolutionType
import com.gdavidpb.tuindice.transactions.data.api.model.resolution.ResolutionResponseAction
import com.gdavidpb.tuindice.transactions.data.api.model.resolution.ResolutionResponseType
import com.gdavidpb.tuindice.transactions.data.api.response.ResolutionResponse

fun ResolutionResponse.toResolution() = Resolution(
	uid = uid,
	localReference = localReference,
	remoteReference = remoteReference,
	type = type.toResolutionType(),
	action = action.toResolutionAction(),
	data = data
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