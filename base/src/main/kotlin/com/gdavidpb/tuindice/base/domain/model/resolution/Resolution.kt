package com.gdavidpb.tuindice.base.domain.model.resolution

data class Resolution(
	val uid: String,
	val localReference: String,
	val remoteReference: String,
	val type: ResolutionType,
	val action: ResolutionAction,
	val data: String
)