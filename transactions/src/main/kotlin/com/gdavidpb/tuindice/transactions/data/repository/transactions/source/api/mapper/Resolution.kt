package com.gdavidpb.tuindice.transactions.data.repository.transactions.source.api.mapper

import com.gdavidpb.tuindice.transactions.data.repository.transactions.source.api.response.ResolutionResponse
import com.gdavidpb.tuindice.transactions.domain.model.Resolution
import com.gdavidpb.tuindice.transactions.domain.model.ResolutionAction
import com.gdavidpb.tuindice.transactions.domain.model.ResolutionType

fun ResolutionResponse.toResolution() = Resolution(
	uid = uid,
	localReference = localReference,
	remoteReference = remoteReference,
	type = ResolutionType.entries[type],
	action = ResolutionAction.entries[action],
	data = data
)