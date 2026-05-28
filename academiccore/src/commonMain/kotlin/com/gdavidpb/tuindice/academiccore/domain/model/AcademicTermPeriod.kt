package com.gdavidpb.tuindice.academiccore.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AcademicTermPeriod(
	val sequence: Int,
	val label: String,
	val shortLabel: String
) {
	@SerialName("JAN_MAR")
	JAN_MAR(
		sequence = 1,
		label = "Enero - Marzo",
		shortLabel = "Ene - Mar"
	),

	@SerialName("APR_JUL")
	APR_JUL(
		sequence = 2,
		label = "Abril - Julio",
		shortLabel = "Abr - Jul"
	),

	@SerialName("JUL_AUG")
	JUL_AUG(
		sequence = 3,
		label = "Julio - Agosto",
		shortLabel = "Jul - Ago"
	),

	@SerialName("SEP_DEC")
	SEP_DEC(
		sequence = 4,
		label = "Septiembre - Diciembre",
		shortLabel = "Sep - Dic"
	);
}
