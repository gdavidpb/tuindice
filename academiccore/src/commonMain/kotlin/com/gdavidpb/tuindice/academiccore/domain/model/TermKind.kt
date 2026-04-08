package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TermKind {
	@SerialName("official_historical")
	OFFICIAL_HISTORICAL,

	@SerialName("official_current")
	OFFICIAL_CURRENT,

	@SerialName("synthetic")
	SYNTHETIC
}

val TermKind.isOfficialHistorical: Boolean
	get() = this == TermKind.OFFICIAL_HISTORICAL

val TermKind.isOfficialCurrent: Boolean
	get() = this == TermKind.OFFICIAL_CURRENT

val TermKind.isSynthetic: Boolean
	get() = this == TermKind.SYNTHETIC

val TermKind.isEditable: Boolean
	get() = this != TermKind.OFFICIAL_HISTORICAL
