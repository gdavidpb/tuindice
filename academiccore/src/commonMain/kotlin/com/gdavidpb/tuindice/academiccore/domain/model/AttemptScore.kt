package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("type")
sealed interface AttemptScore {
	val numericValue: Int?
		get() = null

	val symbolicValue: String?
		get() = null

	companion object {
		fun empty(): AttemptScore = Empty

		fun numeric(value: Int): AttemptScore = Numeric(value)

		fun symbolic(value: String): AttemptScore = Symbolic(value)
	}

	@Serializable
	@SerialName("empty")
	data object Empty : AttemptScore

	@Serializable
	@SerialName("numeric")
	data class Numeric(
		@SerialName("value") val value: Int
	) : AttemptScore {
		init {
			require(value in 0..5) { "Numeric attempt score must be in 0..5." }
		}

		override val numericValue: Int
			get() = value
	}

	@Serializable
	@SerialName("symbolic")
	data class Symbolic(
		@SerialName("value") val value: String
	) : AttemptScore {
		init {
			require(value.isNotBlank()) { "Symbolic attempt score cannot be blank." }
		}

		override val symbolicValue: String
			get() = value
	}
}
