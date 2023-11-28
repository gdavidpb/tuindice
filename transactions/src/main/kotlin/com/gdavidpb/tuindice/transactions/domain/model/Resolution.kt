package com.gdavidpb.tuindice.transactions.domain.model

data class Resolution(
	val uid: String,
	val localReference: String,
	val remoteReference: String,
	val type: ResolutionType,
	val action: ResolutionAction,
	val data: String
)