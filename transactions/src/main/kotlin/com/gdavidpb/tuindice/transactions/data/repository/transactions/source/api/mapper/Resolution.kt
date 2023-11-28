package com.gdavidpb.tuindice.transactions.data.repository.transactions.source.api.mapper

import com.gdavidpb.tuindice.transactions.domain.model.Resolution
import com.gdavidpb.tuindice.transactions.domain.model.ResolutionAction
import com.gdavidpb.tuindice.transactions.domain.model.ResolutionType
import com.gdavidpb.tuindice.transactions.data.repository.transactions.source.api.model.ResolutionResponseAction
import com.gdavidpb.tuindice.transactions.data.repository.transactions.source.api.model.ResolutionResponseType
import com.gdavidpb.tuindice.transactions.data.repository.transactions.source.api.response.ResolutionResponse

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