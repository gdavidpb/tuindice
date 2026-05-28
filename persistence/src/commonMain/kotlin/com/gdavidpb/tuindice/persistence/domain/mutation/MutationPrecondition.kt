package com.gdavidpb.tuindice.persistence.domain.mutation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface MutationPrecondition {
	@Serializable
	@SerialName("none")
	data object None : MutationPrecondition

	@Serializable
	@SerialName("revision")
	data class Revision(val value: Long) : MutationPrecondition
}
