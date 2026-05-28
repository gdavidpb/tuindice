package com.gdavidpb.tuindice.record.domain.model

enum class SyntheticTermLoadBand(
	val label: String
) {
	LIGHT(label = "Ligera"),
	MANAGEABLE(label = "Manejable"),
	NORMAL(label = "Normal"),
	DEMANDING(label = "Exigente"),
	VERY_DEMANDING(label = "Muy exigente")
}
