package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TermKind {
	@SerialName("historical")
	HISTORICAL,

	@SerialName("current")
	CURRENT,

	@SerialName("synthetic")
	SYNTHETIC
}

val TermKind.isHistorical: Boolean
	get() = this == TermKind.HISTORICAL

val TermKind.isCurrent: Boolean
	get() = this == TermKind.CURRENT

val TermKind.isSynthetic: Boolean
	get() = this == TermKind.SYNTHETIC

val TermKind.isEditable: Boolean
	get() = this != TermKind.HISTORICAL
