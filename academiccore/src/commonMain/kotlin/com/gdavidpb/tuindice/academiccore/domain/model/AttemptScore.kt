package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AttemptScore(
	val kind: AttemptScoreKind,
	@SerialName("numeric_value") val numericValue: Int? = null,
	@SerialName("symbolic_value") val symbolicValue: String? = null
) {
	companion object {
		fun empty(): AttemptScore = AttemptScore(kind = AttemptScoreKind.EMPTY)

		fun numeric(value: Int): AttemptScore = AttemptScore(
			kind = AttemptScoreKind.NUMERIC,
			numericValue = value
		)

		fun symbolic(value: String): AttemptScore = AttemptScore(
			kind = AttemptScoreKind.SYMBOLIC,
			symbolicValue = value
		)
	}
}
